package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.util.Optional;

public class FECImportServiceImpl implements FECImportService {

  protected CompanyRepository companyRepository;

  @Inject
  public FECImportServiceImpl(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
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
