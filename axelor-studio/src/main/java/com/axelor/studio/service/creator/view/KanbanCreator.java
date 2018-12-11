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
import com.axelor.meta.schema.views.KanbanView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import java.util.List;
import javax.xml.bind.JAXBException;

public class KanbanCreator extends AbstractViewCreator {

  public KanbanView getView(ViewBuilder viewBuilder) throws JAXBException {

    List<ViewItem> viewItems = viewBuilder.getViewItemList();
    if (viewItems.isEmpty()) {
      return null;
    }

    KanbanView view = getKanbanView(viewBuilder);
    if (view == null) {
      return null;
    }
    processCommon(view, viewBuilder);
    view.setOnNew(viewBuilder.getOnNew());
    view.setOnMove(viewBuilder.getOnLoad());
    //		view.setColumnBy(viewBuilder.getColumnByMetaField().getName());
    //		if (viewBuilder.getSequenceByMetaField() != null) {
    //			view.setSequenceBy(viewBuilder.getSequenceByMetaField().getName());
    //		}
    view.setDraggable(viewBuilder.getDraggable());
    view.setCustomSearch(viewBuilder.getCustomSearch());
    view.setFreeSearch(viewBuilder.getFreeSearchSelect());
    view.setLimit(viewBuilder.getRecordLimit());
    //		view.setTemplate(viewBuilder.getTemplateText());

    view.setItems(updateItems(view.getItems(), viewBuilder.getViewItemList()));

    return view;
  }

  private KanbanView getKanbanView(ViewBuilder viewBuilder) throws JAXBException {

    KanbanView kanban = null;

    MetaView metaView = viewBuilder.getMetaViewGenerated();

    if (metaView == null) {
      kanban = new KanbanView();
      return kanban;
    }

    ObjectViews objectViews = XMLViews.fromXML(metaView.getXml());
    List<AbstractView> views = objectViews.getViews();
    if (!views.isEmpty()) {
      kanban = (KanbanView) views.get(0);
    }

    return kanban;
  }
}
