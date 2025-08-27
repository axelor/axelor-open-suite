export interface MarkerPoint {
  latitude: number;
  longitude: number;
  title: string;
  view?: any;
  fields: { [key: string]: string | number };
}

export interface Filter {
  options: any[];
  name: string;
}

export type Position = [number, number, number?];
