package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class ProjectPlanningTImeRestrictedValueResponse extends ResponseStructure {
  protected boolean isSelectionOnDisplayPlannedTime;
  protected List<Long> plannedTimeValueIdList;

  public ProjectPlanningTImeRestrictedValueResponse(
      int version, boolean isSelectionOnDisplayPlannedTime, List<Long> plannedTimeValueIdList) {
    super(version);
    this.isSelectionOnDisplayPlannedTime = isSelectionOnDisplayPlannedTime;
    this.plannedTimeValueIdList = plannedTimeValueIdList;
  }

  public boolean isSelectionOnDisplayPlannedTime() {
    return isSelectionOnDisplayPlannedTime;
  }

  public List<Long> getPlannedTimeValueIdList() {
    return plannedTimeValueIdList;
  }
}
