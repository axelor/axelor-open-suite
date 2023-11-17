/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Department;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EmploymentContractService {

  @Inject private EmploymentContractRepository employmentContractRepo;

  @Transactional(rollbackOn = {Exception.class})
  public int addAmendment(EmploymentContract employmentContract) throws AxelorException {

    int version = employmentContract.getEmploymentContractVersion() + 1;
    employmentContract.setEmploymentContractVersion(version);
    employmentContractRepo.save(employmentContract);

    return version;
  }

  public void exportEmploymentContract(EmploymentContract employmentContract) throws IOException {
    List<String[]> list = new ArrayList<>();

    this.employmentContractExportSilae(employmentContract, list);

    String fileName = this.employmentContractExportName();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    String[] headers = employmentContractExportHeaders();

    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), employmentContract);
    }
  }

  public void employmentContractExportSilae(
      EmploymentContract employmentContract, List<String[]> list) {
    String[] item = new String[21];

    item[0] = employmentContract.getId().toString();
    item[1] =
        employmentContract.getStartDate() == null
            ? ""
            : employmentContract.getStartDate().toString();
    item[2] =
        employmentContract.getEndDate() == null ? "" : employmentContract.getEndDate().toString();

    Employee employee = employmentContract.getEmployee();
    item[4] = employee.getMaritalName();

    Partner contactPartner = employee.getContactPartner();
    if (contactPartner != null) {
      item[3] = contactPartner.getName();
      item[5] = contactPartner.getFirstName();

      Address mainAddress = contactPartner.getMainAddress();
      if (mainAddress != null) {
        item[6] = mainAddress.getAddressL4();
        item[7] = mainAddress.getAddressL2();
        item[8] = mainAddress.getZip();
        if (mainAddress.getCity() != null) {
          item[9] = mainAddress.getCity().getName();
        } else {
          item[9] = "";
        }
      }

      item[10] = contactPartner.getMobilePhone();
      item[11] = contactPartner.getFixedPhone();
      item[12] =
          contactPartner.getEmailAddress() == null
              ? ""
              : contactPartner.getEmailAddress().getAddress();
    }

    item[13] = employee.getBirthDate() == null ? "" : employee.getBirthDate().toString();

    Department birthDepartment = employee.getDepartmentOfBirth();
    if (birthDepartment != null) {
      item[14] = birthDepartment.getName();
      item[16] = !birthDepartment.getCode().equals("99") ? "FR - FRANCE" : "Oui";
    }

    item[15] = employee.getCityOfBirth() == null ? "" : employee.getCityOfBirth().getName();
    item[17] = employee.getSocialSecurityNumber();
    item[18] = employmentContract.getPayCompany().getName();
    item[19] =
        employmentContract.getContractType() == null
            ? ""
            : employmentContract.getContractType().getId().toString();
    item[20] = employee.getMaidenName();

    list.add(item);
  }

  public String employmentContractExportName() {
    return I18n.get("Employment contract")
        + " - "
        + Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime().toString();
  }

  public String[] employmentContractExportHeaders() {
    String[] headers = {
      I18n.get("MATRICULE"),
      I18n.get("DATE D'ENTREE"),
      I18n.get("DATE DE SORTIE"),
      I18n.get("NOM"),
      I18n.get("NOM MARITAL"),
      I18n.get("PRENOM"),
      I18n.get("VOIE"),
      I18n.get("COMPLEMENT"),
      I18n.get("CP"),
      I18n.get("VILLE"),
      I18n.get("TEL"),
      I18n.get("TEL 2"),
      I18n.get("E-MAIL"),
      I18n.get("DATE NAISS"),
      I18n.get("DEPT NAISS"),
      I18n.get("LIEU"),
      I18n.get("ETRANGER"),
      I18n.get("NUMERO INSEE"),
      I18n.get("ETAB"),
      I18n.get("CDD"),
      I18n.get("NOM DE JEUNE FILLE")
    };

    return headers;
  }
}
