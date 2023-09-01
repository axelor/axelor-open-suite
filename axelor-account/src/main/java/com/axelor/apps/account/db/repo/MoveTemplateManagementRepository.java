package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.MoveTemplate;

public class MoveTemplateManagementRepository extends MoveTemplateRepository {
  @Override
  public MoveTemplate copy(MoveTemplate entity, boolean deep) {
    MoveTemplate copy = super.copy(entity, deep);

    copy.setIsValid(false);

    return copy;
  }
}
