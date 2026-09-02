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
package com.axelor.apps.quality.web;

import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.repo.ControlTypeFieldValueRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Singleton;

@Singleton
public class ControlTypeFieldController {

  /**
   * The type and the usage decide in which column a value is stored and on which line it is
   * created. Changing them once values exist would make every stored value unreadable, so they are
   * frozen as soon as the field is used.
   */
  public void setUsedFieldsReadonly(ActionRequest request, ActionResponse response) {

    ControlTypeField controlTypeField = request.getContext().asType(ControlTypeField.class);

    boolean isUsed = controlTypeField.getId() != null && hasValues(controlTypeField);

    response.setAttr("typeSelect", "readonly", isUsed);
    response.setAttr("usageSelect", "readonly", isUsed);
    response.setAttr("$usedFieldMessage", "hidden", !isUsed);
  }

  protected boolean hasValues(ControlTypeField controlTypeField) {
    return Beans.get(ControlTypeFieldValueRepository.class)
            .all()
            .filter("self.controlTypeField = :controlTypeField")
            .bind("controlTypeField", controlTypeField)
            .count()
        > 0;
  }
}
