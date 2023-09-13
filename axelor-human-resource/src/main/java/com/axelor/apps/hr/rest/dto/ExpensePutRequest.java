package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ExpensePutRequest extends RequestStructure {
  private List<Long> expenseLineIdList;

  public List<Long> getExpenseLineIdList() {
    return expenseLineIdList;
  }

  public void setExpenseLineIdList(List<Long> expenseLineIdList) {
    this.expenseLineIdList = expenseLineIdList;
  }

  public List<ExpenseLine> fetchExpenseLines() {
    if (CollectionUtils.isEmpty(expenseLineIdList)) {
      return Collections.emptyList();
    }

    List<ExpenseLine> expenseLineList = new ArrayList<>();
    for (Long id : expenseLineIdList) {
      expenseLineList.add(ObjectFinder.find(ExpenseLine.class, id, ObjectFinder.NO_VERSION));
    }
    return expenseLineList;
  }
}
