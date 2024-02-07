package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.rest.dto.ControlEntryProgressValuesResponse;

public interface ControlEntryProgressValuesComputeService {
  ControlEntryProgressValuesResponse getProgressValues(
      ControlEntry controlEntry, Long characteristicId, Long sampleId) throws AxelorException;
}
