package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;

public class LeaveRequestCreatePostRequest extends RequestPostStructure {

  @NotNull private LocalDate fromDate;

  @NotNull private Integer startOnSelect;

  List<LeaveRequestReasonRequest> requests;

  public LocalDate getFromDate() {
    return fromDate;
  }

  public void setFromDate(LocalDate fromDate) {
    this.fromDate = fromDate;
  }

  public Integer getStartOnSelect() {
    return startOnSelect;
  }

  public void setStartOnSelect(Integer startOnSelect) {
    this.startOnSelect = startOnSelect;
  }

  public List<LeaveRequestReasonRequest> getRequests() {
    return requests;
  }

  public void setRequests(List<LeaveRequestReasonRequest> requests) {
    this.requests = requests;
  }
}
