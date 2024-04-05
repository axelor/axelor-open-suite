/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.TSTimer;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetPostRequest extends RequestPostStructure {
  @NotNull private LocalDate fromDate;

  private LocalDate toDate;

  private List<Long> timerIdList;

  public LocalDate getFromDate() {
    return fromDate;
  }

  public void setFromDate(LocalDate fromDate) {
    this.fromDate = fromDate;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public void setToDate(LocalDate toDate) {
    this.toDate = toDate;
  }

  public List<Long> getTimerIdList() {
    return timerIdList;
  }

  public void setTimerIdList(List<Long> timerIdList) {
    this.timerIdList = timerIdList;
  }

  public List<TSTimer> fetchTSTimers() {
    if (CollectionUtils.isEmpty(timerIdList)) {
      return Collections.emptyList();
    }

    List<TSTimer> timerList = new ArrayList<>();
    for (Long id : timerIdList) {
      timerList.add(ObjectFinder.find(TSTimer.class, id, ObjectFinder.NO_VERSION));
    }
    return timerList;
  }
}
