import {useEffect, useMemo, useState} from "react";
import {Alert, Box, Chip, Container, Paper, Stack, Typography} from "@mui/material";
import {ticketService} from "../../services/ticket-service.js";
import {
  InvalidTicketScreen,
  PaymentAcceptedScreen,
  PaymentFailedScreen,
  PaymentRequiredScreen,
} from "./components/InternalMachineStates.jsx";
import {useParams} from "react-router";

export const InternalMachineScreen = () => {
  const {ticketId} = useParams(); // route example: /exit/:ticketId

  // machine states
  // "LOADING" | "PAYMENT_REQUIRED" | "PAYMENT_FAILED" | "INVALID" | "ACCEPTED"
  const [state, setState] = useState("LOADING");
  const [error, setError] = useState(undefined);

  // data
  const [entryTime, setEntryTime] = useState(undefined);
  const [durationMinutes, setDurationMinutes] = useState(undefined);
  const [amountCents, setAmountCents] = useState(undefined);
  const [invalidReason, setInvalidReason] = useState(undefined);
  const [paymentFailReason, setPaymentFailReason] = useState(undefined);
  const [alreadyPaid, setAlreadyPaid] = useState(false);

  const titleChip = useMemo(() => {
    const map = {
      LOADING: {label: "Loading…", color: "default", variant: "outlined"},
      PAYMENT_REQUIRED: {label: "Payment", color: "primary", variant: "filled"},
      PAYMENT_FAILED: {label: "Error", color: "error", variant: "filled"},
      INVALID: {label: "Invalid", color: "warning", variant: "filled"},
      ACCEPTED: {label: "Exit", color: "success", variant: "filled"},
    };
    return map[state] ?? map.LOADING;
  }, [state]);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        setError(undefined);
        setState("LOADING");

        const res = await ticketService.getTicketStatus(ticketId);

        if (cancelled) return;

        if (res.status === "INVALID") {
          setInvalidReason(res.reason);
          setState("INVALID");
          return;
        }

        setEntryTime(res.ticket?.entryTime);
        setDurationMinutes(res.parking?.durationMinutes);
        setAmountCents(res.parking?.amountCents);

        if (res.status === "PAID") {
          setAlreadyPaid(true);
          setState("ACCEPTED");
          // simulate lifting barrier delay
          return;
        }

        if (res.status === "PAYMENT_REQUIRED") {
          setAlreadyPaid(false);
          setState("PAYMENT_REQUIRED");

          // Mock: auto-payment attempt (as if user taps a card)
          // In real life: you’d trigger this when the NFC reader says “card present”.
          const payment = await ticketService.pay(ticketId, res.parking.amountCents, {
            forceFail: !!res.failPayment,
          });

          if (cancelled) return;

          if (!payment.ok) {
            setPaymentFailReason(payment.code);
            setState("PAYMENT_FAILED");
            return;
          }

          setState("ACCEPTED");
          return;
        }

        // fallback
        setError("Unknown kiosk state returned by service.");
        setState("PAYMENT_FAILED");
      } catch (e) {
        if (cancelled) return;
        console.error(e);
        setError("Could not fetch exit status");
        setState("PAYMENT_FAILED");
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [ticketId]);

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        bgcolor: "background.default",
        py: 2,
      }}
    >
      <Container maxWidth="sm" sx={{px: 2}}>
        <Paper elevation={6} sx={{p: 2.5, borderRadius: 4}}>
          <Stack spacing={2.25}>
            {/* Header */}
            <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
              <Typography variant="h4" sx={{lineHeight: 1.1}}>
                Park House
              </Typography>

              <Chip
                label={titleChip.label}
                color={titleChip.color}
                variant={titleChip.variant}
                sx={{fontWeight: 900, fontSize: "0.95rem", height: 36}}
              />
            </Stack>

            {/* Generic error (network, etc.) */}
            {error && (
              <Alert severity="error" sx={{fontSize: "1rem"}}>
                {error}
              </Alert>
            )}

            {/* State body */}
            {state === "LOADING" && (
              <Paper variant="outlined" sx={{p: 2, borderRadius: 3}}>
                <Typography variant="h6">Checking ticket…</Typography>
                <Typography color="text.secondary">
                  Please wait.
                </Typography>
              </Paper>
            )}

            {state === "PAYMENT_REQUIRED" && (
              <PaymentRequiredScreen
                entryTime={entryTime}
                durationMinutes={durationMinutes}
                amountCents={amountCents}
              />
            )}

            {state === "PAYMENT_FAILED" && (
              <PaymentFailedScreen message={paymentFailReason}/>
            )}

            {state === "INVALID" && (
              <InvalidTicketScreen reason={invalidReason}/>
            )}

            {state === "ACCEPTED" && (
              <PaymentAcceptedScreen alreadyPaid={alreadyPaid}/>
            )}

            <Typography variant="caption" color="text.secondary" sx={{textAlign: "center"}}>
              Ticket ID: {ticketId || "—"}
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
};
