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
import javax.validation.constraints.NotNull;

public class LeaveRequestCheckDurationPostRequest extends RequestPostStructure {

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
