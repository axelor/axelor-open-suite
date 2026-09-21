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

import com.axelor.apps.quality.db.ControlEntryPlanLine;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.utils.api.ResponseStructure;
import java.util.Optional;

public class ControlEntrySampleLineResponse extends ResponseStructure {

  private Integer resultSelect;
  private Integer sampleResultSelect;

  public ControlEntrySampleLineResponse(ControlEntryPlanLine controlEntrySampleLine) {
    super(controlEntrySampleLine.getVersion());
    this.resultSelect = controlEntrySampleLine.getResultSelect();
    this.sampleResultSelect =
        Optional.ofNullable(controlEntrySampleLine.getControlEntrySample())
            .map(ControlEntrySample::getResultSelect)
            .orElse(null);
  }

  public Integer getResultSelect() {
    return resultSelect;
  }

  public void setResultSelect(Integer resultSelect) {
    this.resultSelect = resultSelect;
  }

  public Integer getSampleResultSelect() {
    return sampleResultSelect;
  }

  public void setSampleResultSelect(Integer sampleResultSelect) {
    this.sampleResultSelect = sampleResultSelect;
  }
}
