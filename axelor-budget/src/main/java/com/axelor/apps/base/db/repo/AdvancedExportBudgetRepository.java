package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.AdvancedExport;

public class AdvancedExportBudgetRepository extends AdvancedExportRepository {

  /** For not allowing user to delete these two records. */
  public static final String EXPORT_ID_1 = "101"; // Budget Template Import Id

  public static final String EXPORT_ID_2 = "102"; // Budget Instance Import Id

  @Override
  public void remove(AdvancedExport entity) {

    String importId = entity.getImportId();
    if (importId != null && (importId.equals(EXPORT_ID_1) || importId.equals(EXPORT_ID_2))) {
      return;
    }
    super.remove(entity);
  }
}
