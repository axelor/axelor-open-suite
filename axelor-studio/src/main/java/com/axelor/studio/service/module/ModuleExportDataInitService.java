package com.axelor.studio.service.module;

import com.axelor.apps.base.db.App;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.repo.AppBuilderRepository;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

public class ModuleExportDataInitService {

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private AppBuilderRepository appBuilderRepo;

  @Inject private ModuleExportService moduleExportService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;
  
  @Inject private ModelBuilderService modelBuilderService;

  private static final String[] APP_HEADER =
      new String[] {"code", "name", "description", "imagePath", "sequence", "depends"};
  
  private static final String REMOTE_SCHEMA = "data-import_" + CSVConfig.VERSION + ".xsd";

  private static final String[] JSON_HEADER =
      new String[] {
        "name",
        "title",
        "onNew",
        "onSave",
        "nameField",
        "menuParent.name",
        "menuIcon,menuBackGround",
        "menuOrder",
        "menuTitle",
        "menuTop",
        "appBuilder.code"
      };

  private static final String[] JSON_FIELD_HEADER =
      new String[] {
        "name",
        "title",
        "type",
        "model",
        "modelField",
        "jsonModel.name",
        "targetModel",
        "targetJsonModel.name",
        "appBuilder.code",
        "contextField",
        "contextFieldTarget",
        "contextFieldTargetName",
        "contextFieldTitle",
        "contextFieldValue",
        "domain",
        "enumType",
        "formView",
        "gridView",
        "help",
        "hidden",
        "hideIf",
        "includeIf",
        "isWkf",
        "maxSize",
        "minSize",
        "nameField",
        "onChange",
        "onClick",
        "precision",
        "readonly",
        "readonlyIf",
        "regex",
        "required",
        "requiredIf",
        "roleNames",
        "scale",
        "selection",
        "sequence",
        "showIf",
        "valueExpr",
        "visibleInGrid",
        "widget",
        "widgetAttrs"
      };

  private static final String dataInitDir = "src/main/resources/data-init/";

  public void addDataInit(String module, ZipOutputStream zipOut) throws IOException, AxelorException {

    String modulePrefix = getModulePrefix(module);
    
    CSVConfig csvConfig = new CSVConfig();
    csvConfig.setInputs(new ArrayList<>());
    
    addApps(module, zipOut, csvConfig);
    addAppBuilders(modulePrefix, zipOut, csvConfig);
    addJsonModels(modulePrefix, zipOut, csvConfig);
    addJsonFields(modulePrefix, zipOut, csvConfig);
    addInputConfig(csvConfig, zipOut);
    
  }

 private void addApps(String module, ZipOutputStream zipOut, CSVConfig csvConfig) throws IOException, AxelorException {

    List<AppBuilder> appBuilders = appBuilderRepo.all().filter("self.isReal = true").fetch();

    for (AppBuilder appBuilder : appBuilders) {
    	
      String fileName = getModulePrefix(module) + appBuilder.getCode() + ".csv";
      
      CSVInput input  = new CSVInput();
      input.setFileName(fileName);
      input.setSeparator(';');
      input.setTypeName(addAppEntity(module, appBuilder, zipOut));
      input.setCallable("com.axelor.csv.script.ImportApp:importApp");
      addAppDependsBind(input);
      csvConfig.getInputs().add(input);

      List<String[]> data = new ArrayList<>();

      createAppRecord(appBuilder, data, zipOut);

      addCsv(zipOut, fileName, APP_HEADER, data);
    }
  }

private void addAppDependsBind(CSVInput input) {
	
  input.setBindings(new ArrayList<>());
  
  CSVBind bind = new CSVBind();
  bind.setColumn("depends");
  bind.setField("dependsOnSet");
  bind.setSearch("self.code in :depends");
  bind.setExpression("depends.split('|') as List");
  input.getBindings().add(bind);
  
}

  private String addAppEntity(String module, AppBuilder appBuilder, ZipOutputStream zipOut) throws AxelorException, IOException {
	  
	  String model = "App" + appBuilder.getCode();
	  String xml = modelBuilderService.build(model, module, App.class.getName());
	  String filePath = ModuleExportService.DOMAIN_DIR + model + ".xml";
	  
	  moduleExportService.addZipEntry(filePath, xml, zipOut);
	  
	  return modelBuilderService.getModelFullName(module, model);
  }

