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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ActionBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ActionViewBuilderService actionViewBuilderService;

  @Inject private ActionScriptBuilderService actionScriptBuilderService;

  @Inject private ActionEmailBuilderService actionEmailBuilderService;

  @Transactional
  public MetaAction build(ActionBuilder builder) {

    if (builder == null) {
      return null;
    }

    Integer typeSelect = builder.getTypeSelect();
    log.debug("Processing action: {}, type: {}", builder.getName(), builder.getTypeSelect());

    if (typeSelect < ActionBuilderRepository.TYPE_SCRIPT
        && (builder.getLines() == null || builder.getLines().isEmpty())) {
      return null;
    }

    MetaAction metaAction = null;

    if (typeSelect <= ActionBuilderRepository.TYPE_SCRIPT) {
      metaAction = actionScriptBuilderService.build(builder);
    } else if (typeSelect == ActionBuilderRepository.TYPE_VIEW) {
      metaAction = actionViewBuilderService.build(builder);
    } else if (typeSelect == ActionBuilderRepository.TYPE_EMAIL) {
      metaAction = actionEmailBuilderService.build(builder);
    }

    if (builder.getMetaModule() != null) {
      metaAction.setModule(builder.getMetaModule().getName());
    }

    MetaStore.clear();

    return metaAction;
  }

  public ActionBuilder setActionBuilderViews(ActionBuilder actionBuilder, String modelName,
      String formViewName, String gridViewName, String dashboardViewName) {
    List<ActionBuilderView> actionBuilderViews = new ArrayList<>();
    if (formViewName != null) {
      setActionBuilderView("form", formViewName, actionBuilderViews);
    }
    if (gridViewName != null) {
      setActionBuilderView("grid", gridViewName, actionBuilderViews);
    }
    if (dashboardViewName != null) {
      setActionBuilderView("dashboard", dashboardViewName, actionBuilderViews);
    }

    actionBuilder.setActionBuilderViews(actionBuilderViews);
    actionBuilder.setModel(modelName);
    return actionBuilder;
  }

  private void setActionBuilderView(String viewTypeTitle, String viewName, List<ActionBuilderView> actionBuilderViews) {
    String viewType = MetaStore.getSelectionItem("view.type.selection", viewTypeTitle).getLocalizedTitle();
    ActionBuilderView actionBuilderView = new ActionBuilderView();
    actionBuilderView.setViewType(viewType);
    actionBuilderView.setViewName(viewName);
    actionBuilderViews.add(actionBuilderView);
  }

  public ActionBuilder setActionBuilderLines(ActionBuilder actionBuilder, String contextLineName, String contextLineValue) {
    ActionBuilderLine contextLine = setActionBuilderLine(contextLineName, contextLineValue);
    List<ActionBuilderLine> lines = new ArrayList<>();
    lines.add(contextLine);
    actionBuilder.setLines(lines);
    return actionBuilder;
  }

  private ActionBuilderLine setActionBuilderLine(String name, String value) {
    ActionBuilderLine actionBuilderLine = new ActionBuilderLine();
    actionBuilderLine.setName(name);
    actionBuilderLine.setValue(value);
    return actionBuilderLine;
  }
}
