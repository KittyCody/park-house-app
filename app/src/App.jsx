import './App.css'
import {useAuth} from "./providers/AuthContext.jsx";
import {ProblemScreen} from "./pages/ProblemScreen.jsx";
import {ExternalMachineScreen} from "./pages/external-machines/ExternalMachineScreen.jsx";
import {InternalMachineScreen} from "./pages/internal-machines/InternalMachineScreen.jsx";

function App() {

  const {accessToken, machineType} = useAuth()

  if (accessToken === undefined) {
    return <ProblemScreen problemMessage="Authentication issue"/>
  }

  if (machineType === undefined) {
    return <ProblemScreen problemMessage="Configuration issue"/>
  }

  switch (machineType) {
    case 'internal_machine':
      return <InternalMachineScreen/>
    case 'external_machine':
      return <ExternalMachineScreen/>
    default:
      return <ProblemScreen problemMessage="Configuration issue: machine type"/>
  }
}

export default App
