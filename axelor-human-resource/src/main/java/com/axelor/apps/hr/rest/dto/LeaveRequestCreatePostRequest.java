/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
