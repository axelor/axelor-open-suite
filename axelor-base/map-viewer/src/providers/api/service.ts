import axios, { type AxiosInstance } from "axios";
import { readCookie } from "../../utils";

export class Service {
  private baseURL: string;
  private headers: any;
  private axiosInstance: AxiosInstance;

  constructor() {
    this.baseURL = import.meta.env.PROD
      ? "./../../"
      : import.meta.env.VITE_PROXY_CONTEXT;

    this.headers = {
      Accept: "application/json",
      "Content-Type": "application/json",
      "X-Requested-With": "XMLHttpRequest",
      "X-CSRF-Token": readCookie("CSRF-TOKEN"),
    };

    this.axiosInstance = axios.create({
      baseURL: this.baseURL,
      headers: this.headers,
      withCredentials: true,
    });

    this.axiosInstance.interceptors.request.use((config: any) => {
      config.url = config.url.replace(/([^:])\/\/+/g, "$1/");
      return config;
    });
  }

  async fetch(url: string, method: string, options: any) {
    return this.axiosInstance({ url, method, ...options })
      .then((response) => response.data)
      .catch((err) => {
        console.error("API Error:", err);
        throw err;
      });
  }

  request(url: string, config: any = {}, data: any = {}) {
    const options = {
      ...config,
      data: config.method === "GET" ? undefined : data,
    };
    return this.fetch(url, config.method, options);
  }

  get(url: string) {
    return this.request(url, { method: "GET" });
  }

  post(url: string, data: any) {
    return this.request(url, { method: "POST" }, data);
  }

  put(url: string, data: any) {
    return this.request(url, { method: "PUT" }, data);
  }

  delete(url: string) {
    return this.request(url, { method: "DELETE" });
  }

  createRecord(entity: string, data: any) {
    return this.put(`ws/rest/${entity}`, { data });
  }

  updateRecord(entity: string, id: number, data: any) {
    return this.post(`ws/rest/${entity}/${id}`, { data });
  }

  fetchRecord(entity: string, id: number, data: any = {}) {
    return this.post(`ws/rest/${entity}/${id}/fetch`, data);
  }

  searchRecords(entity: string, options: any) {
    return this.post(`ws/rest/${entity}/search`, { offset: 0, ...options });
  }

  action(data: any) {
    return this.post(`ws/action`, data);
  }
}

export const restService = new Service();
