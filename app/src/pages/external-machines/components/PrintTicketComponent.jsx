import {useState} from "react";
import {ticketService} from "../../../services/ticket-service.js";
import {TicketComponent} from "./TicketComponent.jsx";

export const PrintTicketComponent = () => {
  const [ticket, setTicket] = useState(undefined);
  const [error, setError] = useState(undefined);

  const printTicket = async () => {
    ticketService.createTicket()
      .then(setTicket)
      .catch(err => {
        setError("could not create ticket")
        console.error(err);
      })
  }

  return (
    <>
      {error && (
        <p>{error}</p>
      )}
      {ticket
        ? <TicketComponent ticket={ticket}/>
        : <button onClick={printTicket}>Print ticket</button>
      }

    </>
  )
}