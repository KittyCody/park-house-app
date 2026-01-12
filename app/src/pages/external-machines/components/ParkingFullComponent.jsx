import {Alert, Paper, Stack, Typography} from "@mui/material";
import DoNotDisturbOnIcon from "@mui/icons-material/DoNotDisturbOn";

export const ParkingFullComponent = () => {
  return (
    <Paper
      elevation={4}
      sx={{
        p: 2.5,
        borderRadius: 3,
      }}
    >
      <Stack spacing={2} alignItems="center">
        <DoNotDisturbOnIcon sx={{fontSize: 56}}/>

        <Typography variant="h5" fontWeight={900} textAlign="center">
          No spaces available
        </Typography>

        <Typography variant="body1" color="text.secondary" textAlign="center">
          We’re sorry — our parking is currently full.
          <br/>
          Please try again later.
        </Typography>

        <Alert
          severity="info"
          sx={{
            width: "100%",
            fontSize: "1rem",
            "& .MuiAlert-message": {width: "100%"},
          }}
        >
          If you are already inside, please proceed to the exit.
        </Alert>
      </Stack>
    </Paper>
  );
};