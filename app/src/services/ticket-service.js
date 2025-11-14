import {api} from "./api-client.js";

export const ticketService = {
  createTicket: async () => {
    const {data} = await api.post("/api/v1/tickets/entries", {})
    return data;
  }
}