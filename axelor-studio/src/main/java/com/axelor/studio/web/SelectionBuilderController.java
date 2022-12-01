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

import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelect;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.SelectionBuilder;
import com.axelor.studio.db.repo.SelectionBuilderRepository;
import com.axelor.studio.service.builder.SelectionBuilderService;
import java.util.List;
import java.util.Map;

public class SelectionBuilderController {

  public void fillSelectionText(ActionRequest request, ActionResponse response) {

    MetaSelect metaSelect = (MetaSelect) request.getContext().get("metaSelect");

    if (metaSelect != null) {
      String name = metaSelect.getName();
      List<Map<String, String>> selectOptions =
          Beans.get(SelectionBuilderService.class).createSelectionText(name);

      String selectionText =
          Beans.get(SelectionBuilderService.class).generateSelectionText(selectOptions);

      response.setValue("selectionText", selectionText);
      response.setValue("$selectOptionList", selectOptions);
      response.setValue("name", name);
    } else {
      response.setValue("$selectOptionList", null);
      response.setValue("selectionText", null);
      response.setValue("name", null);
    }
  }

  @SuppressWarnings("unchecked")
  public void generateSelectionText(ActionRequest request, ActionResponse response) {
    try {
      List<Map<String, String>> selectOptions =
          (List<Map<String, String>>) request.getContext().get("selectOptionList");

      String selectionText =
          Beans.get(SelectionBuilderService.class).generateSelectionText(selectOptions);

      response.setValue("selectionText", selectionText);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillSelectOption(ActionRequest request, ActionResponse response) {
    try {
      SelectionBuilder selectionBuilder = request.getContext().asType(SelectionBuilder.class);
      if (selectionBuilder.getId() != null) {
        selectionBuilder =
            Beans.get(SelectionBuilderRepository.class).find(selectionBuilder.getId());
      }

      List<Map<String, String>> selectOptions =
          Beans.get(SelectionBuilderService.class)
              .getSelectOptions(selectionBuilder.getSelectionText());

      response.setValue("$selectOptionList", selectOptions);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