  private void addCsv(ZipOutputStream zipOut, String fileName, String[] header, List<String[]> data)
      throws IOException {

    File file = MetaFiles.createTempFile(fileName.replace(".csv", ""), ".csv").toFile();
    CsvTool.csvWriter(file.getParent(), file.getName(), ';', '"', header, data);
    moduleExportService.addZipEntry(dataInitDir + "input/" + fileName, file, zipOut);
    
  }

  private String getModulePrefix(String module) {

    if (!module.contains("-")) {
      return module;
    }

    return module.substring(module.indexOf("-") + 1) + "_";
  }

  private void addAppBuilders(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig) throws IOException {

    List<AppBuilder> appBuilders = appBuilderRepo.all().filter("self.isReal = false").fetch();

    if (appBuilders.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();

    String fileName = modulePrefix + AppBuilder.class.getSimpleName() + ".csv";

    for (AppBuilder appBuilder : appBuilders) {
      createAppRecord(appBuilder, data, zipOut);
    }
    
    CSVInput input = new CSVInput();
    input.setFileName(fileName);
    input.setTypeName(AppBuilder.class.getName());
    input.setSeparator(';');
    input.setSearch("self.code = :code");
    addAppDependsBind(input);
    csvConfig.getInputs().add(input);
    
    addCsv(zipOut, fileName, APP_HEADER, data);
  }

  private String addImage(AppBuilder appBuilder, ZipOutputStream zipOut) throws IOException {

    File imageFile = MetaFiles.getPath(appBuilder.getImage()).toFile();
    String extension = FilenameUtils.getExtension(imageFile.getName());
    String imagePath = dataInitDir + "img/app-" + appBuilder.getCode() + "." + extension;
    moduleExportService.addZipEntry(imagePath, imageFile, zipOut);

    return "app-" + appBuilder.getCode();
  }

  private void createAppRecord(AppBuilder appBuilder, List<String[]> data, ZipOutputStream zipOut)
      throws IOException {

    String imageName = null;
    if (appBuilder.getImage() != null) {
      imageName = addImage(appBuilder, zipOut);
      ;
    }

    List<String> dependsOn =
        appBuilder.getDependsOnSet().stream().map(it -> it.getCode()).collect(Collectors.toList());

    String[] record =
        new String[] {
          appBuilder.getCode(),
          appBuilder.getName(),
          appBuilder.getDescription(),
          imageName,
          appBuilder.getSequence().toString(),
          Joiner.on("|").join(dependsOn)
        };

    data.add(record);
  }

  private void addJsonModels(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig) throws IOException {
    List<MetaJsonModel> jsonModels = metaJsonModelRepo.all().filter("self.isReal = false").fetch();

    if (jsonModels.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();
    for (MetaJsonModel jsonModel : jsonModels) {
      data.add(
          new String[] {
            jsonModel.getName(),
            jsonModel.getTitle(),
            jsonModel.getOnNew(),
            jsonModel.getOnSave(),
            jsonModel.getNameField(),
            jsonModel.getMenuParent() != null ? jsonModel.getMenuParent().getName() : null,
            jsonModel.getMenuIcon(),
            jsonModel.getMenuOrder().toString(),
            jsonModel.getMenuTitle(),
            jsonModel.getMenuTop().toString(),
            jsonModel.getAppBuilder() != null ? jsonModel.getAppBuilder().getCode() : null
          });
    }

    String fileName = modulePrefix + MetaJsonModel.class.getSimpleName() + ".csv";
    CSVInput input = new CSVInput();
    input.setFileName(fileName);
    input.setTypeName(MetaJsonModel.class.getName());
    input.setSeparator(';');
    input.setSearch("self.name = :name");
    csvConfig.getInputs().add(input);
    
    addCsv(zipOut, fileName, JSON_HEADER, data);
  }

  private void addJsonFields(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig) throws IOException {

    List<MetaJsonField> jsonFields =
        metaJsonFieldRepo
            .all()
            .filter(
                "self.jsonModel.isReal = false OR self.model IN (SELECT fullName FROM MetaModel WHERE isReal = false)")
            .fetch();

    if (jsonFields.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();
    for (MetaJsonField jsonField : jsonFields) {

      String roles = null;
      if (!jsonField.getRoles().isEmpty()) {
        List<String> roleNames =
            jsonField.getRoles().stream().map(it -> it.getName()).collect(Collectors.toList());
        roles = Joiner.on("|").join(roleNames);
      }

      data.add(
          new String[] {
            jsonField.getName(),
            jsonField.getTitle(),
            jsonField.getType(),
            jsonField.getModel(),
            jsonField.getModelField(),
            jsonField.getJsonModel() != null ? jsonField.getJsonModel().getName() : null,
            jsonField.getTargetModel(),
            jsonField.getTargetJsonModel() != null
                ? jsonField.getTargetJsonModel().getName()
                : null,
            jsonField.getAppBuilder() != null ? jsonField.getAppBuilder().getCode() : null,
            jsonField.getContextField(),
            jsonField.getContextFieldTarget(),
            jsonField.getContextFieldTargetName(),
            jsonField.getContextFieldTitle(),
            jsonField.getContextFieldValue(),
            jsonField.getDomain(),
            jsonField.getEnumType(),
            jsonField.getFormView(),
            jsonField.getGridView(),
            jsonField.getHelp(),
            jsonField.getHidden().toString(),
            jsonField.getHideIf(),
            jsonField.getIncludeIf(),
            jsonField.getIsWkf().toString(),
            jsonField.getMaxSize().toString(),
            jsonField.getMinSize().toString(),
            jsonField.getNameField().toString(),
            jsonField.getOnChange(),
            jsonField.getOnClick(),
            jsonField.getPrecision().toString(),
            jsonField.getReadonly().toString(),
            jsonField.getReadonlyIf(),
            jsonField.getRegex(),
            jsonField.getRequired().toString(),
            jsonField.getRequiredIf(),
            roles,
            jsonField.getScale().toString(),
            jsonField.getSelection(),
            jsonField.getSequence().toString(),
            jsonField.getShowIf(),
            jsonField.getValueExpr(),
            jsonField.getVisibleInGrid().toString(),
            jsonField.getWidget(),
            jsonField.getWidgetAttrs()
          });
    }
    
    String fileName = modulePrefix + MetaJsonField.class.getSimpleName() + ".csv";
    
    CSVInput input = new CSVInput();
    input.setFileName(fileName);
    input.setTypeName(MetaJsonField.class.getName());
    input.setSeparator(';');
    input.setSearch("self.name = :name and self.model = :model and self.modelField = :modelField");
    
    input.setBindings(new ArrayList<>());
    CSVBind bind = new CSVBind();
    bind.setColumn("roleNames");
    bind.setField("roles");
    bind.setSearch("self.name in :roleNames");
    bind.setExpression("roleNames.split('|') as List");
    input.getBindings().add(bind);
    
    csvConfig.getInputs().add(input);
    
    addCsv(zipOut, fileName, JSON_FIELD_HEADER, data);
  }
  
  
  private void addInputConfig(CSVConfig csvConfig, ZipOutputStream zipOut) throws IOException {
	  
	  XStream xStream = new XStream();
	  xStream.processAnnotations(CSVConfig.class);
	  String xml = prepareXML(xStream.toXML(csvConfig));
	  
	  moduleExportService.addZipEntry(dataInitDir + "input-config.xml", xml, zipOut);
	  
  }
  
  private String prepareXML(String xml) {
	
	xml = xml.replace("<csv-inputs>", "").replaceAll("</csv-inputs>", "");
	
    StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n");
    sb.append("<csv-inputs")
        .append(" xmlns='")
        .append(CSVConfig.NAMESPACE)
        .append("'")
        .append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
        .append(" xsi:schemaLocation='")
        .append(CSVConfig.NAMESPACE)
        .append(" ")
        .append(CSVConfig.NAMESPACE + "/" + REMOTE_SCHEMA)
        .append("'")
        .append(">\n\n")
        .append(xml)
        .append("\n\n</csv-inputs>");
	 
    return sb.toString();
  }
}
