/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.builder;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.views.KanbanView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.exception.IExceptionMessage;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KanbanBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ViewBuilderService viewBuilderService;

  public KanbanView build(ViewBuilder viewBuilder, String module) throws AxelorException {

    log.debug(
        "Creating kanban view for module: {}, viewBuilder: {}", module, viewBuilder.getName());

    List<ViewItem> viewItems = viewBuilder.getViewItemList();
    if (viewItems.isEmpty()) {
      log.debug("No view items found");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PLEASE_ADD_FIELDS));
    }

    KanbanView view = new KanbanView();
    viewBuilderService.processCommon(view, viewBuilder, module);
    view.setOnNew(viewBuilder.getOnNew());
    view.setOnMove(viewBuilder.getOnLoad());
    view.setColumnBy(viewBuilder.getColumnBy());
    if (viewBuilder.getSequenceBy() != null) {
      view.setSequenceBy(viewBuilder.getSequenceBy());
    }
    view.setDraggable(viewBuilder.getDraggable());
    view.setCustomSearch(viewBuilder.getCustomSearch());
    view.setFreeSearch(viewBuilder.getFreeSearchSelect());
    view.setLimit(viewBuilder.getRecordLimit());
    view.setItems(viewBuilderService.getItems(viewBuilder));
    view.setTemplate(createTemplate(viewBuilder));

    return view;
  }

  private String createTemplate(ViewBuilder viewBuilder) throws AxelorException {

    StringBuilder template = new StringBuilder("");
    StringBuilder body = new StringBuilder();
    String model = viewBuilder.getModel();
    boolean isJson = viewBuilder.getIsJson();

    viewBuilderService.sortBySequence(viewBuilder.getViewItemList());

    for (ViewItem viewItem : viewBuilder.getViewItemList()) {

      if (template.length() == 0) {
        template.append("\t\t<h4>{{" + viewItem.getName() + "}}</h4>\n");
        continue;
      }

      String[] field = null;
      if (isJson) {
        field = viewBuilderService.getJsonField(model, viewItem.getName());
      } else {
        field = viewBuilderService.getMetaField(model, viewItem.getName());
      }

      body.append("\t\t\t\t<dt ng-show=\"");
      body.append(field[0]);
      body.append("\" x-translate>");
      body.append(field[1]);
      body.append("</dt><dd ng-show=\"");
      body.append(field[0]);
      body.append("\">{{");
      body.append(field[0]);
      body.append("}}</dd>\n");
    }

    if (body.length() != 0) {
      template.append("\t\t<div class=\"card-body\">\n");
      template.append("\t\t\t<dl>");
      template.append(body.toString());
      template.append("\t\t\t</dl>");
      template.append("\t\t</div>");
    }

    return template.toString();
  }
}
