import {api} from "./api-client.js";

export const parkingService = {
  getStatus: async () => {
    const {data} = await api.get("api/v1/status")
    return data;
  }
}