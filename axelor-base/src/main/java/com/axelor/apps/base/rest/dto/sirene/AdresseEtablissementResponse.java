/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
