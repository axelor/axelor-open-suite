package com.axelor.apps.base.rest.dto.sirene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdresseEtablissementResponse {
  private String codePostalEtablissement;
  private String complementAdresseEtablissement;
  private String distributionSpecialeEtablissement;
  private String enseigne1Etablissement;
  private String libelleCommuneEtablissement;
  private String numeroVoieEtablissement;
  private String typeVoieEtablissement;
  private String libelleVoieEtablissement;

  public String getCodePostalEtablissement() {
    return codePostalEtablissement;
  }

  public String getComplementAdresseEtablissement() {
    return complementAdresseEtablissement;
  }

  public String getDistributionSpecialeEtablissement() {
    return distributionSpecialeEtablissement;
  }

  public String getEnseigne1Etablissement() {
    return enseigne1Etablissement;
  }

  public String getLibelleCommuneEtablissement() {
    return libelleCommuneEtablissement;
  }

  public String getNumeroVoieEtablissement() {
    return numeroVoieEtablissement;
  }

  public String getTypeVoieEtablissement() {
    return typeVoieEtablissement;
  }

  public String getLibelleVoieEtablissement() {
    return libelleVoieEtablissement;
  }
}
