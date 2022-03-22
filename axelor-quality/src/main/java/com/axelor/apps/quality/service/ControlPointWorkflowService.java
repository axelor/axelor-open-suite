package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.exception.AxelorException;

public interface ControlPointWorkflowService {

  void closeControlPoint(ControlPoint controlPoint) throws AxelorException;
}
