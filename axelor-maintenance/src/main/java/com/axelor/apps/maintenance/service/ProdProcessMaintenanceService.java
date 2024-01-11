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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.apps.report.engine.ReportSettings;
import com.google.inject.Inject;

public class ProdProcessMaintenanceService extends ProdProcessService {

  @Inject
  public ProdProcessMaintenanceService(ProdProcessRepository prodProcessRepo) {
    super(prodProcessRepo);
  }

  public String print(ProdProcess prodProcess) throws AxelorException {
    return ReportFactory.createReport(
            prodProcess.getTypeSelect() == ManufOrderRepository.TYPE_MAINTENANCE
                ? IReport.MAINTENANCE_PROD_PROCESS
                : com.axelor.apps.production.report.IReport.PROD_PROCESS,
            prodProcess.getName() + "-${date}")
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam("ProdProcessId", prodProcess.getId().toString())
        .generate()
        .getFileLink();
  }
}
