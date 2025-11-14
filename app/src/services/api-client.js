import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const accessToken = import.meta.env.VITE_ACCESS_TOKEN
  config.headers.Authorization = `Bearer ${accessToken}`

  return config
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error(error)
    }

    return Promise.reject(error)
  }
);