package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.inject.Beans;

public class ControlEntryManagementRepository extends ControlEntryRepository {

  @Override
  public ControlEntry copy(ControlEntry entity, boolean deep) {
    ControlEntry copy = super.copy(entity, deep);

    copy.setStatusSelect(DRAFT_STATUS);
    copy.setEntryDateTime(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    copy.clearControlEntrySamplesList();

    return copy;
  }
}
