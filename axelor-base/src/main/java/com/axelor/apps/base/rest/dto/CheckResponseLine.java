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

public class CheckResponseLine extends ResponseStructure {

  public static final String CHECK_TYPE_ALERT = "alert";
  public static final String CHECK_TYPE_ERROR = "error";

  private String message;
  private String checkType;
  private String modelName;

  public CheckResponseLine(Model model, String message, String checkType) {
    super(model.getVersion());
    this.message = message;
    this.checkType = checkType;
    this.modelName = model.getClass().getSimpleName();
  }

  public String getMessage() {
    return message;
  }

  public String getCheckType() {
    return checkType;
  }

  public String getModelName() {
    return modelName;
  }
}
