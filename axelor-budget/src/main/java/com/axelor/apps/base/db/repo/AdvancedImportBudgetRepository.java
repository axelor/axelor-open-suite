package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.AdvancedImport;

public class AdvancedImportBudgetRepository extends AdvancedImportBaseRepository {

  /** For not allowing user to delete these two records. */
  public static final String IMPORT_ID_1 = "101"; // Budget Template Import Id

  public static final String IMPORT_ID_2 = "102"; // Budget Instance Import Id

  @Override
  public void remove(AdvancedImport entity) {
    if (entity.getImportId() != null
        && (entity.getImportId().equals(IMPORT_ID_1) || entity.getImportId().equals(IMPORT_ID_2))) {
      return;
    }
    super.remove(entity);
  }
}
