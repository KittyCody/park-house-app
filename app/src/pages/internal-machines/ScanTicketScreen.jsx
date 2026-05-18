import {useEffect, useRef} from "react";
import {useNavigate} from "react-router";
import {Box, Paper, Stack, Typography} from "@mui/material";
import {Html5QrcodeScanner} from "html5-qrcode";

export function ScanTicketScreen() {
  const navigate = useNavigate();
  const scannerRef = useRef(null);

  useEffect(() => {
    const scanner = new Html5QrcodeScanner(
      "qr-reader",
      {fps: 10, qrbox: {width: 250, height: 250}},
      false
    );

    scanner.render(
      (decodedText) => {
        scanner.clear();
        navigate(`/exit/${decodedText.trim()}`);
      },
      () => {}
    );

    scannerRef.current = scanner;

    return () => {
      scannerRef.current?.clear().catch(() => {});
    };
  }, [navigate]);

  return (
    <Paper elevation={6} sx={{p: 2.5, borderRadius: 4}}>
      <Stack spacing={2}>
        <Typography variant="h5" fontWeight={900}>Scan ticket</Typography>
        <Typography color="text.secondary">
          Point the camera at the QR code on your ticket.
        </Typography>
        <Box id="qr-reader"/>
      </Stack>
    </Paper>
  );
}
