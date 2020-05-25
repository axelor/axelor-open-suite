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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.exception.AxelorException;

public class MaintenanceBillOfMaterialService extends BillOfMaterialServiceImpl {

  @Override
  public String computeName(BillOfMaterial bom) {

    return bom.getProduct() != null
        ? super.computeName(bom)
        : bom.getMachineType().getName() + "-" + bom.getId();
  }

  @Override
  public String getReportLink(
      BillOfMaterial billOfMaterial, String name, String language, String format)
      throws AxelorException {

    String reportLink;

    if (billOfMaterial.getTypeSelect() == BillOfMaterialRepository.TYPE_PRODUCTION) {
      reportLink = super.getReportLink(billOfMaterial, name, language, format);
    } else {
      reportLink =
          ReportFactory.createReport(IReport.MAINTENANCE_BILL_OF_MATERIAL, name + "-${date}")
              .addParam("Locale", language)
              .addParam("BillOfMaterialId", billOfMaterial.getId())
              .addFormat(format)
              .generate()
              .getFileLink();
    }

    return reportLink;
  }
}
