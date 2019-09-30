/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmploymentContractService {

  @Inject private EmploymentContractRepository employmentContractRepo;

  @Transactional
  public int addAmendment(EmploymentContract EmploymentContract) throws AxelorException {
    String name =
        EmploymentContract.getFullName() + "_" + EmploymentContract.getEmploymentContractVersion();

    ReportFactory.createReport(IReport.EMPLYOMENT_CONTRACT, name + "-${date}")
        .addParam("ContractId", EmploymentContract.getId())
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .toAttach(EmploymentContract)
        .generate()
        .getFileLink();

    int version = EmploymentContract.getEmploymentContractVersion() + 1;
    EmploymentContract.setEmploymentContractVersion(version);
    employmentContractRepo.save(EmploymentContract);

    return version;
  }
}
