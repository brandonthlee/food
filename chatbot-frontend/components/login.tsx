import { useState } from "react"
import Modal from "@/components/modal"
import proxy from "@/utils/proxy"
import { saveJwt } from "@/utils/jwtDecoder"
import { CardTitle, CardHeader, CardContent, CardFooter, Card } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

export default function LoginModal({
  onClickClose
}: {
  onClickClose(): void
  onClickJoin(): void
  onClickFindId(): void
  onClickFindPassword(): void
}) {
  const [formData, setFormData] = useState({
    loginId: "",
    password: "",
  })
  const [error, setError] = useState("")

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }))
  }

  const validateLoginId = () => {
    let isValid = true
    if (formData.loginId.trim() === "") {
      setError("Enter username")
      isValid = false
    }

    return isValid
  }

  const validatePassword = () => {
    let isValid = true

    if (formData.password === "") {
      setError("Enter password")
      isValid = false
    }

    return isValid
  }

  const validateForm = async () => {
    if (!validateLoginId()) return false
    if (!validatePassword()) return false

    return true
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!(await validateForm())) {
      return
    }

    proxy
      .post("/login", {
        loginId: formData.loginId,
        password: formData.password,
      })
      .then((res) => {
        const jwt = res.headers.authorization
        saveJwt(jwt)
        onClickClose()
      })
      .catch(() => {
        setError("Invalid username or password")
      })
  }

  return (
    <Modal onClickClose={onClickClose}>
      <div className="p-5 h-0 grow">
        <form className="max-h-full" onSubmit={handleSubmit}>
          <div className="flex flex-col items-center">
          <Card className="w-full max-w-md">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl">Enter your username and password</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4">
            <div className="grid gap-2">
              <Label htmlFor="username">Username</Label>
              <Input id="username" placeholder="Enter your username" type="text" />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">Password</Label>
              <Input id="password" placeholder="Enter your password" type="password" />
            </div>
          </CardContent>
          <CardFooter>
            <Button className="w-full">Login</Button>
          </CardFooter>
        </Card>
          </div>
        </form>
      </div>
    </Modal>
  )
}