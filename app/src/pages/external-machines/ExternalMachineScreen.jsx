import {useEffect, useState} from "react";
import {PrintTicketComponent} from "./components/PrintTicketComponent.jsx";
import {ParkingFullComponent} from "./components/ParkingFullComponent.jsx";
import {OutOfHoursComponent} from "./components/OutOfHoursComponent.jsx";
import {parkingService} from "../../services/parking-service.js";

export const ExternalMachineScreen = () => {
  const [availableSpaces, setAvailableSpaces] = useState(undefined)
  const [isOpen, setIsOpen] = useState(undefined)
  const [error, setError] = useState(undefined)

  useEffect(() => {
    parkingService.getStatus()
      .then(status => {
        setAvailableSpaces(status.availableSpaces)
        setIsOpen(status.isOpen)
      })
      .catch(err => {
        setError("could not fetch parking status")
        console.error(err)
      })
  }, [])

  return (
    <>
      <h1>Welcome to Park House!</h1>
      {error && <p>{error}</p>}
      {isOpen
        ? <>
          <p>{availableSpaces ? `${availableSpaces} spaces left` : 'No spaces left'}</p>
          {availableSpaces > 0
            ? <PrintTicketComponent/>
            : <ParkingFullComponent/>}
        </>
        : <OutOfHoursComponent/>}
    </>
  )
}