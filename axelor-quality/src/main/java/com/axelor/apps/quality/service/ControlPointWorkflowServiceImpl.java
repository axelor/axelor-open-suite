package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.apps.quality.db.repo.ControlPointRepository;
import com.axelor.apps.quality.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class ControlPointWorkflowServiceImpl implements ControlPointWorkflowService {

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void closeControlPoint(ControlPoint controlPoint) throws AxelorException {
    if (controlPoint.getStatusSelect() == null
        || controlPoint.getStatusSelect() != ControlPointRepository.STATUS_ON_HOLD) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.CONTROL_POINT_CLOSE_WRONG_STATUS));
    }
    controlPoint.setStatusSelect(ControlPointRepository.STATUS_DONE);
  }
}
