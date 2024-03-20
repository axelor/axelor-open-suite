package com.axelor.apps.quality.db.repo;

import com.axelor.apps.quality.db.ControlPlan;
import com.axelor.inject.Beans;
import java.util.stream.Collectors;

public class ControlPlanManagementRepository extends ControlPlanRepository {

  @Override
  public ControlPlan copy(ControlPlan entity, boolean deep) {
    ControlPlan copy = super.copy(entity, deep);

    copy.setStatusSelect(DRAFT_STATUS);
    copy.clearControlPlanLinesList();

    if (entity.getControlPlanLinesList() != null) {
      ControlEntryPlanLineRepository controlEntryPlanLineRepository =
          Beans.get(ControlEntryPlanLineRepository.class);
      copy.setControlPlanLinesList(
          entity.getControlPlanLinesList().stream()
              .map(
                  controlEntryPlanLine ->
                      controlEntryPlanLineRepository.copy(controlEntryPlanLine, deep))
              .collect(Collectors.toList()));
    }

    return copy;
  }
}
