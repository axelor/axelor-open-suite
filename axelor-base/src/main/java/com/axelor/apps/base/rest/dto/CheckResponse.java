/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.rest.dto;

import com.axelor.db.Model;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class CheckResponse extends ResponseStructure {
  private Long modelId;
  private String modelName;
  private final List<CheckResponseLine> checks;
  private List<CheckResponse> otherChecks;

  public CheckResponse(Model model, List<CheckResponseLine> checks) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checks = checks;
  }

  public CheckResponse(
      Model model, List<CheckResponseLine> checks, List<CheckResponse> otherChecks) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checks = checks;
    this.otherChecks = otherChecks;
  }

  public List<CheckResponseLine> getChecks() {
    return checks;
  }

  public List<CheckResponse> getOtherChecks() {
    return otherChecks;
  }

  public Long getModelId() {
    return modelId;
  }

  public String getModelName() {
    return modelName;
  }
}
