package com.axelor.studio.service.module;

import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.builder.ActionBuilderService;
import com.axelor.studio.service.builder.CalendarBuilderService;
import com.axelor.studio.service.builder.CardsBuilderService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.axelor.studio.service.builder.GridBuilderService;
import com.axelor.studio.service.builder.KanbanBuilderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleExportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String DOMAIN_DIR = "src/main/resources/domains/";

  public static final String VIEW_DIR = "src/main/resources/views/";

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private ViewBuilderRepository viewBuilderRepo;

  @Inject private MetaModuleRepository metaModuleRepo;

  @Inject private ModelBuilderService modelBuilderService;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private KanbanBuilderService kanbanBuilderService;

  @Inject private CalendarBuilderService calendarBuilderService;

  @Inject private CardsBuilderService cardsBuilderService;

  @Inject private FormBuilderService formBuilderService;

  @Inject private GridBuilderService gridBuilderService;

  @Inject private ActionBuilderService actionBuilderService;

  @Inject private MetaFiles metaFiles;

  @Inject private MetaFileRepository metaFileRepo;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Transactional
  public MetaFile export(String module)
      throws AxelorException, ZipException, IOException, JAXBException {

    List<MetaJsonModel> jsonModels = metaJsonModelRepo.all().filter("self.isReal = true").fetch();
    List<MetaModel> metaModels = metaModelRepo.all().filter("self.isReal = true").fetch();
    List<ViewBuilder> viewBuilders = viewBuilderRepo.all().fetch();

    if (jsonModels.isEmpty() && metaModels.isEmpty() && viewBuilders.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(IExceptionMessage.NO_MODULE_DATA));
    }

    File zipFile = MetaFiles.createTempFile(module, ".zip").toFile();
    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

    addBuildGradle(module, zipOut);

    Map<String, ObjectViews> viewMap = new HashMap<>();

    addJsonModel(module, jsonModels, zipOut, viewMap);
    addMetaModel(module, metaModels, zipOut, viewMap);
    addMenu(module, jsonModels, viewMap);
    addView(module, viewBuilders, zipOut, viewMap);

    Beans.get(ModuleExportDataInitService.class).exportDataInit(module, zipOut);

    zipOut.close();

    MetaFile metaFile = metaFiles.upload(zipFile);
    metaFile.setFileName(module + ".zip");
    metaFileRepo.save(metaFile);

    return metaFile;
  }

  private void addBuildGradle(String module, ZipOutputStream zipOut) throws IOException {

    StringBuilder builder = new StringBuilder();
    builder.append("apply plugin: \"com.axelor.app-module\"\n\n");
    builder.append("spotless { \n" + "	java {  \n" + "		googleJavaFormat()\n" + "	} \n" + "}\n\n");
    builder.append("axelor {\n" + "	title \"" + module + "\"\n" + "}\n\n");
    builder.append("dependencies {\n");

    List<MetaModule> metaModules =
        metaModuleRepo
            .all()
            .filter(
                "self.name != ?1 and self.application = false and self.name NOT IN ('axelor-web','axelor-core')",
                module)
            .fetch();
    for (MetaModule metaModule : metaModules) {
      builder.append("	compile project(\":modules:" + metaModule.getName() + "\")\n");
    }

    builder.append("}");

    addZipEntry("build.gradle", builder.toString(), zipOut);
  }

  public void addZipEntry(String fileName, String fileText, ZipOutputStream zipOut)
      throws IOException {
    if (fileText != null) {
      addZipEntry(fileName, fileText.getBytes(), zipOut);
    }
  }

  public void addZipEntry(String fileName, File file, ZipOutputStream zipOut) throws IOException {
    if (file != null) {
      addZipEntry(fileName, Files.readAllBytes(file.toPath()), zipOut);
    }
  }

  private void addZipEntry(String fileName, byte[] data, ZipOutputStream zipOut)
      throws IOException {

    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    zipOut.write(data);
    zipOut.closeEntry();
  }

  private void addJsonModel(
      String module,
      List<MetaJsonModel> jsonModels,
      ZipOutputStream zipOut,
      Map<String, ObjectViews> viewMap)
      throws AxelorException, IOException, JAXBException {

    for (MetaJsonModel model : jsonModels) {
      String fileText = modelBuilderService.build(model, module);
      updateViewMap(model.getName(), viewMap, formBuilderService.build(model, module));
      updateViewMap(model.getName(), viewMap, gridBuilderService.build(model, module));
      addActions(model.getName(), module, model.getFields(), viewMap);
      addSelection(model.getFields(), viewMap);
      addZipEntry(DOMAIN_DIR + model.getName() + ".xml", fileText, zipOut);
    }
  }

  private void addMetaModel(
      String module,
      List<MetaModel> metaModels,
      ZipOutputStream zipOut,
      Map<String, ObjectViews> viewMap)
      throws AxelorException, IOException, JAXBException {

    for (MetaModel model : metaModels) {
      List<MetaJsonField> fields =
          metaJsonFieldRepo.all().filter("self.model = ?1", model.getFullName()).fetch();
      String fileText = modelBuilderService.build(model, fields, module);
      addZipEntry(DOMAIN_DIR + model.getName() + ".xml", fileText, zipOut);
      updateViewMap(model.getName(), viewMap, formBuilderService.build(model, module));
      addActions(model.getName(), module, fields, viewMap);
      addSelection(fields, viewMap);
      //      updateViewMap(model.getName(), viewMap, gridBuilderService.build(model, module));
    }
  }

  private void updateViewMap(String model, Map<String, ObjectViews> viewMap, AbstractView view) {

    if (view == null) {
      return;
    }

    if (!viewMap.containsKey(model)) {
      ObjectViews objectViews = new ObjectViews();
      objectViews.setViews(new ArrayList<>());
      viewMap.put(model, objectViews);
    }

    viewMap.get(model).getViews().add(view);
  }

  private void addView(
      String module,
      List<ViewBuilder> viewBuilders,
      ZipOutputStream zipOut,
      Map<String, ObjectViews> viewMap)
      throws AxelorException, IOException, JAXBException {

    for (ViewBuilder viewBuilder : viewBuilders) {

      AbstractView view = null;
      switch (viewBuilder.getViewType()) {
        case "kanban":
          view = kanbanBuilderService.build(viewBuilder, module);
          break;
        case "calendar":
          view = calendarBuilderService.build(viewBuilder, module);
          break;
        case "cards":
          view = cardsBuilderService.build(viewBuilder, module);
          break;
      }

      updateViewMap(viewBuilder.getModel(), viewMap, view);
    }

    for (String model : viewMap.keySet()) {
      ObjectViews objectViews = viewMap.get(model);
      if (objectViews == null) {
        continue;
      }
      addZipEntry(VIEW_DIR + model + ".xml", viewBuilderService.createXml(objectViews), zipOut);
    }
  }

  private void addMenu(
      String module, List<MetaJsonModel> jsonModels, Map<String, ObjectViews> viewMap)
      throws JAXBException {

    ObjectViews objectViews = new ObjectViews();
    objectViews.setMenus(new ArrayList<>());
    objectViews.setActions(new ArrayList<>());
    viewMap.put("Menu", objectViews);

    for (MetaJsonModel jsonModel : jsonModels) {
      MetaMenu menu = jsonModel.getMenu();
      if (menu == null) {
        continue;
      }
      if (menu.getParent() != null) {
        addMenu(jsonModel.getName(), null, menu.getParent(), viewMap);
      }
      addMenu(module, jsonModel.getName(), menu, viewMap);
    }

    if (objectViews.getMenus().isEmpty()) {
      viewMap.remove("Menu");
    }
  }

  private void addMenu(
      String module, String simpleModel, MetaMenu metaMenu, Map<String, ObjectViews> viewMap)
      throws JAXBException {

    String model = modelBuilderService.getModelFullName(module, simpleModel);

    MenuItem menu = new MenuItem();
    menu.setName(metaMenu.getName().replaceAll("json-model", module));
    menu.setTitle(metaMenu.getTitle());
    menu.setOrder(metaMenu.getOrder());
    menu.setParent(metaMenu.getParent() != null ? metaMenu.getParent().getName() : null);
    menu.setIcon(metaMenu.getIcon());
    menu.setIconBackground(metaMenu.getIconBackground());
    menu.setTop(metaMenu.getTop());
    menu.setXmlId(module + "-" + menu.getName());
    menu.setConditionToCheck(metaMenu.getConditionToCheck());
    menu.setModuleToCheck(metaMenu.getModuleToCheck());

    MetaAction metaAction = metaMenu.getAction();
    if (model != null && metaAction != null) {
      String name = metaAction.getName().replace("all.json", module.replace("-", "."));
      ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
      if (objectViews != null) {
        ActionView action = (ActionView) objectViews.getActions().get(0);
        ActionViewBuilder builder = ActionView.define(action.getTitle());
        builder.model(model);
        builder.domain(null);
        for (View view : action.getViews()) {
          builder.add(
              view.getType(), viewBuilderService.getDefaultViewName(view.getType(), simpleModel));
        }
        ActionView actionView = builder.get();
        actionView.setName(name);
        actionView.setXmlId(module + "." + name);
        viewMap.get("Menu").getActions().add(actionView);
      }
      menu.setAction(name);
    }

    viewMap.get("Menu").getMenus().add(menu);
  }

  public void addActions(
      String model, String module, List<MetaJsonField> fields, Map<String, ObjectViews> viewMap)
      throws AxelorException, JAXBException {

    ObjectViews views = viewMap.get(model);

    List<Action> actions = actionBuilderService.build(fields, module);

    views.setActions(actions);
  }

  public void addSelection(List<MetaJsonField> fields, Map<String, ObjectViews> viewMap)
      throws AxelorException, JAXBException {

    List<Selection> selections = new ArrayList<>();

    for (MetaJsonField field : fields) {
      if (field.getIsWkf() && field.getSelection() != null) {
        List<Option> options = MetaStore.getSelectionList(field.getSelection());
        if (!ObjectUtils.isEmpty(options)) {
          Selection selection = new Selection();
          selection.setName(field.getSelection());
          selection.setOptions(options);
          selections.add(selection);
        }
      }
    }

    if (!selections.isEmpty()) {
      ObjectViews views = viewMap.get("Selects");
      if (views == null) {
        views = new ObjectViews();
        views.setSelections(new ArrayList<>());
        viewMap.put("Selects", views);
      }
      views.getSelections().addAll(selections);
    }
  }
}
