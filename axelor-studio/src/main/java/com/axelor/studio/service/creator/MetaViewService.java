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
package com.axelor.studio.service.creator;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.AbstractView;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MetaViewService {

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private MetaActionRepository metaActionRepo;

  /**
   * Create or Update metaView from AbstractView.
   *
   * @param viewIterator ViewBuilder iterator
   */
  @Transactional
  public MetaView generateMetaView(String module, AbstractView view) {

    String name = view.getName();
    String xmlId = view.getXmlId();
    String model = view.getModel();
    String viewType = view.getType();

    MetaView metaView;
    if (xmlId != null) {
      metaView =
          metaViewRepo
              .all()
              .filter(
                  "self.name = ?1 and self.xmlId = ?2 and self.type = ?3", name, xmlId, viewType)
              .fetchOne();
    } else {
      metaView =
          metaViewRepo.all().filter("self.name = ?1 and self.type = ?2", name, viewType).fetchOne();
    }

    if (metaView == null) {
      metaView =
          metaViewRepo
              .all()
              .filter("self.name = ?1 and self.type = ?2", name, viewType)
              .order("-priority")
              .fetchOne();
      Integer priority = 20;
      if (metaView != null) {
        priority = metaView.getPriority() + 1;
      }
      metaView = new MetaView();
      metaView.setName(name);
      metaView.setModule(module);
      metaView.setXmlId(xmlId);
      metaView.setModel(model);
      metaView.setPriority(priority);
      metaView.setType(viewType);
      metaView.setTitle(view.getTitle());
    }

    String viewXml = XMLViews.toXml(view, true);
    metaView.setXml(viewXml.toString());

    return metaViewRepo.save(metaView);
  }

  /**
   * Create or Update MetaAction from Action
   *
   * @param actionIterator Action iterator.
   */
  @Transactional
  public void generateMetaAction(String module, List<Action> actions) {

    if (module == null || actions == null) {
      return;
    }

    for (Action action : actions) {
      if (action == null) {
        continue;
      }
      String name = action.getName();

      MetaAction metaAction =
          metaActionRepo
              .all()
              .filter("self.name = ?1 and self.module = ?2", name, module)
              .fetchOne();

      if (metaAction == null) {
        metaAction = new MetaAction();
        metaAction.setModule(module);
        metaAction.setName(name);
        metaAction.setModel(action.getModel());
        Class<?> klass = action.getClass();
        String type = klass.getSimpleName().replaceAll("([a-z\\d])([A-Z]+)", "$1-$2").toLowerCase();
        metaAction.setType(type);
        metaAction.setXmlId(action.getXmlId());
      }

      metaAction.setXml(XMLViews.toXml(action, true));

      metaAction = metaActionRepo.save(metaAction);
    }
  }
}
