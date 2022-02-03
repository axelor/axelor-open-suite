package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.base.db.Company;
import com.axelor.meta.db.MetaFile;

public interface FECImportService {

  /**
   * Method to get company from dataMetaFile.
   *
   * @param dataMetaFile
   * @return company.
   */
  Company getCompany(MetaFile dataMetaFile);
}
