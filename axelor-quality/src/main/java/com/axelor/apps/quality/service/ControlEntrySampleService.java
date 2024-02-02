package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;

public interface ControlEntrySampleService {
  ControlEntrySample createSample(int i, String defaultSampleName, ControlEntry controlEntry);

  void updateResult(ControlEntrySample controlEntrySample) throws AxelorException;
}
