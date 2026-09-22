/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.rest.dto;

import com.axelor.utils.api.RequestStructure;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ControlEntrySampleLinePutRequest extends RequestStructure {

  @NotNull @Valid protected List<ControlTypeFieldValuePutRequest> entryValueList;

  public List<ControlTypeFieldValuePutRequest> getEntryValueList() {
    return entryValueList;
  }

  public void setEntryValueList(List<ControlTypeFieldValuePutRequest> entryValueList) {
    this.entryValueList = entryValueList;
  }

  public Long[] getEntryValueIds() {
    return entryValueList.stream()
        .map(ControlTypeFieldValuePutRequest::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList())
        .toArray(new Long[0]);
  }
}
