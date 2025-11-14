export const ProblemScreen = ({problemMessage}) => {
  return (
    <>
      <h1>Issue</h1>
      <p>There is an issue with this machine, please contact service desk</p>

      {problemMessage ?
        (
          <p><strong>Problem</strong>: {problemMessage}</p>
        )
        : <strong>Unknown Problem</strong>}
    </>
  )
}