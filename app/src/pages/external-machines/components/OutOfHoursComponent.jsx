import {Alert, Paper, Stack, Typography} from "@mui/material";
import ScheduleIcon from "@mui/icons-material/Schedule";

export const OutOfHoursComponent = () => {
  return (
    <Paper
      elevation={4}
      sx={{
        p: 2.5,
        borderRadius: 3,
      }}
    >
      <Stack spacing={2} alignItems="center">
        <ScheduleIcon sx={{fontSize: 56}}/>

        <Typography variant="h5" fontWeight={900} textAlign="center">
          Closed for the day
        </Typography>

        <Typography variant="body1" color="text.secondary" textAlign="center">
          We’ll be happy to welcome you tomorrow.
        </Typography>

        <Alert
          severity="warning"
          sx={{
            width: "100%",
            fontSize: "1rem",
            "& .MuiAlert-message": {width: "100%"},
          }}
        >
          Please come back during opening hours.
        </Alert>
      </Stack>
    </Paper>
  );
};