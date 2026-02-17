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

export function openAxelorView(viewName: string, id: number, model: string) {
  getAxelorScope()?.openView?.({
    title: model.split(".")?.[-1],
    action: `base.map.view.record.detail.${id}`,
    model,
    viewType: "form",
    views: [{ type: "form", name: viewName }],
    params: { forceEdit: true },
    context: { _showRecord: id },
  });
}
