import React, { useCallback, useContext, useEffect, useRef, useState } from "react"
import MessageInputContainer from "@/components/ui/message-input"
import MessageBoxListContainer from "@/components/ui/message-box-list"
import { ChatMessage, Cursor } from "@/types/chat"
import proxy from "@/utils/proxy"

export default function ChatUi() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const messageNextKey = useRef<number>(1)
  const getMessagesLength = 20

  const toChatMessageFormat = (messageList: ChatMessage[]): ChatMessage[] => {
    return messageList.map((message) => {
      const newMessage = { ...message }
      newMessage.key = messageNextKey.current
      messageNextKey.current += 1
      return newMessage
    })
  }
  const addMessage = (message: string, isFromChatbot: boolean) => {
    setMessages((messagesState) => {
      const chatMessage: ChatMessage = {
        key: 0,
        content: message,
        isFromChatbot,
      }
      return [...messagesState, ...toChatMessageFormat([chatMessage])]
    })
  }

  const handleStreamMessage = async (message: string) => {
    setMessages((prevState) => {
      const lastMessage = prevState[prevState.length - 1]
      if (lastMessage && lastMessage.isFromChatbot) {
        lastMessage.content = message
        return [...prevState.slice(0, -1), lastMessage]
      }
      const chatMessage: ChatMessage = {
        key: 0,
        content: message,
        isFromChatbot: true,
      }
      return [...prevState, ...toChatMessageFormat([chatMessage])]
    })
  }

  const handleStreamEndWhichCaseUser = (userMessageId: number, chatbotMessageId: number, regenerate: boolean) => {
    setMessages((prevState) => {
      const chatbotMessage = prevState[prevState.length - 1]
      chatbotMessage.id = chatbotMessageId
      if (regenerate) {
        return [...prevState.slice(0, -1), chatbotMessage]
      }
      const userMessage = prevState[prevState.length - 2]
      userMessage.id = userMessageId
      return [...prevState.slice(0, -2), userMessage, chatbotMessage]
    })
  }

  const prepareRegenerate = () => {
    setMessages((prevState) => prevState.slice(0, -1))
  }

  const getMessages = useCallback(
    (_cursor: Cursor) => {
      if (_cursor.key === -1) return
      const params = {
      }
      proxy
        .get(`/chatRooms/${chatRoomId}/messages`, { params })
        .then((res) => {
          const patchedMessages = res.data.response.body.messages
          if (_cursor.key === undefined) {
            setMessages((prev) => {
              const remain = prev.filter((chatMessage) => chatMessage.id === undefined)
              return [...toChatMessageFormat(patchedMessages), ...remain]
            })
          } else {
            setMessages((prev) => [...toChatMessageFormat(patchedMessages), ...prev])
          }
        })
        .catch((res) => {
          alert(res.response.data.errorMessage)
        })
    },
    [chatRoomId],
  )

  useEffect(() => {
    const defaultCursor = { size: getMessagesLength }
    if (chatRoomId === 0) {
      setMessages([])
    } else {
      getMessages(defaultCursor)
    }
  }, [chatroomId, getMessages])

  useEffect(() => {
    setChatRoomId(0)
    setMessages([])
  }, [userId, setChatRoomId])

  return (
    <div className="flex flex-col min-h-full">
      <MessageBoxListContainer
        messages={messages}
      />
      <MessageInputContainer
        messages={messages}
        handleStreamMessage={handleStreamMessage}
        handleStreamEndWhichCaseUser={handleStreamEndWhichCaseUser}
        addUserMessage={(message: string) => addMessage(message, false)}
        prepareRegenerate={prepareRegenerate}
      />
    </div>
  )
}