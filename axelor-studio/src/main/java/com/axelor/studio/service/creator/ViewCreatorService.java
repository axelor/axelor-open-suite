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

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.ViewRemovalService;
import com.axelor.studio.service.ViewWriterService;
import com.axelor.studio.service.creator.view.AbstractViewCreator;
import com.axelor.studio.service.creator.view.AbstractViewCreatoryFactory;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

/**
 * This service class handle all view side processing. It call recording of different types of
 * ViewBuilder. Provides common method to create view and action xml for all types of view. It also
 * call Menu and RightMangement processing.
 *
 * @author axelor
 */
public class ViewCreatorService {

  private List<Action> actions;

  @Inject private ViewBuilderRepository viewBuilderRepo;

  @Inject private ViewRemovalService removalService;

  @Inject private ConfigurationService configService;

  @Inject private MenuCreatorService menuCreatorService;

  @Inject private ActionCreatorService actionCreatorService;

  @Inject private ViewWriterService viewWriterService;

  @Inject private MetaViewService metaViewService;

  @Inject private AbstractViewCreatoryFactory viewCreatoryFactory;

  public String build(String module, boolean updateMeta, boolean autoCreate, boolean updateAll)
      throws AxelorException, JAXBException, IOException {

    try {

      File viewDir = null;
      if (!updateMeta) {
        viewDir = configService.getViewDir(module, true);
        menuCreatorService.writeMenus(module, viewDir);
        actionCreatorService.writeActions(module, viewDir);
      }
      removalService.remove(module, viewDir);

      List<ViewBuilder> viewBuilders = getViewBuilders(module, updateMeta, updateAll);

      Map<String, List<ViewBuilder>> modelMap = splitByModel(viewBuilders);
      for (String model : modelMap.keySet()) {

        for (ViewBuilder viewBuilder : modelMap.get(model)) {
          actions = new ArrayList<>();
          AbstractView view = processView(autoCreate, viewBuilder);
          if (view != null) {
            MetaView metaView = metaViewService.generateMetaView(module, view);
            viewBuilder.setMetaViewGenerated(metaView);
            metaViewService.generateMetaAction(module, actions);
          }

          if (!updateMeta && (view != null || !actions.isEmpty())) {
            viewWriterService.writeView(viewDir, model, view, actions);
          }
        }
      }

      updateEdited(viewBuilders, updateMeta);

      return null;
    } catch (IOException | JAXBException e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }

  private AbstractView processView(boolean autoCreate, ViewBuilder viewBuilder)
      throws JAXBException, AxelorException {

    AbstractViewCreator viewCreator =
        viewCreatoryFactory.getViewCreator(viewBuilder.getViewType(), autoCreate);

    AbstractView view = viewCreator.getView(viewBuilder);
    if (viewCreator.getActions() != null) {
      actions.addAll(viewCreator.getActions());
    }

    return view;
  }

  private List<ViewBuilder> getViewBuilders(String module, boolean updateMeta, boolean updateAll) {

    String query = "self.metaModule.name = ?1";

    if (!updateAll) {
      query += " AND (self.edited = true";
      if (!updateMeta) {
        query += " OR self.recorded = false";
      }
      query += ")";
    }

    return viewBuilderRepo.all().filter(query, module).fetch();
  }

  /**
   * Update 'edited' boolean of ViewBuilder to false and 'recorded' to true if not updateMeta.
   * Method called at the end of ViewBuilder processing.
   *
   * @param viewBuilders List of ViewBuilders.
   * @param updateMeta Boolean to check if only to update 'edited' boolean or 'recorded' too.
   */
  @Transactional
  public void updateEdited(List<ViewBuilder> viewBuilders, boolean updateMeta) {

    for (ViewBuilder viewBuilder : viewBuilders) {
      if (!updateMeta) {
        //				viewBuilder.setRecorded(true);
      }
      //			viewBuilder.setEdited(false);
      viewBuilderRepo.save(viewBuilder);
    }
  }

  /**
   * Split ViewBuilder according to model and update modelMap.
   *
   * @param iterator ViewBuilder iterator
   */
  private Map<String, List<ViewBuilder>> splitByModel(List<ViewBuilder> viewBuilders) {

    Map<String, List<ViewBuilder>> modelMap = Maps.newHashMap();

    for (ViewBuilder viewBuilder : viewBuilders) {
      String model = viewBuilder.getModel();
      String viewType = viewBuilder.getViewType();

      if (model == null) {
        if (viewType.equals("dashboard")) {
          model = "Dashboard";
        } else {
          continue;
        }
      } else {
        model = model.substring(model.lastIndexOf(".") + 1);
      }

      if (!modelMap.containsKey(model)) {
        modelMap.put(model, new ArrayList<ViewBuilder>());
      }
      modelMap.get(model).add(viewBuilder);
    }

    return modelMap;
  }
}
