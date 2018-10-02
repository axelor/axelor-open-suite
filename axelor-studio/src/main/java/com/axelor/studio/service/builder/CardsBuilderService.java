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
import com.axelor.meta.schema.views.CardsView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.exception.IExceptionMessage;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardsBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ViewBuilderService viewBuilderService;

  public CardsView build(ViewBuilder viewBuilder, String module) throws AxelorException {

    log.debug("Creating cards view for module: {}, viewBuilder: {}", module, viewBuilder.getName());

    List<ViewItem> viewItems = viewBuilder.getViewItemList();
    if (viewItems.isEmpty()) {
      log.debug("No view items found");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PLEASE_ADD_FIELDS));
    }

    CardsView view = new CardsView();
    viewBuilderService.processCommon(view, viewBuilder, module);
    view.setCss("rect-image");
    view.setItems(viewBuilderService.getItems(viewBuilder));
    view.setTemplate(createTemplate(viewBuilder));

    return view;
  }

  private String createTemplate(ViewBuilder viewBuilder) throws AxelorException {

    StringBuilder template = new StringBuilder("");
    StringBuilder body = new StringBuilder();
    StringBuilder imageBody = new StringBuilder();
    String model = viewBuilder.getModel();
    boolean isJson = viewBuilder.getIsJson();

    viewBuilderService.sortBySequence(viewBuilder.getViewItemList());

    for (ViewItem viewItem : viewBuilder.getViewItemList()) {

      if (template.length() == 0) {
        template.append(
            "\t\t<div class=\"span12\"><strong>{{" + viewItem.getName() + "}}</strong></div>\n");
        continue;
      }

      String[] field = null;
      if (isJson) {
        field = viewBuilderService.getJsonField(model, viewItem.getName());
      } else {
        field = viewBuilderService.getMetaField(model, viewItem.getName());
      }

      if (field[2].equals("image")) {
        if (imageBody.length() == 0) {
          imageBody.append("\t\t<div class=\"span4 card-image\">\n");
          imageBody.append("\t\t<img ng-show=\"");
          imageBody.append(field[0]);
          imageBody.append("\" ng-src=\"{{$image(");
          imageBody.append("'" + field[0] + "'");
          imageBody.append(", 'content')}}\" />\n\t\t</div>\n");
        }
        continue;
      }

      body.append("\n\t\t\t{{");
      body.append(viewBuilderService.getFormatted(field[0], field[2]));
      body.append("}}<br/>");
    }

    template.append("\t\t<div>");
    String bodySpan = "span12";
    if (imageBody.length() != 0) {
      template.append(imageBody);
      bodySpan = "span8";
    }
    if (body.length() != 0) {
      template.append("\t\t<div class=\"" + bodySpan + "\">\n");
      template.append("\t\t\t<span>");
      template.append(body.toString());
      template.append("\t\t\t</span>\n");
      template.append("\t\t</div>");
    }

    return template.toString();
  }
}
