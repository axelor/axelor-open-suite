package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntrySample;

public interface ControlEntrySampleUpdateService {

  void updateResult(ControlEntrySample controlEntrySample) throws AxelorException;
}
