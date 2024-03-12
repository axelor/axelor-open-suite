package com.axelor.apps.quality.db.repo;

import com.axelor.apps.quality.db.ControlPlan;

public class ControlPlanManagementRepository extends ControlPlanRepository {

  @Override
  public ControlPlan copy(ControlPlan entity, boolean deep) {
    ControlPlan copy = super.copy(entity, deep);

    copy.setStatusSelect(DRAFT_STATUS);

    return copy;
  }
}
