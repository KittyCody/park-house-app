import {api} from "./api-client.js";

export const ticketService = {
  createTicket: async () => {
    const {data} = await api.post("/api/v1/tickets/entries", {})
    return data;
  },

  getTicketStatus: async (ticketId) => {
    const {data} = await api.get(`/api/v1/tickets/${ticketId}/status`)
    return data;
  },

  pay: async (ticketId) => {
    const MOCK_PAYMENT = true;

    if (MOCK_PAYMENT) {
      const shouldFail = Math.random() < 0.3;

      if (shouldFail) {
        const code =
          PAYMENT_ERROR_CODES[Math.floor(Math.random() * PAYMENT_ERROR_CODES.length)];

        return {ok: false, code};
      }

      await api.post(`/api/v1/tickets/${ticketId}/pay`);
      return {ok: true};
    }
  },

  exit: async (ticketId) => {
    await api.post(`/api/v1/tickets/${ticketId}/exit`);
    return {ok: true};
  },
}

const PAYMENT_ERROR_CODES = [
  "CARD_DECLINED",
  "NETWORK_CONNECTION_ISSUE",
  "INVALID_TICKET",
  "ALREADY_PAID",
];
