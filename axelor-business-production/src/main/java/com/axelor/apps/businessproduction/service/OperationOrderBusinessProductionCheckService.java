package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.OperationOrder;

public interface OperationOrderBusinessProductionCheckService {

  /**
   * This method checks if users currently working on the operation order are associated to a
   * employee.
   *
   * @param operationOrder
   * @return true if it the case, else false
   */
  boolean workingUsersHaveEmployee(OperationOrder operationOrder);

  /**
   * This method check if users currently working on the operaton order have timesheet imputation
   * select set to tsImputationSelect.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param operationOrder
   * @param tsImputationSelect
   * @return true if it the case, else false
   */
  boolean workingUsersHaveTSImputationSelect(OperationOrder operationOrder, int tsImputationSelect);

  /**
   * This method check if users currently working on the operaton order have their time logging
   * preference not set on DAYS.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param operationOrder
   * @return true if it the case, else false
   */
  boolean workingUsersHaveCorrectTimeLoggingPref(OperationOrder operationOrder);

  /**
   * This method check if users currently working on the operaton order have their employee time
   * logging preference matching with their current timesheet.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param operationOrder
   * @return true if it the case, else false
   */
  boolean workingUsersHaveTSTimeLoggingPrefMatching(OperationOrder operationOrder);
}
