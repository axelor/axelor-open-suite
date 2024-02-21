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
import com.axelor.utils.api.RequestStructure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetPutRequest extends RequestStructure {
  public static final String TIMESHEET_UPDATE_CONFIRM = "confirm";
  public static final String TIMESHEET_UPDATE_VALIDATE = "validate";
  public static final String TIMESHEET_UPDATE_REFUSE = "refuse";
  public static final String TIMESHEET_UPDATE_CANCEL = "cancel";
  private List<Long> timerIdList;
  private String groundForRefusal;

  @Pattern(
      regexp =
          TIMESHEET_UPDATE_CONFIRM
              + "|"
              + TIMESHEET_UPDATE_VALIDATE
              + "|"
              + TIMESHEET_UPDATE_REFUSE
              + "|"
              + TIMESHEET_UPDATE_CANCEL,
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String toStatus;

  public List<Long> getTimerIdList() {
    return timerIdList;
  }

  public void setTimerIdList(List<Long> timerIdList) {
    this.timerIdList = timerIdList;
  }

  public String getGroundForRefusal() {
    return groundForRefusal;
  }

  public void setGroundForRefusal(String groundForRefusal) {
    this.groundForRefusal = groundForRefusal;
  }

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
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
