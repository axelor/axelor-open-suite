import { restService } from "../providers";

export async function computeMapGroupData(id: number) {
  return restService.get(`ws/aos/map-group/compute/${id}`);
}
