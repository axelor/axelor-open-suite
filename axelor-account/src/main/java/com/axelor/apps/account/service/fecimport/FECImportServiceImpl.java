package com.axelor.apps.account.service.fecimport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

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

  @Override
  public MetaFile getMetaFile(String bindMetaFile) throws IOException {
	  
	  
	  
    Path path = MetaFiles.getPath(bindMetaFile);
    if (path != null) {
      return metaFiles.upload(path.toFile());
    }

    return null;
  }
}
