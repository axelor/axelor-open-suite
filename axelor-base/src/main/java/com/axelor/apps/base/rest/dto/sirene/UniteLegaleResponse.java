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
public class UniteLegaleResponse {
  private String categorieEntreprise;
  private String categorieJuridiqueUniteLegale;
  private String nomUniteLegale;
  private String prenom1UniteLegale;
  private String sexeUniteLegale;
  private String denominationUniteLegale;
  private String trancheEffectifsUniteLegale;
  private String activitePrincipaleUniteLegale;

  public String getActivitePrincipaleUniteLegale() {
    return activitePrincipaleUniteLegale;
  }

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

  public String getTrancheEffectifsUniteLegale() {
    return trancheEffectifsUniteLegale;
  }
}
