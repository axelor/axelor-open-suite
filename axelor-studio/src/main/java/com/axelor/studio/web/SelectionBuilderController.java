/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelect;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.service.builder.SelectionBuilderService;

public class SelectionBuilderController {

  public void fillSelectionText(ActionRequest request, ActionResponse response) {

    MetaSelect metaSelect = (MetaSelect) request.getContext().get("metaSelect");

    if (metaSelect != null) {
      String name = metaSelect.getName();
      response.setValue(
          "selectionText", Beans.get(SelectionBuilderService.class).createSelectionText(name));
      response.setValue("name", name);
    } else {
      response.setValue("selectionText", null);
      response.setValue("name", null);
    }
  }
}
