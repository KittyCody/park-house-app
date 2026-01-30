import {Alert, Box, Chip, Paper, Stack, Typography} from "@mui/material";
import CreditCardIcon from "@mui/icons-material/CreditCard";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import BlockIcon from "@mui/icons-material/Block";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

const ERROR_MESSAGES = {
  CARD_DECLINED: "Card declined. Please try another card.",
  NETWORK_CONNECTION_ISSUE: "Network error. Please try again.",
  INVALID_TICKET: "Invalid or unreadable ticket.",
  ALREADY_PAID: "This ticket has already been paid.",
  DEFAULT: "Payment failed. Please contact assistance.",
};

const formatMoney = (cents) =>
  new Intl.NumberFormat(undefined, {style: "currency", currency: "EUR"}).format((cents ?? 0) / 100);

const formatDuration = (minutes) => {
  const m = Math.max(0, Math.floor(minutes ?? 0));
  const h = Math.floor(m / 60);
  const mm = m % 60;
  if (h <= 0) return `${mm} min`;
  return `${h} h ${String(mm).padStart(2, "0")} min`;
};

const formatTime = (iso) => {
  try {
    return new Date(iso).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
  } catch {
    return String(iso ?? "");
  }
};

const Row = ({label, value}) => (
  <Stack direction="row" justifyContent="space-between" sx={{width: "100%"}}>
    <Typography variant="body2" fontWeight={700}>
      {label}
    </Typography>
    <Typography variant="body2">{value}</Typography>
  </Stack>
);

/** 1) Ticket needs payment */
export const PaymentRequiredScreen = ({entryTime, durationMinutes, amountCents}) => {
  return (
    <Paper elevation={4} sx={{p: 2.5, borderRadius: 3}}>
      <Stack spacing={2} alignItems="center">
        <CreditCardIcon sx={{fontSize: 60}}/>
        <Typography variant="h5" fontWeight={900} textAlign="center">
          Payment required
        </Typography>

        <Paper
          variant="outlined"
          sx={{p: 2, borderRadius: 3, width: "100%"}}
        >
          <Stack spacing={1.25}>
            <Row label="Entry time" value={formatTime(entryTime)}/>
            <Row label="Duration" value={formatDuration(durationMinutes)}/>
            <Box sx={{display: "flex", justifyContent: "space-between", alignItems: "center"}}>
              <Typography variant="body2" fontWeight={900}>
                Amount
              </Typography>
              <Chip
                label={formatMoney(amountCents)}
                color="primary"
                sx={{fontWeight: 900, fontSize: "1rem", height: 40}}
              />
            </Box>
          </Stack>
        </Paper>

        <Alert
          severity="info"
          sx={{width: "100%", fontSize: "1rem", "& .MuiAlert-message": {width: "100%"}}}
        >
          Please tap your credit card on the reader to pay.
        </Alert>
      </Stack>
    </Paper>
  );
};

/** 2) Payment failed */
export const PaymentFailedScreen = ({message}) => {
  const text = ERROR_MESSAGES[message] ?? ERROR_MESSAGES.DEFAULT;

  return (
    <Paper elevation={4} sx={{p: 2.5, borderRadius: 3}}>
      <Stack spacing={2} alignItems="center">
        <ErrorOutlineIcon sx={{fontSize: 60}}/>
        <Typography variant="h5" fontWeight={900} textAlign="center">
          Payment failed
        </Typography>

        <Typography variant="body1" color="text.secondary" textAlign="center">
          Your payment could not be processed.
          <br/>
          Please try again or use another card.
        </Typography>

        <Alert
          severity="error"
          sx={{width: "100%", fontSize: "1rem", "& .MuiAlert-message": {width: "100%"}}}
        >
          <strong>Reason:</strong> {text}
        </Alert>
      </Stack>
    </Paper>
  );
};

/** 3) Invalid ticket */
export const InvalidTicketScreen = ({reason}) => {
  return (
    <Paper elevation={4} sx={{p: 2.5, borderRadius: 3}}>
      <Stack spacing={2} alignItems="center">
        <BlockIcon sx={{fontSize: 60}}/>
        <Typography variant="h5" fontWeight={900} textAlign="center">
          Invalid ticket
        </Typography>

        <Alert
          severity="warning"
          sx={{width: "100%", fontSize: "1rem", "& .MuiAlert-message": {width: "100%"}}}
        >
          <strong>Reason:</strong> {reason || "Unknown reason."}
        </Alert>

        <Typography variant="body2" color="text.secondary" textAlign="center">
          Please contact the service desk if you believe this is a mistake.
        </Typography>
      </Stack>
    </Paper>
  );
};

/** 4) Payment accepted OR already paid */
export const PaymentAcceptedScreen = ({alreadyPaid}) => {
  return (
    <Paper elevation={4} sx={{p: 2.5, borderRadius: 3, bgcolor: "success.light"}}>
      <Stack spacing={2} alignItems="center">
        <CheckCircleIcon sx={{fontSize: 64, color: "success.main"}}/>
        <Typography variant="h5" fontWeight={900} textAlign="center">
          {alreadyPaid ? "Ticket already paid" : "Payment accepted"}
        </Typography>

        <Typography variant="body1" textAlign="center">
          Lifting barrier…
        </Typography>

        <Alert
          severity="success"
          sx={{width: "100%", fontSize: "1rem", "& .MuiAlert-message": {width: "100%"}}}
        >
          You may exit safely. Thank you!
        </Alert>
      </Stack>
    </Paper>
  );
};