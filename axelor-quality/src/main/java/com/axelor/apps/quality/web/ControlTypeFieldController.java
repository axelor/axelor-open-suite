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

import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.repo.ControlTypeFieldRepository;
import com.axelor.apps.quality.db.repo.ControlTypeFieldValueRepository;
import com.axelor.apps.quality.db.repo.ControlTypeRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  /** Attaches the existing fields selected in the wizard to the control type of the dashlet. */
  @SuppressWarnings("unchecked")
  public void addToControlType(ActionRequest request, ActionResponse response) {

    ControlType controlType = getControlType(request.getContext().get("_controlTypeId"));
    List<Map<String, Object>> selectedFields =
        (List<Map<String, Object>>) request.getContext().get("controlTypeFieldSet");

    if (controlType == null || selectedFields == null) {
      response.setCanClose(true);
      return;
    }

    ControlTypeFieldRepository controlTypeFieldRepository =
        Beans.get(ControlTypeFieldRepository.class);
    selectedFields.stream()
        .map(
            selected ->
                controlTypeFieldRepository.find(Long.valueOf(selected.get("id").toString())))
        .filter(Objects::nonNull)
        .forEach(controlTypeField -> link(controlTypeField, controlType, true));

    response.setCanClose(true);
  }

  /** Detaches the field of the clicked row from its control type, without deleting the field. */
  public void removeFromControlType(ActionRequest request, ActionResponse response) {

    ControlType controlType = getControlType(request.getContext().get("_controlTypeId"));
    ControlTypeField controlTypeField =
        Beans.get(ControlTypeFieldRepository.class)
            .find(request.getContext().asType(ControlTypeField.class).getId());

    if (controlType == null || controlTypeField == null) {
      return;
    }

    link(controlTypeField, controlType, false);

    response.setReload(true);
  }

  @Transactional
  protected void link(ControlTypeField controlTypeField, ControlType controlType, boolean add) {
    if (add) {
      controlTypeField.addControlTypeSetItem(controlType);
    } else {
      controlTypeField.removeControlTypeSetItem(controlType);
    }
    JPA.save(controlTypeField);
  }

  protected ControlType getControlType(Object id) {
    if (id == null) {
      return null;
    }
    return Beans.get(ControlTypeRepository.class).find(Long.valueOf(id.toString()));
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
