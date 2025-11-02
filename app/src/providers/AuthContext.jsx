import {createContext, useContext, useEffect, useState} from "react";

const AuthContext = createContext()

export const AuthProvider = ({children}) => {
  const [accessToken, setAccessToken] = useState(undefined)
  const [machineType, setMachineType] = useState(undefined)

  useEffect(() => {
    const token = import.meta.env.VITE_ACCESS_TOKEN
    const machineType = import.meta.env.VITE_MACHINE_TYPE;

    setAccessToken(token)
    setMachineType(machineType)
  }, [])

  return (
    <AuthContext.Provider value={{accessToken, machineType}}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within a AuthProvider')
  }

  return context
}