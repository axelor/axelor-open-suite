/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
