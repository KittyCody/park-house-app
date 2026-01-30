import {useEffect, useMemo, useState} from "react";
import {Alert, Box, Button, Chip, Container, Paper, Stack, TextField, Typography} from "@mui/material";
import {useNavigate, useParams} from "react-router";
import {ticketService} from "../../services/ticket-service.js";
import {
  InvalidTicketScreen,
  PaymentAcceptedScreen,
  PaymentFailedScreen,
  PaymentRequiredScreen,
} from "./components/InternalMachineStates.jsx";

const isUuid = (s) =>
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(String(s ?? ""));

const CHIP = {
  LOADING: {label: "Loading…", color: "default", variant: "outlined"},
  PAYMENT_REQUIRED: {label: "Payment", color: "primary", variant: "filled"},
  PAID: {label: "Paid", color: "success", variant: "filled"},
  PAYMENT_FAILED: {label: "Error", color: "error", variant: "filled"},
  INVALID: {label: "Invalid", color: "warning", variant: "filled"},
  ACCEPTED: {label: "Exit", color: "success", variant: "filled"},
};

export const InternalMachineScreen = () => {
  const {ticketId} = useParams();
  const navigate = useNavigate();

  const [state, setState] = useState("LOADING");
  const [error, setError] = useState(undefined);

  const [ticket, setTicket] = useState(undefined);
  const [invalidReason, setInvalidReason] = useState(undefined);
  const [paymentFailReason, setPaymentFailReason] = useState(undefined);

  const [busy, setBusy] = useState(false);

  const [scanValue, setScanValue] = useState("");

  const titleChip = useMemo(() => CHIP[state] ?? CHIP.LOADING, [state]);

  const resetUi = () => {
    setError(undefined);
    setPaymentFailReason(undefined);
    setInvalidReason(undefined);
    setTicket(undefined);
    setBusy(false);
  };

  const loadStatus = async (id) => {
    resetUi();
    setState("LOADING");

    if (!id || !isUuid(id)) {
      setInvalidReason("INVALID_TICKET_ID");
      setState("INVALID");
      return;
    }

    try {
      const data = await ticketService.getTicketStatus(id);
      setTicket(data);

      if (data.status === "INVALID") {
        setInvalidReason(data.reason);
        setState("INVALID");
        return;
      }

      if (data.status === "PAYMENT_REQUIRED") {
        setState("PAYMENT_REQUIRED");
        return;
      }

      if (data.status === "PAID") {
        // paid but not necessarily exited -> just show "Open barrier"
        setState("PAID");
        return;
      }

      setError("Unknown kiosk state returned by service.");
      setState("PAYMENT_FAILED");
    } catch (e) {
      const status = e?.response?.status;
      setError(status ? `Could not fetch exit status (HTTP ${status})` : "Could not fetch exit status");
      setState("PAYMENT_FAILED");
    }
  };

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      if (cancelled) return;
      await loadStatus(ticketId);
      setScanValue(ticketId ?? "");
    };

    run();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ticketId]);

  const onScan = () => {
    const value = scanValue.trim();
    if (!value) return;
    navigate(`/exit/${value}`);
  };

  const onPay = async () => {
    if (!ticketId || !isUuid(ticketId)) return;

    setBusy(true);
    setPaymentFailReason(undefined);

    try {
      const res = await ticketService.pay(ticketId);

      if (!res?.ok) {
        setPaymentFailReason(res?.code);
        setState("PAYMENT_FAILED");
        return;
      }

      await ticketService.exit(ticketId);
      await loadStatus(ticketId);

      setState("ACCEPTED");
    } catch (e) {
      const status = e?.response?.status;
      setPaymentFailReason(status ? `HTTP_${status}` : "UNEXPECTED_ERROR");
      setState("PAYMENT_FAILED");
    } finally {
      setBusy(false);
    }
  };

  const onExit = async () => {
    if (!ticketId || !isUuid(ticketId)) return;

    setBusy(true);
    setPaymentFailReason(undefined);

    try {
      await ticketService.exit(ticketId);
      await loadStatus(ticketId);
      setState("ACCEPTED");
    } catch (e) {
      const status = e?.response?.status;
      setPaymentFailReason(status ? `HTTP_${status}` : "UNEXPECTED_ERROR");
      setState("PAYMENT_FAILED");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Box sx={{minHeight: "100vh", display: "flex", alignItems: "center", bgcolor: "background.default", py: 2}}>
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

            {/* Dev scan */}
            <Paper variant="outlined" sx={{p: 2, borderRadius: 3}}>
              <Stack spacing={1.25}>
                <Typography variant="body2" fontWeight={800}>
                  Scan ticket (dev)
                </Typography>

                <Stack direction="row" spacing={1}>
                  <TextField
                    size="small"
                    fullWidth
                    placeholder="Paste ticket UUID…"
                    value={scanValue}
                    onChange={(e) => setScanValue(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && onScan()}
                  />
                  <Button variant="contained" onClick={onScan} disabled={!scanValue.trim()}>
                    Go
                  </Button>
                </Stack>
              </Stack>
            </Paper>

            {/* Error */}
            {error && (
              <Alert severity="error" sx={{fontSize: "1rem"}}>
                {error}
              </Alert>
            )}

            {/* Body */}
            {state === "LOADING" && (
              <Paper variant="outlined" sx={{p: 2, borderRadius: 3}}>
                <Typography variant="h6">Checking ticket…</Typography>
                <Typography color="text.secondary">Please wait.</Typography>
              </Paper>
            )}

            {state === "PAYMENT_REQUIRED" && ticket && (
              <Stack spacing={1.5}>
                <PaymentRequiredScreen
                  entryTime={ticket.entryTime}
                  durationMinutes={ticket.durationMinutes}
                  amountCents={ticket.amountCents}
                />

                <Button
                  size="large"
                  variant="contained"
                  onClick={onPay}
                  disabled={busy}
                  sx={{minHeight: 56, fontWeight: 900}}
                >
                  {busy ? "Processing…" : "Pay & open barrier"}
                </Button>
              </Stack>
            )}

            {state === "PAID" && (
              <Stack spacing={1.5}>
                <Alert severity="success">Ticket already paid.</Alert>

                <Button
                  size="large"
                  variant="contained"
                  onClick={onExit}
                  disabled={busy}
                  sx={{minHeight: 56, fontWeight: 900}}
                >
                  {busy ? "Opening…" : "Open barrier"}
                </Button>
              </Stack>
            )}

            {state === "PAYMENT_FAILED" && <PaymentFailedScreen message={paymentFailReason}/>}

            {state === "INVALID" && <InvalidTicketScreen reason={invalidReason}/>}

            {state === "ACCEPTED" && <PaymentAcceptedScreen alreadyPaid={false}/>}

            <Typography variant="caption" color="text.secondary" sx={{textAlign: "center"}}>
              Ticket ID: {ticketId || "—"}
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
};
