import {Alert, Box, Paper, Stack, Typography} from "@mui/material";
import ReportProblemIcon from "@mui/icons-material/ReportProblem";

export const ProblemScreen = ({problemMessage}) => {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        bgcolor: "error.light",
        px: 2,
      }}
    >
      <Paper
        elevation={6}
        sx={{
          p: 3,
          borderRadius: 3,
          width: "100%",
          maxWidth: 420,
          mx: "auto",
        }}
      >
        <Stack spacing={2.5} alignItems="center">
          <ReportProblemIcon
            sx={{
              fontSize: 72,
              color: "error.main",
            }}
          />

          <Typography variant="h4" fontWeight={900} textAlign="center">
            Machine issue
          </Typography>

          <Typography
            variant="body1"
            color="text.secondary"
            textAlign="center"
          >
            This parking machine is currently unavailable.
            <br/>
            Please contact the service desk.
          </Typography>

          <Alert
            severity="error"
            sx={{
              width: "100%",
              fontSize: "1rem",
              "& .MuiAlert-message": {width: "100%"},
            }}
          >
            <strong>Problem:</strong>{" "}
            {problemMessage || "Unknown problem"}
          </Alert>

          <Typography
            variant="caption"
            color="text.secondary"
            textAlign="center"
          >
            We apologise for the inconvenience.
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
};