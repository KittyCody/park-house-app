import {useCallback, useEffect, useState} from "react";
import {PrintTicketComponent} from "./components/PrintTicketComponent.jsx";
import {ParkingFullComponent} from "./components/ParkingFullComponent.jsx";
import {OutOfHoursComponent} from "./components/OutOfHoursComponent.jsx";
import {parkingService} from "../../services/parking-service.js";
import {Alert, Box, Chip, Container, Paper, Stack, Typography,} from "@mui/material";

export const ExternalMachineScreen = () => {
  const [availableSpaces, setAvailableSpaces] = useState(undefined);
  const [isOpen, setIsOpen] = useState(undefined);
  const [error, setError] = useState(undefined);

  const onTicketPrinted = useCallback(() => {
    setAvailableSpaces((current) => current - 1);
  }, []);

  useEffect(() => {
    parkingService
      .getStatus()
      .then((status) => {
        setAvailableSpaces(status.availableSpaces);
        setIsOpen(status.isOperational);
      })
      .catch((err) => {
        setError("Could not fetch parking status");
        console.error(err);
      });
  }, []);

  const loading = isOpen === undefined || availableSpaces === undefined;

  const statusLabel = loading
    ? "Loading…"
    : isOpen
      ? "Open"
      : "Closed";

  const statusColor = loading
    ? "default"
    : isOpen
      ? "success"
      : "warning";

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
        <Paper
          elevation={6}
          sx={{
            p: 2.5,
            borderRadius: 4,
          }}
        >
          <Stack spacing={2.25}>
            {/* Header */}
            <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
              <Typography variant="h4" sx={{lineHeight: 1.1}}>
                Park House
              </Typography>

              <Chip
                label={statusLabel}
                color={statusColor}
                variant={loading ? "outlined" : "filled"}
                sx={{
                  fontWeight: 800,
                  fontSize: "0.95rem",
                  height: 36,
                }}
              />
            </Stack>

            <Typography variant="body1" color="text.secondary">
              Welcome! Tap below to get your ticket.
            </Typography>

            {/* Error */}
            {error && (
              <Alert severity="error" sx={{fontSize: "1rem"}}>
                {error}
              </Alert>
            )}

            {/* Content */}
            {loading ? (
              <Paper variant="outlined" sx={{p: 2, borderRadius: 3}}>
                <Typography variant="h6">Checking parking status…</Typography>
                <Typography color="text.secondary">
                  Please wait.
                </Typography>
              </Paper>
            ) : isOpen ? (
              <>
                {/* Spaces banner */}
                <Paper
                  variant="outlined"
                  sx={{
                    p: 2,
                    borderRadius: 3,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    gap: 2,
                  }}
                >
                  <Stack spacing={0.5}>
                    <Typography variant="h6">Available spaces</Typography>
                    <Typography variant="body1" color="text.secondary">
                      Real-time availability
                    </Typography>
                  </Stack>

                  <Chip
                    label={`${availableSpaces}`}
                    color={availableSpaces > 0 ? "success" : "error"}
                    sx={{
                      fontWeight: 900,
                      fontSize: "1.2rem",
                      height: 44,
                      px: 1,
                    }}
                  />
                </Paper>

                {/* Main action area */}
                <Box sx={{mt: 0.5}}>
                  {availableSpaces > 0 ? (
                    <PrintTicketComponent onPrintedTicket={onTicketPrinted}/>
                  ) : (
                    <ParkingFullComponent/>
                  )}
                </Box>
              </>
            ) : (
              <OutOfHoursComponent/>
            )}

            {/* Footer hint (small display friendly) */}
            <Typography variant="caption" color="text.secondary" sx={{textAlign: "center"}}>
              For assistance, please contact the reception.
            </Typography>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
};