import './App.css'
import {useAuth} from "./providers/AuthContext.jsx";
import {ProblemScreen} from "./pages/ProblemScreen.jsx";
import {InternalMachineScreen} from "./pages/internal-machines/InternalMachineScreen.jsx";
import {BrowserRouter, Navigate, Route, Routes} from "react-router";
import {ExternalMachineScreen} from "./pages/external-machines/ExternalMachineScreen.jsx";
import {ScanTicketScreen} from "./pages/internal-machines/scanTicketScreen.jsx";

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
      return <BrowserRouter>
        <Routes>
          <Route path="/exit" element={<ScanTicketScreen/>}/>
          <Route path="/exit/:ticketId" element={<InternalMachineScreen/>}/>
          <Route path="/" element={<Navigate to="/exit"/>}/>
        </Routes>
      </BrowserRouter>
    case 'external_machine':
      return <ExternalMachineScreen/>

    default:
      return <ProblemScreen problemMessage="Configuration issue: machine type"/>
  }
}

export default App
