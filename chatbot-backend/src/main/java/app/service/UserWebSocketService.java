package app.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dto.ChatbotRequest;
import app.dto.ChatUserRequest;
import app.dto.ChatUserResponse;
import app.exception.ChatRoomNotFoundException;
import app.exception.ResouceLimitException;
import app.exception.UserForbiddenException;
import app.model.ChatLog;
import app.model.ChatRoom;
import app.model.Food;
import app.model.Message;
import app.model.MessageId;
import app.model.MyFunction;
import app.model.UserPreference;
import app.repository.ChatLogRepository;
import app.repository.ChatRoomRepository;
import app.repository.MessageRepository;
import app.repository.UserPreferenceRepository;
import app.repository.UserRepository;
import app.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles WebSocket communication and coordinates user-chat related operations
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserWebSocketService {

    @Value("${chatbot.url}")
    private String serverUri;

    private final ObjectMapper objMapper;

    private final UserRepository userRepository;

    private final MessageRepository messageRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatLogRepository chatLogRepository;

    private final UserPreferenceRepository preferenceRepository;

    public void requestToChatbot(ChatbotRequest.MessageDto messageDto, WebSocketSession user, Long chatRoomId)
            throws IOException {

        var chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        sendMessageToChatbot(messageDto, user, messageDto.regenerate() ? message -> {
            Message oldMessage = messageRepository.findTop1ByChatroomIdOrderByIdDesc(chatRoomId).orElse(null);
            if (oldMessage != null && oldMessage.isFromChatbot()) {
                oldMessage.updateContent(message);
                var chatbotMessageId = messageRepository.save(oldMessage).getId();
                return new MessageId(null, chatbotMessageId);
            }
            Message chatbotMessage = Message.builder().chatRoom(chatRoom).isFromChatbot(true).content(message).build();
            var chatbotMessageId = messageRepository.save(chatbotMessage).getId();
            return new MessageId(null, chatbotMessageId);
        } : message -> {
            String userInput = messageDto.user_input();
            if (messageDto.is_favor_chat()) {
                int startIndex = messageDto.user_input().indexOf(".");
                if (startIndex != -1) {
                    userInput = messageDto.user_input().substring(startIndex + 1);
                }
            }
            var userMessage = Message.builder().chatRoom(chatRoom).isFromChatbot(false).content(userInput).build();

            var userMessageId = messageRepository.save(userMessage).getId();

            Message chatbotMessage = Message.builder().chatRoom(chatRoom).isFromChatbot(true).content(message).build();

            var chatbotMessageId = messageRepository.save(chatbotMessage).getId();
            return new MessageId(userMessageId, chatbotMessageId);
        });
    }

    public void requestToChatbotPublic(ChatbotRequest.MessageDto messageDto, WebSocketSession user,
            String requestMessage) throws IOException {
        
        var ip = getClientIp(user);
        Long todayRequestNum = chatLogRepository.countByIpAndCreatedAtBetween(ip, LocalDate.now().atStartOfDay(),
                LocalDateTime.now());
        if (todayRequestNum >= 20) {
            log.info(ip + ": Reached daily request limit");
            throw new ResouceLimitException("Reached daily request limit!");
        }
        sendMessageToChatbot(messageDto, user, (message) -> {
            ChatLog chatLog = ChatLog.builder().ip(getClientIp(user)).requestMessage(requestMessage)
                    .output(message).build();
            chatLogRepository.save(chatLog);
            log.debug("public api last message sent!!\n" + message);
            return new MessageId(null, null);
        });
    }

    public Long getUserId(WebSocketSession session) throws IOException {
        String token = extractTokenFromSession(session);
        DecodedJWT decodedJWT = JwtProvider.verify(token);
        return decodedJWT.getClaim("id").asLong();
    }

    @Transactional
    public ChatbotRequest.MessageDto makeChatbotRequestDto(ChatUserRequest.MessageDto userMessageDto, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByIdJoinUser(userMessageDto.chatroomId())
                .orElseThrow(() -> new ChatRoomNotFoundException("ChatRoom not found!"));

        if (!Objects.equals(userId, chatRoom.getUser().getId()))
            throw new UserForbiddenException("You don't have permission to access this chat room!");

        var messages = messageRepository.findTop38ByChatroomIdOrderByIdDesc(userMessageDto.chatroomId());

        List<List<String>> history = makeHistoryFromMessages(messages);
        var favorString = makeFavorString(userId);
        var processedUserInput = new StringBuilder(userMessageDto.input());

        if (userMessageDto.regenerate()) {
            if (!history.isEmpty()) {
                var originalMessageList = new ArrayList<>(history.get(history.size() - 1));

                originalMessageList.set(0, favorString + originalMessageList.get(0));
                history.set(history.size() - 1, originalMessageList);
            }
        } else {
            processedUserInput.insert(0, favorString);
        }
        return new ChatbotRequest.MessageDto(processedUserInput.toString(), userMessageDto.regenerate(), history,
                chatRoom.getUser().getName(), !favorString.isEmpty());
    }

    private String getClientIp(WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeHeaders();
        String realIp = headers.getFirst("X-Real-IP");

        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        } else {
            return Objects.requireNonNull(session.getRemoteAddress()).getAddress().getHostAddress();
        }
    }

    private void sendMessageToChatbot(ChatbotRequest.MessageDto chatbotMessageDto, WebSocketSession user,
            MyFunction function) throws IOException {
        String messageToSend = objMapper.writeValueAsString(chatbotMessageDto);

        ChatbotWebSocketService chatbotWebSocketService = new ChatbotWebSocketService(serverUri);
        try {
            chatbotWebSocketService.sendMessage(messageToSend);
        } catch (Exception e) {
            throw new ResouceLimitException("Error occurred while sending message to chatbot!");
        }
        chatbotWebSocketService.listenForMessages(user, function);
    }

    private String makeFavorString(Long userId) {
        var favors = preferenceRepository.findByUserId(userId);

        if (favors.isEmpty()) {
            return "";
        }

        var favorFoodNames = favors.stream().map(favor -> favor.getFood().getName()).toList();

        StringBuilder result = new StringBuilder("I like ");

        List<String> randomFoods = new ArrayList<>();

        if (favors.size() > 5) {
            List<String> shuffledFoods = new ArrayList<>(favorFoodNames);
            Collections.shuffle(shuffledFoods);
            randomFoods = shuffledFoods.subList(0, 5);
        } else {
            randomFoods = favorFoodNames;
        }
        for (var food : randomFoods) {
            result.append(food).append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append(". Please recommend me based on this.");
        return result.toString();
    }

    private List<String> makeFavorMessages(Long userId) {
        var preferences = preferenceRepository.findByUserId(userId);
        List<String> favorMessages = new ArrayList<>();

        if (!preferences.isEmpty()) {

            Random random = new Random();
            int randomFoodIndex = random.nextInt(preferences.size());
            Food randomFood = preferences.get(randomFoodIndex).getFood();
            String randomFoodName = randomFood.getName();

            // Maps to count the frequency of each element
            Map<String, Integer> flavorCounts = new HashMap<>();
            Map<String, Integer> countryCounts = new HashMap<>();
            Map<String, Integer> temperatureCounts = new HashMap<>();
            Map<String, Integer> mainIngredientCounts = new HashMap<>();
            Map<String, Integer> spicyCounts = new HashMap<>();
            Map<String, Integer> oilyCounts = new HashMap<>();

            for (UserPreference preference : preferences) {
                Food food = preference.getFood();
                var foodFlavor = food.getFlavor();
                var foodCountry = food.getCountry();
                var foodTemperature = food.getTemperature();
                var foodMainIngredient = food.getIngredient();
                var foodSpicy = toSpicyString(food.getSpicy());
                var foodOily = food.getOily();

                // Count the frequency of each element
                flavorCounts.put(foodFlavor, flavorCounts.getOrDefault(foodFlavor, 0) + 1);
                countryCounts.put(foodCountry, countryCounts.getOrDefault(foodCountry, 0) + 1);
                temperatureCounts.put(foodTemperature, temperatureCounts.getOrDefault(foodTemperature, 0) + 1);
                mainIngredientCounts.put(foodMainIngredient,
                        mainIngredientCounts.getOrDefault(foodMainIngredient, 0) + 1);
                spicyCounts.put(foodSpicy, spicyCounts.getOrDefault(foodSpicy, 0) + 1);
                oilyCounts.put(foodOily, oilyCounts.getOrDefault(foodOily, 0) + 1);
            }

            // Find the most preferred element in each category
            String mostPreferredFlavor = findMostPreferredElement(flavorCounts);
            String mostPreferredCountry = findMostPreferredElement(countryCounts);
            String mostPreferredTemperature = findMostPreferredElement(temperatureCounts);
            String mostPreferredMainIngredient = findMostPreferredElement(mainIngredientCounts);
            String mostPreferredSpicy = findMostPreferredElement(spicyCounts);
            String mostPreferredOily = findMostPreferredElement(oilyCounts);

            var userMessage = "My favorite flavor is '" + mostPreferredFlavor + "', "
                    + "I prefer foods from '" + mostPreferredCountry + "', "
                    + "I prefer the temperature to be '" + mostPreferredTemperature + "', "
                    + "I prefer main ingredients like '" + mostPreferredMainIngredient + "', "
                    + "I prefer the spiciness to be '" + mostPreferredSpicy + "', and I prefer foods with '"
                    + mostPreferredOily + "' level of oiliness. "
                    + "For example, I like '" + randomFoodName + "'. "
                    + "Please respond based on my preferences in future conversations!";
            var chatbotMessage = "Got it, I've remembered your preferences! I'll respond based on these preferences from now on.";
            favorMessages.add(userMessage);
            favorMessages.add(chatbotMessage);
            log.info(userMessage);
        }
        return favorMessages;
    }

    // Find the most preferred element in a map
    private String findMostPreferredElement(Map<String, Integer> elementCounts) {
        String mostPreferredElement = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : elementCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostPreferredElement = entry.getKey();
            }
        }

        return mostPreferredElement;
    }

    // Define the toSpicyString method (converts spiciness level to text)
    private String toSpicyString(int spicy) {
        return switch (spicy) {
            case 1 -> "Mild";
            case 2 -> "Moderate";
            case 3 -> "Spicy";
            default -> "Not Spicy";
        };
    }

    // Convert messages into a history format
    private List<List<String>> makeHistoryFromMessages(List<Message> messages) {
        List<String> reversedMessages = new ArrayList<>();

        for (Message message : messages) {

            var isChatbotTurn = reversedMessages.size() % 2 == 0;

            if (isChatbotTurn && !message.isFromChatbot()) {
                reversedMessages.add("");
            }
            if (!isChatbotTurn && message.isFromChatbot()) {
                reversedMessages.add("");
            }

            reversedMessages.add(message.getContent());

            if (reversedMessages.size() >= 38)
                break;
        }
        if (reversedMessages.size() % 2 == 1)
            reversedMessages.remove(reversedMessages.size() - 1);

        List<List<String>> history = new ArrayList<>();

        for (int i = reversedMessages.size() - 1; i >= 0; i -= 2) {
            history.add(List.of(reversedMessages.get(i), reversedMessages.get(i - 1)));
        }

        return history;
    }

    // Create an error message
    public TextMessage createErrorMessage(String message) {
        var errorResponse = new ChatUserResponse.MessageDto("error", message);
        try {
            return new TextMessage(objMapper.writeValueAsString(errorResponse));
        } catch (JsonProcessingException e) {
            log.error("Error caught while creating error message: " + message
			return new TextMessage("{\"event\":\"error\",\"response\":\"Server error\"}");
		}
	}

	public String extractTokenFromSession(WebSocketSession session) throws IOException {
		String queryString = Objects.requireNonNull(session.getUri()).getQuery();

		if (queryString == null) {
			log.info(session + "Client token does not exist");
			throw new RuntimeException();
		}
		String tokenParam = "token=";
		int tokenParamIndex = queryString.indexOf(tokenParam);
		if (tokenParamIndex == -1) {
			throw new RuntimeException();
		}
		int tokenValueStartIndex = tokenParamIndex + tokenParam.length();
		int tokenValueSplitIndex = queryString.indexOf('&', tokenValueStartIndex);
		int tokenValueEndIndex = tokenValueSplitIndex == -1 ? queryString.length() : tokenValueSplitIndex;
		return queryString.substring(tokenValueStartIndex, tokenValueEndIndex);
	}
}
