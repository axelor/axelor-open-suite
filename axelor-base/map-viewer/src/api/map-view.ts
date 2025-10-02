import { restService } from "../providers";

export async function fetchMapView(id: number) {
  return restService.fetchRecord("com.axelor.apps.base.db.MapView", id, {
    fields: ["mapGroupList"],
  });
}
