package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.service.ControlEntryPlanLineService;
import com.axelor.apps.quality.service.ControlEntrySampleService;
import com.axelor.inject.Beans;

public class ControlEntryPlanLineManagementRepository extends ControlEntryPlanLineRepository {

  @Override
  public ControlEntryPlanLine save(ControlEntryPlanLine entity) {

    try {
      if (entity.getTypeSelect().equals(TYPE_ENTRY_SAMPLE_LINE)) {
        Beans.get(ControlEntryPlanLineService.class).conformityEval(entity);
      }
      if (entity.getControlEntrySample() != null) {
        Beans.get(ControlEntrySampleService.class).updateResult(entity.getControlEntrySample());
      }
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
    }

    return super.save(entity);
  }
}
