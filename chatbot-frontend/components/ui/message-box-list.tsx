import React, { useContext } from "react"
import { ChatMessage } from "@/types/chat"
import { ChatroomContext } from "@/contexts/chatRoomContext"

export default function MessageBoxListContainer({
  messages,
}: {
  messages: ChatMessage[]
}) {
  const { chatroomId } = useContext(ChatroomContext)

  const exampleUserMessage: ChatMessage = {
    key: 1,
    content: "Based on my food preferences, could you recommend a dish for me to try tonight?",
    isFromChatbot: false,
  }

  const exampleChatbotMessage: ChatMessage = {
    key: 2,
    content:
    "Of course! Since you enjoy spicy food, I recommend trying Buldak Bokkeum Myun (Fire Noodles).
    These are extremely spicy stir-fried noodles that have gained popularity for their intense heat.
    They are delicious and come with a kick that spicy food lovers appreciate.",
    isFromChatbot: true,
  }

  return (
    <div className="grow flex justify-center items-center h-auto">
      {messages.length === 0 && chatroomId === 0 ? (
        <MessageBoxList messages={[exampleUserMessage, exampleChatbotMessage]} />
      ) : (
        <MessageBoxList
          messages={messages}
          // cursor={cursor}
          // getMessages={getMessages}
        />
      )}
    </div>
  )
}