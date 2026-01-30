import {useState} from "react";
import {ticketService} from "../../../services/ticket-service.js";
import {TicketComponent} from "./TicketComponent.jsx";
import {Alert, Box, Button, CircularProgress, Paper, Stack, Typography,} from "@mui/material";
import LocalParkingIcon from "@mui/icons-material/LocalParking";

export const PrintTicketComponent = ({onPrintedTicket}) => {
  const [ticket, setTicket] = useState(undefined);
  const [error, setError] = useState(undefined);
  const [loading, setLoading] = useState(false);

  const printTicket = async () => {
    setError(undefined);
    setLoading(true);

    ticketService
      .createTicket()
      .then((ticket) => {
        setTicket(ticket);
        onPrintedTicket();
      })
      .catch((err) => {
        setError("Could not create ticket");
        console.error(err);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  if (ticket) {
    return (
      <Paper
        elevation={4}
        sx={{
          p: 2.5,
          borderRadius: 3,
          textAlign: "center",
          bgcolor: "success.light",
        }}
      >
        <Stack spacing={1.5}>
          <Typography variant="h6" fontWeight={800}>
            Ticket printed
          </Typography>

          <Typography variant="body1">
            Please take your ticket below.
          </Typography>

          <TicketComponent ticket={ticket}/>
        </Stack>
      </Paper>
    );
  }

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{fontSize: "1rem"}}>
          {error}
        </Alert>
      )}

      <Paper
        elevation={3}
        sx={{
          p: 2.5,
          borderRadius: 3,
        }}
      >
        <Stack spacing={2} alignItems="center">
          <Typography variant="h6" textAlign="center">
            Press the button to get a ticket
          </Typography>

          <Box width="100%">
            <Button
              fullWidth
              size="large"
              variant="contained"
              startIcon={!loading && <LocalParkingIcon/>}
              onClick={printTicket}
              disabled={loading}
              sx={{
                minHeight: 64,
                fontSize: "1.1rem",
              }}
            >
              {loading ? (
                <>
                  <CircularProgress size={26} sx={{mr: 1}}/>
                  Printing…
                </>
              ) : (
                "Print ticket"
              )}
            </Button>
          </Box>

          <Typography
            variant="body2"
            color="text.secondary"
            textAlign="center"
          >
            Take the ticket and proceed to the barrier.
          </Typography>
        </Stack>
      </Paper>
    </Stack>
  );
};