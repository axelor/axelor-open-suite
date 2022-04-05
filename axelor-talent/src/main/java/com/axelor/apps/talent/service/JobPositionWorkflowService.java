package com.axelor.apps.talent.service;

import com.axelor.apps.talent.db.JobPosition;
import com.axelor.exception.AxelorException;

public interface JobPositionWorkflowService {

  /**
   * Set the job position status to draft.
   *
   * @param jobPosition
   * @throws AxelorException if the job position wasn't canceled.
   */
  void backToDraft(JobPosition jobPosition) throws AxelorException;

  /**
   * Set the job position status to open.
   *
   * @param jobPosition
   * @throws AxelorException if the job position wasn't drafted.
   */
  void open(JobPosition jobPosition) throws AxelorException;

  /**
   * Set the job position status to closed.
   *
   * @param jobPosition
   * @throws AxelorException if the job position wasn't open nor on hold.
   */
  void close(JobPosition jobPosition) throws AxelorException;

  /**
   * Set the job position status to on hold.
   *
   * @param jobPosition
   * @throws AxelorException if the job position wasn't open.
   */
  void pause(JobPosition jobPosition) throws AxelorException;

  /**
   * Set the job position status to canceled.
   *
   * @param jobPosition
   * @throws AxelorException if the job position wasn't closed.
   */
  void cancel(JobPosition jobPosition) throws AxelorException;
}
