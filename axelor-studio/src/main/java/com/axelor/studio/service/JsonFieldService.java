/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.service;

import com.axelor.common.Inflector;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.studio.service.builder.SelectionBuilderService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class JsonFieldService {

  public static final String SELECTION_PREFIX = "custom-json-select-";

  @Inject private SelectionBuilderService selectionBuilderService;

  @Transactional
  public void updateSelection(MetaJsonField metaJsonField) {

    String selectionText = metaJsonField.getSelectionText();

    String name = getSelectionName(metaJsonField);

    if (Strings.isNullOrEmpty(selectionText)) {
      selectionBuilderService.removeSelection(name, null);

      if (metaJsonField.getSelection() != null && metaJsonField.getSelection().equals(name)) {
        metaJsonField.setSelection(null);
      }

      return;
    }

    metaJsonField.setSelection(
        selectionBuilderService.updateMetaSelectFromText(
            selectionText, name, metaJsonField.getAppBuilder(), null));
  }

  @Transactional
  public void removeSelection(MetaJsonField metaJsonField) {

    String name = getSelectionName(metaJsonField);

    if (metaJsonField.getSelection() != null && metaJsonField.getSelection().equals(name)) {
      selectionBuilderService.removeSelection(name, null);
    }
  }

  @CallMethod
  public String checkName(String name, boolean isFieldName) {
    if (name == null) return "";
    name = name.replaceAll("[^a-zA-Z0-9 ]", "");
    return Inflector.getInstance().camelize(name, isFieldName);
  }

  public String getSelectionName(MetaJsonField metaJsonField) {

    String model =
        metaJsonField.getJsonModel() != null
            ? metaJsonField.getJsonModel().getName()
            : metaJsonField.getModel();
    model = model.contains(".") ? model.substring(model.lastIndexOf(".") + 1) : model;

    return SELECTION_PREFIX + model + "-" + metaJsonField.getName();
  }
}
