/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.util.Optional;

public class FECImportServiceImpl implements FECImportService {

  protected CompanyRepository companyRepository;
  protected MetaFiles metaFiles;

  @Inject
  public FECImportServiceImpl(CompanyRepository companyRepository, MetaFiles metaFiles) {
    this.companyRepository = companyRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public Company getCompany(MetaFile dataMetaFile) {
    Company company = null;
    if (dataMetaFile != null && dataMetaFile.getFileName() != null) {
      String fileName = dataMetaFile.getFileName();
      int separatorIndex = fileName.indexOf('F');
      if (separatorIndex > 0) {
        String registrationCode = fileName.substring(0, separatorIndex);
        company =
            companyRepository
                .all()
                .filter("self.partner.registrationCode = ?", registrationCode)
                .fetchOne();
      }
    }

    if (company != null) {
      return company;
    } else if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)
        != null) {
      return Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    } else {
      return companyRepository.all().fetchOne();
    }
  }
}
