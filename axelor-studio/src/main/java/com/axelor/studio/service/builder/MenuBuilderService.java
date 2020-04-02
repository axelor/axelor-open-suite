/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.xml.bind.JAXBException;

public class MenuBuilderService {

  @Inject private ActionBuilderService actionBuilderService;

  @Inject private MetaMenuRepository metaMenuRepo;

  @Inject private StudioMetaService metaService;

  @Transactional
  public MetaMenu build(MenuBuilder builder) {

    MetaMenu menu = metaService.createMenu(builder);
    ActionBuilder actionBuilder = builder.getActionBuilder();
    if (actionBuilder != null) {
      if (actionBuilder.getName() == null) {
        actionBuilder.setName(menu.getName().replace("-", "."));
      }
      actionBuilder.setTitle(menu.getTitle());
      actionBuilder.setAppBuilder(builder.getAppBuilder());
      menu.setAction(actionBuilderService.build(actionBuilder));
    }

    MetaStore.clear();

    return metaMenuRepo.save(menu);
  }

  @SuppressWarnings("unchecked")
  public ActionBuilder createActionBuilder(MetaAction metaAction) {

    try {
      ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
      List<Action> actions = objectViews.getActions();
      if (actions != null && !actions.isEmpty()) {
        ActionView action = (ActionView) actions.get(0);
        if (action.getModel() != null
            && action.getModel().contentEquals(MetaJsonRecord.class.getName())) {
          return null;
        }
        ActionBuilder actionBuilder = new ActionBuilder(action.getName());
        actionBuilder.setTitle(action.getTitle());
        actionBuilder.setModel(action.getModel());
        actionBuilder.setTypeSelect(3);
        String domain = action.getDomain();
        actionBuilder.setDomainCondition(domain);
        for (ActionView.View view : action.getViews()) {
          ActionBuilderView builderView = new ActionBuilderView();
          builderView.setViewType(view.getType());
          builderView.setViewName(view.getName());
          actionBuilder.addActionBuilderView(builderView);
        }
        if (action.getParams() != null) {
          for (ActionView.Param param : action.getParams()) {
            ActionBuilderLine paramLine = new ActionBuilderLine();
            paramLine.setName(param.getName());
            paramLine.setValue(param.getValue());
            actionBuilder.addViewParam(paramLine);
          }
        }
        if (action.getContext() != null) {
          for (ActionView.Context ctx : (List<ActionView.Context>) action.getContext()) {
            ActionBuilderLine ctxLine = new ActionBuilderLine();
            ctxLine.setName(ctx.getName());
            if (ctx.getName().contentEquals("jsonModel")
                && domain != null
                && domain.contains("self.jsonModel = :jsonModel")) {
              actionBuilder.setIsJson(true);
              actionBuilder.setModel(ctx.getExpression());
            }
            ctxLine.setValue(ctx.getExpression());
            actionBuilder.addLine(ctxLine);
          }
        }

        return actionBuilder;
      }
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return null;
  }
}
