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
