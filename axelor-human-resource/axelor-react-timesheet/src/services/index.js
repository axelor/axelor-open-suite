import axios from "axios";

let lastCookieString;
let lastCookies = {};

function readCookie(name) {
  let cookieString = document.cookie || "";
  if (cookieString !== lastCookieString) {
    lastCookieString = cookieString;
    lastCookies = cookieString.split("; ").reduce((obj, value) => {
      let parts = value.split("=");
      obj[parts[0]] = parts[1] || "";
      return obj;
    }, {});
  }
  return lastCookies[name];
}

export function getHeaders(file, offset) {
  const attachment = file.file;
  if (!attachment) {
    return;
  }
  const headers = {
    "X-File-Name": attachment.name,
    "X-File-Offset": offset,
    "X-File-Size": attachment.size,
    "X-File-Type": attachment.type,
    "X-CSRF-Token": readCookie("CSRF-TOKEN"),
  };
  if (file.id) {
    headers["X-File-Id"] = file.id;
  }
  return headers;
}

export class Service {
  constructor() {
    this.baseURL = import.meta.env.PROD
      ? "../../"
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

    // Ensure no duplicate slashes in URLs
    this.axiosInstance.interceptors.request.use((config) => {
      config.url = config.url.replace(/([^:])\/\/+/g, "$1/"); // Removes duplicate slashes
      return config;
    });
  }

  fetch(url, method, options) {
    return this.axiosInstance({
      url,
      method,
      ...options,
    })
      .then((response) => response.data)
      .catch((err) => {
        console.error("API Error:", err);
        throw err;
      });
  }

  request(url, config = {}, data = {}) {
    const options = {
      ...config,
      data: config.method === "GET" ? undefined : data,
    };
    return this.fetch(url, config.method, options);
  }

  get(url) {
    return this.request(url, { method: "GET" });
  }

  post(url, data) {
    return this.request(url, { method: "POST" }, data);
  }

  put(url, data) {
    return this.request(url, { method: "PUT" }, data);
  }

  delete(url, data) {
    return this.request(url, { method: "DELETE" }, data);
  }

  add(entity, record) {
    return this.post(`ws/rest/${entity}`, { data: record });
  }

  fetchId(entity, id, data = {}) {
    return this.post(`ws/rest/${entity}/${id}/fetch`, data);
  }

  search(entity, options) {
    return this.post(`ws/rest/${entity}/search`, { offset: 0, ...options });
  }

  action(data) {
    return this.post(`ws/action`, data);
  }
}

export default new Service();
