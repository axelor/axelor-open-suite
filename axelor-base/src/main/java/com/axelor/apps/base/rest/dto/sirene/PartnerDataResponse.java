package com.axelor.apps.base.rest.dto.sirene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerDataResponse {
  private String siret;
  private UniteLegaleResponse uniteLegale;
  private AdresseEtablissementResponse adresseEtablissement;

  public String getSiret() {
    return siret;
  }

  public UniteLegaleResponse getUniteLegale() {
    return uniteLegale;
  }

  public AdresseEtablissementResponse getAdresseEtablissement() {
    return adresseEtablissement;
  }
}
