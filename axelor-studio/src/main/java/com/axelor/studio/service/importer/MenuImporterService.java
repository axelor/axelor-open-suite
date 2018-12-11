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
package com.axelor.studio.service.importer;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.TranslationService;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.exporter.MenuExporter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuImporterService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Integer parentMenuSeq = 0;

  private Map<Long, Integer> menuSeqMap = new HashMap<Long, Integer>();

  @Inject private MetaMenuRepository metaMenuRepo;

  @Inject private MenuBuilderRepository menuBuilderRepo;

  @Inject private TranslationService translationService;

  @Inject private ImporterService importerService;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private ViewBuilderRepository viewBuilderRepo;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private ActionBuilderRepository actionBuilderRepo;

  public void importMenus(DataReaderService reader, String key) throws AxelorException {
    log.debug("Importing menus");
    if (reader == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);
    for (int rowNum = 0; rowNum < totalLines; rowNum++) {
      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      MetaModule module = importerService.getModule(row[MenuExporter.MODULE], null);
      if (module == null) {
        continue;
      }

      String modelName = row[MenuExporter.OBJECT];
      MetaModel model = null;
      if (modelName != null) {
        model = metaModelRepo.findByName(modelName);
        if (model == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Menu model not found for sheet: %s row: %s"),
              key,
              rowNum + 1);
        }
      }

      createMenu(module, model, row, key, rowNum);
    }
  }

  @Transactional
  public void createMenu(MetaModule module, MetaModel model, String[] row, String key, int rowNum)
      throws AxelorException {

    String name = row[MenuExporter.NAME];
    String title = getTitle(row[MenuExporter.TITLE], row[MenuExporter.TITLE_FR], module.getName());

    if (name == null && title == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Menu name and title empty for sheet: %s row: %s"),
          key,
          rowNum + 1);
    }

    if (name == null) {
      name = title;
    }

    name = Inflector.getInstance().dasherize(name);

    MenuBuilder menuBuilder = getMenuBuilder(name, module, model);
    menuBuilder.setTitle(title);

    String parentName = row[MenuExporter.PARENT];
    if (parentName != null) {
      menuBuilder = setParentMenu(menuBuilder, parentName);
    }

    menuBuilder.setIcon(row[MenuExporter.ICON]);
    menuBuilder.setIconBackground(row[MenuExporter.BACKGROUND]);

    if (model != null) {
      ActionBuilder actionBuilder = getActionBuilder(menuBuilder, module, model, row, name);
      menuBuilder.setActionBuilder(actionBuilder);
      menuBuilder.setShowAction(true);
    }

    String order = row[MenuExporter.ORDER];
    menuBuilder = setMenuOrder(menuBuilder, order);

    menuBuilderRepo.save(menuBuilder);
  }

  private ActionBuilder getActionBuilder(
      MenuBuilder menuBuilder, MetaModule module, MetaModel model, String[] row, String name) {

    String actionName = name.replace("-", ".");

    ActionBuilder actionBuilder = menuBuilder.getActionBuilder();

    if (actionBuilder == null) {
      actionBuilder =
          actionBuilderRepo
              .all()
              .filter("self.name = ?1 and self.metaModule = ?2", actionName, module)
              .fetchOne();
    }

    if (actionBuilder == null) {
      actionBuilder = new ActionBuilder(actionName);
      actionBuilder.setMetaModule(module);
    }

    actionBuilder.setMenuAction(true);
    actionBuilder.setTypeSelect(3);
    actionBuilder.setDomainCondition(row[MenuExporter.FILTER]);
    actionBuilder.setModel(model.getFullName());
    actionBuilder.setTitle(menuBuilder.getTitle());
    actionBuilder.clearActionBuilderViews();

    String viewNames = row[MenuExporter.VIEWS];
    if (Strings.isNullOrEmpty(viewNames)) {
      viewNames = ViewLoaderService.getDefaultViewName(model.getName(), "grid");
      viewNames += "," + ViewLoaderService.getDefaultViewName(model.getName(), "form");
    }

    actionBuilder = setActionViews(actionBuilder, viewNames);

    return actionBuilder;
  }

  public ActionBuilder setActionViews(ActionBuilder actionBuilder, String viewNames) {

    List<String> names = Arrays.asList(viewNames.split(","));
    List<MetaView> views = metaViewRepo.all().filter("self.name in (?1)", names).fetch();

    if (views.isEmpty()) {
      return setViewBuilderSet(actionBuilder, names);
    }

    Set<String> viewTypes = new HashSet<>();

    for (MetaView view : views) {
      String type = view.getType();
      if (viewTypes.contains(type)) {
        continue;
      }
      viewTypes.add(type);
      ActionBuilderView abView = new ActionBuilderView();
      abView.setViewName(view.getName());
      abView.setViewType(type);
      actionBuilder.addActionBuilderView(abView);

      if (actionBuilder.getTitle() == null) {
        actionBuilder.setTitle(view.getTitle());
      }
    }

    return actionBuilder;
  }

  private ActionBuilder setViewBuilderSet(ActionBuilder actionBuilder, List<String> names) {

    log.debug("Menu action view builders name: {}", names);
    List<ViewBuilder> viewBuilders =
        viewBuilderRepo.all().filter("self.name in (?1)", names).fetch();

    for (ViewBuilder viewBuilder : viewBuilders) {
      ActionBuilderView abView = new ActionBuilderView();
      abView.setViewName(viewBuilder.getName());
      abView.setViewType(viewBuilder.getViewType());
      actionBuilder.addActionBuilderView(abView);
      if (actionBuilder.getTitle() == null) {
        actionBuilder.setTitle(viewBuilder.getTitle());
      }
    }

    return actionBuilder;
  }

  private String getTitle(String title, String titleFr, String module) {

    if (title == null) {
      title = titleFr;
    } else if (titleFr != null) {
      translationService.addTranslation(title, titleFr, "fr", module);
    }

    return title;
  }

  private MenuBuilder getMenuBuilder(String name, MetaModule module, MetaModel model) {

    MenuBuilder menuBuilder =
        menuBuilderRepo
            .all()
            .filter("self.name = ?1 and self.metaModule.name = ?2", name, module.getName())
            .fetchOne();
    if (menuBuilder == null) {
      menuBuilder = new MenuBuilder(name);
      menuBuilder.setMetaModule(module);
    }

    return menuBuilder;
  }

  private MenuBuilder setParentMenu(MenuBuilder menuBuilder, String parentName)
      throws AxelorException {

    parentName = Inflector.getInstance().dasherize(parentName);
    MetaMenu parentMenu = metaMenuRepo.findByName(parentName);
    if (parentMenu != null) {
      menuBuilder.setParentMenu(parentMenu);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("No parent menu found %s for menu %s"),
          parentName,
          menuBuilder.getTitle());
    }

    return menuBuilder;
  }

  private MenuBuilder setMenuOrder(MenuBuilder menuBuilder, String order) {

    Integer seq = null;
    if (order != null) {
      try {
        seq = Integer.parseInt(order);
      } catch (NumberFormatException e) {
        seq = null;
      }
    }

    if (seq != null) {
      menuBuilder.setOrder(seq);
    } else {
      menuBuilder.setOrder(getMenuOrder(menuBuilder));
    }

    return menuBuilder;
  }

  private int getMenuOrder(MenuBuilder menuBuilder) {

    Integer seq = 0;
    MetaMenu parentMenu = menuBuilder.getParentMenu();
    if (parentMenu != null) {
      Long menuId = parentMenu.getId();
      if (menuSeqMap.containsKey(menuId)) {
        seq = menuSeqMap.get(menuId);
      }
      menuSeqMap.put(menuId, seq + 1);
      return seq;
    }

    seq = parentMenuSeq;

    parentMenuSeq++;

    return seq;
  }
}
