package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.exception.AxelorException;

public interface ControlPointWorkflowService {

  /**
   * Set the control point status to closed.
   *
   * @param controlPoint
   * @throws AxelorException if the control point wasn't on hold.
   */
  void closeControlPoint(ControlPoint controlPoint) throws AxelorException;
}
