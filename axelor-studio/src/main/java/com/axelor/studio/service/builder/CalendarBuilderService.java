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
import com.axelor.meta.schema.views.CalendarView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.exception.IExceptionMessage;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ViewBuilderService viewBuilderService;

  public CalendarView build(ViewBuilder viewBuilder, String module) throws AxelorException {

    log.debug(
        "Creating calendar view for module: {}, viewBuilder: {}", module, viewBuilder.getName());

    List<ViewItem> viewItems = viewBuilder.getViewItemList();
    if (viewItems.isEmpty()) {
      log.debug("No view items found");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PLEASE_ADD_FIELDS));
    }

    CalendarView view = new CalendarView();
    viewBuilderService.processCommon(view, viewBuilder, module);
    view.setOnChange(viewBuilder.getOnLoad());
    view.setMode(viewBuilder.getModeSelect());
    view.setEventStart(viewBuilder.getEventStart());
    view.setEditable(viewBuilder.getEditable());
    view.setEventStop(viewBuilder.getEventStop());
    view.setColorBy(viewBuilder.getColorBy());
    if (viewBuilder.getEventLength() != 0) {
      view.setEventLength(viewBuilder.getEventLength());
    } else {
      view.setEventLength(null);
    }

    view.setItems(viewBuilderService.getItems(viewBuilder));

    return view;
  }
}
