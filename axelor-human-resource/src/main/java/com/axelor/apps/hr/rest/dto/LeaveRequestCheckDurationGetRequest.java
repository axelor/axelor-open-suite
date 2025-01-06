package com.axelor.apps.hr.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;

public class LeaveRequestCheckDurationGetRequest extends RequestPostStructure {

  @NotNull private LocalDate fromDate;

  @NotNull private LocalDate toDate;

  @NotNull private int startOnSelect;

  @NotNull private int endOnSelect;

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

  public int getStartOnSelect() {
    return startOnSelect;
  }

  public void setStartOnSelect(int startOnSelect) {
    this.startOnSelect = startOnSelect;
  }

  public int getEndOnSelect() {
    return endOnSelect;
  }

  public void setEndOnSelect(int endOnSelect) {
    this.endOnSelect = endOnSelect;
  }
}
