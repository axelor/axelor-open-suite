package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticLevel;
import com.axelor.common.ObjectUtils;

public class AnalyticLevelManagementRepository extends AnalyticLevelRepository {

  @Override
  public AnalyticLevel save(AnalyticLevel entity) {

    if (ObjectUtils.isEmpty(entity.getName())) {
      entity.setName(entity.getNbr().toString());
    }

    return super.save(entity);
  }
}
