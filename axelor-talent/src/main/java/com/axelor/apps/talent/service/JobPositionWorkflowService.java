package com.axelor.apps.talent.service;

import com.axelor.apps.talent.db.JobPosition;
import com.axelor.exception.AxelorException;

public interface JobPositionWorkflowService {

  void backToDraft(JobPosition jobPosition) throws AxelorException;

  void open(JobPosition jobPosition) throws AxelorException;

  void close(JobPosition jobPosition) throws AxelorException;

  void pause(JobPosition jobPosition) throws AxelorException;

  void cancel(JobPosition jobPosition) throws AxelorException;
}
