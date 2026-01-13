export interface MarkerPoint {
  recordId: number;
  latitude: number;
  longitude: number;
  viewName?: string;
  cardContent: string;
}

export interface Filter {
  options: any[];
  name: string;
}

export type Position = [number, number, number?];
