package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.exception.AxelorException;

public interface OperationOrderRestService {

  void updateStatusOfOperationOrder(OperationOrder operationOrder, Integer targetStatus)
      throws AxelorException;
}
