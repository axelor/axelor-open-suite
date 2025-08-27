import type { MarkerPoint } from "../utils";

export const markers: MarkerPoint[] = [
  {
    latitude: 43.6109744,
    longitude: 1.4426612,
    title: "T0008 - MASSON SA",
    view: {
      title: "Partner",
      action: "crm.root.customers",
      model: "com.axelor.apps.base.db.Partner",
      viewType: "form",
      views: [{ type: "form", name: "partner-customer-form" }],
      params: { forceEdit: true },
      context: { _showRecord: 1 },
    },
    fields: {
      address: "4 BD D'ARCOLE<br/>31000 TOULOUSE<br/>FRANCE",
      phone: "05.44.40.54.99",
      email: "contact@masson.fr",
      industry: "Manufacturing",
    },
  },
  {
    latitude: 48.8546155,
    longitude: 2.3253654,
    title: "T0045 - MEYER Philippe",
    fields: {
      address: "55 RUE DE GRENELLE<br/>75001 PARIS<br/>FRANCE",
      phone: "01.52.66.34.99",
      email: "p.meyer@yahoo.com",
      industry: "Manufacturing",
    },
  },
  {
    latitude: 53.4093657,
    longitude: -2.9731887,
    title: "T0046 - MITCHELL Sandra",
    fields: {
      address: "79 LONDON ROAD<br/>LIVERPOOL\nL3 8JA<br/>ROYAUME-UNI",
      phone: "+44 151 227 8999",
      email: "s.mitchell@yahoo.com",
      industry: "Consulting",
    },
  },
  {
    latitude: 25.8516371,
    longitude: -80.179404,
    title: "T0047 - PARKER Laura",
    fields: {
      address: "846 NORTHEAST 83RD STREET<br/>MIAMI FL  33138<br/>ETATS-UNIS",
      phone: "+1 305 507 7799",
      email: "l.parker@msn.com",
      industry: "Consulting",
    },
  },
  {
    latitude: 48.5923485,
    longitude: 7.7561537,
    title: "T0044 - PICARD Anna",
    fields: {
      address: "55 BD CLEMENCEAU<br/>67000 STRASBOURG<br/>FRANCE",
      phone: "03.79.67.80.99",
      email: "a.picard@msn.com",
    },
  },
  {
    latitude: 48.8378693,
    longitude: 2.5869407,
    title: "T0048 - Axelor",
    fields: {
      address: "23 RUE ALFRED NOBEL<br/>77420 CHAMPS-SUR-MARNE<br/>FRANCE",
      phone: "01.83.64.06.50",
      email: "info@axelor.com",
    },
  },
  {
    latitude: 48.8695072,
    longitude: 2.3314533,
    title: "T0079 - Apple",
    fields: {
      address: "RUE DE LA PAIX<br/>75001 PARIS<br/>FRANCE",
      phone: " ",
    },
  },
  {
    latitude: 48.8712549,
    longitude: 2.3381824,
    title: "T0001 - APOLLO",
    fields: {
      address: "178 rue favart<br/>75000 PARIS<br/>FRANCE",
      phone: "04.35.79.88.99",
      email: "info@apollo.fr",
    },
  },
  {
    latitude: 48.8707573,
    longitude: 2.3053312,
    title: "T0080 - Samsung",
    fields: {
      address: "CHAMPS ÉLYSÉES<br/>75001 PARIS<br/>FRANCE",
      phone: " ",
    },
  },
  {
    latitude: 35.749051,
    longitude: 139.759719,
    title: "T0030 - SASAKI Components",
    fields: {
      address: "4-5-6 NISHIOGU<br/>116-0011 TOKYO<br/>JAPON",
      phone: "+81 3 5850 9999",
      email: "info@sasaki-components.com",
    },
  },
  {
    latitude: 50.8476948,
    longitude: 4.3492136,
    title: "T0032 - DE KIMPE Engineering",
    fields: {
      address: "BD ANSPACH, 98<br/>1000 BRUXELLES<br/>BELGIQUE",
      phone: "+32 2 234 99 99",
      email: "contact@dekimpe-eng.com",
    },
  },
  {
    latitude: 48.8524095,
    longitude: 2.3344497,
    title: "T0014 - 123 Services",
    fields: {
      address: "8 rue princesse<br/>75000 PARIS<br/>FRANCE",
      phone: "03.35.52.63.33",
      email: "contact@123-services.fr",
    },
  },
  {
    latitude: 44.8351294,
    longitude: -0.5854136,
    title: "T0011 - GERARD Solutions",
    fields: {
      address: "56 COURS MARECHAL JUIN<br/>33000 BORDEAUX<br/>FRANCE",
      phone: "05.22.62.48.99",
      email: "contact@gerard-solutions.fr",
    },
  },
  {
    latitude: 52.3803076,
    longitude: 4.8862665,
    title: "T0034 - DG Technologies",
    fields: {
      address: "LINDENGRACHT, 55<br/>1015 KH  AMSTERDAM<br/>PAYS-BAS",
      phone: "+31 20 530 79 99",
      email: "info@dg-technologies.com",
    },
  },
  {
    latitude: 48.783137,
    longitude: 2.4567896,
    title: "T0018 - BOURGEOIS INDUSTRIE",
    fields: {
      address: "10 RUE DES ARCHIVES<br/>94000 CRETEIL<br/>FRANCE",
      phone: "01.97.37.77.99",
      email: "info@bourgeois-industries.fr",
    },
  },
  {
    latitude: 37.783438,
    longitude: -122.43972,
    title: "T0038 - WM Cinetics",
    fields: {
      address: "2200 GEARY BVD<br/>SAN FRANCISCO CA  94115<br/>ETATS-UNIS",
      phone: "+1 415 490 2999",
      email: "contact@wm-cinetics.com",
    },
  },
  {
    latitude: 48.8648897,
    longitude: 2.3447477,
    title: "T0040 - TGM Consulting",
    fields: {
      address: "69 RUE ETIENNE MARCEL<br/>75001 PARIS<br/>FRANCE",
      phone: "01.73.66.77.99",
      email: "info@tgm-consulting.com",
    },
  },
];
