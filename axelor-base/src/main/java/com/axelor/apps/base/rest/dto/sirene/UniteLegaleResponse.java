package com.axelor.apps.base.rest.dto.sirene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UniteLegaleResponse {
  private String categorieEntreprise;
  private String categorieJuridiqueUniteLegale;
  private String nomUniteLegale;
  private String prenom1UniteLegale;
  private String sexeUniteLegale;
  private String denominationUniteLegale;

  public String getCategorieEntreprise() {
    return categorieEntreprise;
  }

  public String getCategorieJuridiqueUniteLegale() {
    return categorieJuridiqueUniteLegale;
  }

  public String getNomUniteLegale() {
    return nomUniteLegale;
  }

  public String getPrenom1UniteLegale() {
    return prenom1UniteLegale;
  }

  public String getSexeUniteLegale() {
    return sexeUniteLegale;
  }

  public String getDenominationUniteLegale() {
    return denominationUniteLegale;
  }
}
