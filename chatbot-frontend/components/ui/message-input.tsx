import Image from "next/image"
import { useContext, useState } from "react"
import { ChatMessage } from "@/types/chat"
import { AuthContext } from "@/contexts/authContext"
import { ChatRoomContext } from "@/contexts/chatRoomContext"
import { getJwtExp, getJwtTokenFromStorage, saveJwt } from "@/utils/jwtDecoder"
import proxy from "@/utils/proxy"

export default function MessageInputContainer({
  messages,
  handleStreamMessage,
  addUserMessage,
  prepareRegenerate,
  handleStreamEndWhichCaseUser,
}: {
  messages: ChatMessage[]
  handleStreamMessage: (message: string) => void
  addUserMessage: (message: string) => void
  prepareRegenerate: () => void
  handleStreamEndWhichCaseUser: (userMessageId: number, chatbotMessageId: number, regenerate: boolean) => void
}) {
  const [isGenerating, setIsGenerating] = useState(false)

  const { userId, isLoad } = useContext(AuthContext)
  const { chatRoomId, setchatRoomId } = useContext(ChatRoomContext)

  const resizeBox = () => {
    const userInputBox = document.querySelector<HTMLTextAreaElement>("#user-input-box")

    if (userInputBox !== null) {
      userInputBox.style.height = `${Math.min(userInputBox.scrollHeight, 120)}px`
    }
  }

  const makeHistory = () => {
    const messages38 = messages.slice(-38)
    const formattedMessages: string[] = []
    messages38.forEach((message) => {
      const isChatbotTurn = formattedMessages.length % 2 === 1

      if (isChatbotTurn && !message.isFromChatbot) {
        formattedMessages.push("")
      }
      if (!isChatbotTurn && message.isFromChatbot) {
        formattedMessages.push("")
      }
      formattedMessages.push(message.content)
    })

    if (formattedMessages.length % 2 === 1) formattedMessages.push("")

    const formattedMessages38 = formattedMessages.slice(-38)

    const history: string[][] = []
    for (let i = 0; i < formattedMessages38.length; i += 2) {
      history.push([formattedMessages38[i], formattedMessages38[i + 1]])
    }
    return history
  }

  async function generateFoodieResponse(userInputValue: string, regenerate: boolean) {
    let url = `${process.env.NEXT_PUBLIC_WS_URL}/api`
    let chatRoomIdToSend = chatRoomId
    if (userId === 0) {
      url += "/public-chat"
    } else {
      const jwtExp = getJwtExp()
      let jwt = getJwtTokenFromStorage() ?? ""
      if (jwtExp != null && jwtExp * 1000 < Date.now()) {
        const res = await proxy.post("/authentication")
        jwt = res.headers.authorization
        saveJwt(jwt)
      }
      url += `/chat?token=${jwt}`

      if (chatRoomIdToSend === 0) {
        const res = await proxy.post("/chatRooms")
        chatRoomIdToSend = res.data.response.chatRoomId
        setChatroomId(chatRoomIdToSend)
      }
    }

    const socket = new WebSocket(url)

    let successConnect = false

    socket.addEventListener("open", () => {
      successConnect = true
      let messageToSend: string
      if (userId === 0) {
        messageToSend = JSON.stringify({
          input: userInputValue,
          history: makeHistory(),
          regenerate,
        })
      } else {
        messageToSend = JSON.stringify({
          input: userInputValue,
          chatRoomId: chatRoomIdToSend,
          regenerate,
        })
      }
      socket.send(messageToSend)
    })

    socket.addEventListener("message", (event) => {
      const res = JSON.parse(event.data)
      switch (res.event) {
        case "text_stream": {
          handleStreamMessage(res.response)
          return
        }
        case "stream_end":
          if (userId !== 0) {
            handleStreamEndWhichCaseUser(
              Number(res.response.userMessageId),
              Number(res.response.chatbotMessageId),
              regenerate,
            )
          }
          break
        case "error":
          alert(res.response)
          break
        default:
          alert("Encountered unexpected error!")
      }
      socket.close()
    })

    socket.addEventListener("close", () => {
      setIsGenerating(false)
      if (!successConnect) alert("Cannot connect to server!")
    })
  }

  const onRegenerateClick = () => {
    if (isGenerating) return
    setIsGenerating(true)
    prepareRegenerate()
    generateFoodieResponse("", true).then(() => {})
  }

  const onSendClick = () => {
    if (!isLoad || isGenerating) return
    const userInputBox = document.querySelector<HTMLTextAreaElement>("#user-input-box")

    const userInputValue = userInputBox!.value
    if (userInputValue) {
      addUserMessage(userInputValue)
      setIsGenerating(true)
      generateFoodieResponse(userInputValue, false).then(() => {})
      userInputBox!.value = ""
      resizeBox()
    }
  }

  const exampleQuestions = [
    "Based on my food preferences, could you recommend a dish for me to try tonight?"
  ]

  const renderExampleButtons = () => {
    const maxButtons = exampleQuestions.length
    return exampleQuestions.slice(0, maxButtons).map((question) => (
      <button
        type="button"
        key={question}
        className="px-4 py-2 text-orange-500 rounded-lg border border-black hover:bg-orange-500 hover:text-white hover:border-white max-md:text-sm"
        onClick={() => {
          addUserMessage(question)
          setIsGenerating(true)
          generateFoodieResponse(question, false).then(() => {})
        }}
      >
        {question}
      </button>
    ))
  }

  return (
    <div className="sticky bottom-6 max-md:bottom-10 flex flex-col items-center bg-white">
      {isLoad && messages.length === 0 && (
        <div
          className={`justify-center w-[50%] max-lg:w-[70%] max-md:w-[90%] grid gap-4 mb-2 `}
        >
          {renderExampleButtons()}
        </div>
      )}
      <div className="flex justify-center items-center mt-3 mb-6 max-md:mb-2 w-[50%] max-lg:w-[70%] max-md:w-[90%] border-2 border-solid border-gray-400 rounded py-3 max-md:py-2 box-content focus-within:shadow-[0_0_4px_4px_rgba(0,0,0,0.1)]">
        <button
          type="button"
          className={`px-8 border bg-white border-gray-400 rounded flex justify-center items-center py-1.5 mb-4 absolute -top-9 opacity-70 hover:opacity-100 transition${
            messages.length !== 0 && !isGenerating ? "" : " invisible"
          }`}
          onClick={onRegenerateClick}
        >
          <Image
            src="/svg/refresh.svg"
            alt=""
            width="16"
            height="16"
            style={{ width: "16px", height: "16px" }}
            className="max-md:h-3.5 max-md:w-3.5"
          />
          <p className="ml-2 text-sm max-md:text-xs">Refresh</p>
        </button>
        <textarea
          className="w-full focus:outline-none pl-5 mr-5 custom-scroll-bar-4px overflow-y scroll resize-none h-6 max-md:text-sm max-md:h-5"
          id="user-input-box"
          onChange={(e) => {
            limitInputNumber(e, 500)
            resizeBox()
          }}
          onKeyDown={(e) => {
            pressEnter(e, onSendClick)
          }}
          placeholder="Enter message"
        />
        <button
          type="button"
          className={`w-10 flex justify-center items-center${isGenerating ? " hover:cursor-default" : ""}`}
          onClick={onSendClick}
        >
          {isGenerating ? (
            <div className="-translate-x-2">
              <div className="dot-elastic" />
            </div>
          ) : (
            <Image src="/svg/send.svg" alt="전송" width="16" height="16" />
          )}
        </button>
      </div>
    </div>
  )
}