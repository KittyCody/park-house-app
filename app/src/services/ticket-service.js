import {api} from "./api-client.js";

export const ticketService = {
  getAvailableSpaces: async () => {
    const {data} = await api.get("/status")

    return data.availableSpaces;
  },

  createTicket: async () => {
    const {data} = await api.post("/tickets", {})
    return data;
  }
}