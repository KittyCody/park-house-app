import {Box, Divider, Paper, Stack, Typography} from "@mui/material";
import {QRCodeSVG} from "qrcode.react";

// helper to format time nicely (optional but recommended)
const formatTime = (isoDate) =>
  new Date(isoDate).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

export const TicketComponent = ({ticket}) => {
  return (
    <Paper
      elevation={2}
      sx={{
        p: 2,
        borderRadius: 2,
        bgcolor: "background.paper",
        border: "2px dashed",
        borderColor: "grey.400",
        maxWidth: 280,
        mx: "auto",
      }}
    >
      <Stack spacing={1.25}>
        {/* Header */}
        <Typography
          variant="subtitle1"
          fontWeight={900}
          textAlign="center"
          letterSpacing={1}
        >
          PARK HOUSE
        </Typography>

        <Divider/>

        {/* QR Code Section */}
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: 1.5,
            py: 1,
          }}
        >
          <QRCodeSVG
            value={ticket.id.toString()}
            size={140}
            level="M"
            includeMargin={true}
          />
        </Box>

        <Divider/>

        {/* Ticket info */}
        <Stack spacing={0.75}>
          <Row label="Entry time" value={formatTime(ticket.timeOfEntry)}/>
          <Row label="Ticket ID" value={ticket.id}/>
        </Stack>

        <Divider/>

        {/* Footer */}
        <Typography
          variant="caption"
          color="text.secondary"
          textAlign="center"
        >
          Keep this ticket with you
          <br/>
          Present it at the exit
        </Typography>
      </Stack>
    </Paper>
  );
};

/* Small helper for aligned rows */
const Row = ({label, value}) => (
  <Stack direction="row" justifyContent="space-between">
    <Typography variant="body2" fontWeight={600}>
      {label}
    </Typography>
    <Typography variant="body2">{value}</Typography>
  </Stack>
);