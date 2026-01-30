import {useState} from "react";
import {useNavigate} from "react-router";
import {Button, Paper, Stack, TextField, Typography} from "@mui/material";

export function ScanTicketScreen() {
  const [value, setValue] = useState("");
  const navigate = useNavigate();

  return (
    <Paper elevation={6} sx={{p: 2.5, borderRadius: 4}}>
      <Stack spacing={2}>
        <Typography variant="h5" fontWeight={900}>Scan ticket</Typography>
        <Typography color="text.secondary">
          Scan the QR code (or paste the ticket UUID).
        </Typography>

        <TextField
          label="Ticket ID"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          fullWidth
        />

        <Button
          variant="contained"
          size="large"
          disabled={!value.trim()}
          onClick={() => navigate(`/exit/${value.trim()}`)}
        >
          Continue
        </Button>
      </Stack>
    </Paper>
  );
}
