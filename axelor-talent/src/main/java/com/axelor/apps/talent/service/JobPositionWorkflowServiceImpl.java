package com.axelor.apps.talent.service;

import com.axelor.apps.talent.db.JobPosition;
import com.axelor.apps.talent.db.repo.JobPositionRepository;
import com.axelor.apps.talent.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class JobPositionWorkflowServiceImpl implements JobPositionWorkflowService {
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void backToDraft(JobPosition jobPosition) throws AxelorException {
    if (jobPosition.getStatusSelect() == null
        || jobPosition.getStatusSelect() != JobPositionRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.JOB_POSITION_DRAFT_WRONG_STATUS));
    }
    jobPosition.setStatusSelect(JobPositionRepository.STATUS_DRAFT);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void open(JobPosition jobPosition) throws AxelorException {
    if (jobPosition.getStatusSelect() == null
        || jobPosition.getStatusSelect() != JobPositionRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.JOB_POSITION_OPEN_WRONG_STATUS));
    }
    jobPosition.setStatusSelect(JobPositionRepository.STATUS_OPEN);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void close(JobPosition jobPosition) throws AxelorException {

    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(JobPositionRepository.STATUS_OPEN);
    authorizedStatus.add(JobPositionRepository.STATUS_ON_HOLD);

    if (jobPosition.getStatusSelect() == null
        || !authorizedStatus.contains(jobPosition.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.JOB_POSITION_CLOSE_WRONG_STATUS));
    }
    jobPosition.setStatusSelect(JobPositionRepository.STATUS_CLOSED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void pause(JobPosition jobPosition) throws AxelorException {
    if (jobPosition.getStatusSelect() == null
        || jobPosition.getStatusSelect() != JobPositionRepository.STATUS_OPEN) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.JOB_POSITION_PAUSE_WRONG_STATUS));
    }
    jobPosition.setStatusSelect(JobPositionRepository.STATUS_ON_HOLD);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancel(JobPosition jobPosition) throws AxelorException {
    if (jobPosition.getStatusSelect() == null
        || jobPosition.getStatusSelect() != JobPositionRepository.STATUS_CLOSED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.JOB_POSITION_CANCEL_WRONG_STATUS));
    }
    jobPosition.setStatusSelect(JobPositionRepository.STATUS_CANCELED);
  }
}
