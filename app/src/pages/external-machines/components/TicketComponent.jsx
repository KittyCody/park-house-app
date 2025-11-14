export const TicketComponent = ({ticket}) => {
  return (
    <>
      <p>Entered at: {ticket.timeOfEntry}</p>
      <p>Entered from gate: {ticket.entryGateId}</p>
    </>
  )
}