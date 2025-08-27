import type { Position } from "./types";

export function readCookie(name: string) {
  const cookieString = document.cookie || "";

  const cookies = cookieString.split("; ").reduce((obj: any, value: string) => {
    const parts = value.split("=");
    obj[parts[0]] = parts[1] || "";
    return obj;
  }, {});

  return cookies[name];
}

export function getUserGeolocalisation(setPosition: (pos: Position) => void) {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (location) => {
        const { latitude, longitude } = location.coords;
        setPosition([latitude, longitude]);
      },
      (error) => {
        console.error("Geolocalisation error", error);
      }
    );
  }
}

function getAxelorScope() {
  return (window?.top?.parent as any)?.axelor;
}

export function openAxelorView(view: {
  title: string;
  action: string;
  model: string;
  viewType: string;
  views: any[];
  params?: any;
  context?: any;
}) {
  getAxelorScope()?.openView?.(view);
}
