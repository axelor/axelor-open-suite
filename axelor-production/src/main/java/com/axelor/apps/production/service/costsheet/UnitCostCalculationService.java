/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;

public interface UnitCostCalculationService {

  public MetaFile exportUnitCostCalc(UnitCostCalculation unitCostCalculation, String fileName)
      throws IOException;

  public void importUnitCostCalc(MetaFile dataFile, UnitCostCalculation unitCostCalculation)
      throws IOException;

  public void runUnitCostCalc(UnitCostCalculation unitCostCalculation) throws AxelorException;

  public void updateUnitCosts(UnitCostCalculation unitCostCalculation) throws AxelorException;

  public String createProductSetDomain(UnitCostCalculation unitCostCalculation, Company company)
      throws AxelorException;

  public void fillCompanySet(UnitCostCalculation unitCostCalculation, Company company);

  public Boolean hasDefaultBOMSelected();

  public Company getSingleCompany(UnitCostCalculation unitCostCalculation);
}
