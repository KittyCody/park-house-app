import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {AuthProvider} from "./providers/AuthContext.jsx";
import {createTheme} from '@mui/material/styles';
import {CssBaseline, ThemeProvider} from "@mui/material";

export const kioskTheme = createTheme({
  typography: {
    fontFamily: "Roboto, system-ui, -apple-system, Segoe UI, Arial, sans-serif",
    h4: {fontWeight: 800, letterSpacing: 0.2},
    h6: {fontWeight: 700},
    body1: {fontSize: "1.05rem"},
  },
  shape: {borderRadius: 16},
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          minHeight: 56,          // good for touch
          borderRadius: 16,
          fontWeight: 700,
          textTransform: "none",
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 20,
        },
      },
    },
  },
});

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <ThemeProvider theme={kioskTheme}>
        <CssBaseline/>

        <App/>

      </ThemeProvider>
    </AuthProvider>
  </StrictMode>,
)
