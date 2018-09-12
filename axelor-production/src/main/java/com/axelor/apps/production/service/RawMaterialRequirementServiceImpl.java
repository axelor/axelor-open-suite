/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.production.db.RawMaterialRequirement;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;

public class RawMaterialRequirementServiceImpl implements RawMaterialRequirementService {

  /** The title of the report. */
  public static final String RAW_MATERIAL_REPORT_TITLE = "Raw material requirement";

  @Override
  public String print(RawMaterialRequirement rawMaterialRequirement) throws AxelorException {
    String name = String.format("%s - ${date}", I18n.get(RAW_MATERIAL_REPORT_TITLE));
    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.RAW_MATERIAL_REQUIREMENT, name);

    String locale = ReportSettings.getPrintingLocale(null);
    return reportSetting
        .addParam("RawMaterialRequirementId", rawMaterialRequirement.getId())
        .addParam("Locale", locale)
        .generate()
        .getFileLink();
  }
}
