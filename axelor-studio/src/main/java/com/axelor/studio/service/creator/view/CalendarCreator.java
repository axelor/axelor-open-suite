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
package com.axelor.studio.service.creator.view;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.CalendarView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import java.util.List;
import javax.xml.bind.JAXBException;

public class CalendarCreator extends AbstractViewCreator {

  public CalendarView getView(ViewBuilder viewBuilder) throws JAXBException {

    List<ViewItem> viewItems = viewBuilder.getViewItemList();
    if (viewItems.isEmpty()) {
      return null;
    }

    CalendarView view = getCalendarView(viewBuilder);
    if (view == null) {
      return null;
    }

    processCommon(view, viewBuilder);
    view.setOnChange(viewBuilder.getOnLoad());
    //		view.setCss(viewBuilder.getCss());
    view.setMode(viewBuilder.getModeSelect());
    //		view.setEventStart(viewBuilder.getEventStartMetaField().getName());
    view.setEditable(viewBuilder.getEditable());

    if (viewBuilder.getEventLength() != 0) {
      view.setEventLength(viewBuilder.getEventLength());
    } else {
      view.setEventLength(null);
    }

    //		if (viewBuilder.getEventStopMetaField() != null) {
    //			view.setEventStop(viewBuilder.getEventStopMetaField().getName());
    //		}
    //		else {
    view.setEventStop(null);
    //		}

    //		if (viewBuilder.getColorByMetaField() != null) {
    //			view.setColorBy(viewBuilder.getColorByMetaField().getName());
    //		}
    //		else {
    view.setColorBy(null);
    //		}

    view.setItems(updateItems(view.getItems(), viewBuilder.getViewItemList()));

    return view;
  }

  private CalendarView getCalendarView(ViewBuilder viewBuilder) throws JAXBException {

    MetaView metaView = viewBuilder.getMetaViewGenerated();

    if (metaView == null) {
      return new CalendarView();
    }

    ObjectViews objectViews = XMLViews.fromXML(metaView.getXml());
    List<AbstractView> views = objectViews.getViews();
    if (!views.isEmpty()) {
      return (CalendarView) views.get(0);
    }

    return null;
  }
}
