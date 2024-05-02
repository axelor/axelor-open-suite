package com.axelor.apps.quality.db.repo;

import com.axelor.apps.quality.db.ControlEntryPlanLine;

public class ControlEntryPlanLineManagementRepository extends ControlEntryPlanLineRepository {

  @Override
  public ControlEntryPlanLine copy(ControlEntryPlanLine entity, boolean deep) {
    ControlEntryPlanLine copy = super.copy(entity, deep);

    copy.setEntryAttrs(null);
    copy.setPlanAttrs(null);

    return copy;
  }
}
