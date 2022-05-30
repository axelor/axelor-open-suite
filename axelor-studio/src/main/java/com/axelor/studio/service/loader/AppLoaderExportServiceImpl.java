/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.loader;

import com.axelor.apps.tool.context.FullContext;
import com.axelor.apps.tool.context.FullContextHelper;
import com.axelor.common.Inflector;
import com.axelor.common.ResourceUtils;
import com.axelor.data.XStreamUtils;
import com.axelor.data.xml.XMLBind;
import com.axelor.data.xml.XMLBindJson;
import com.axelor.data.xml.XMLConfig;
import com.axelor.data.xml.XMLInput;
import com.axelor.db.JpaSecurity;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.axelor.studio.db.AppDataLoader;
import com.axelor.studio.db.AppLoader;
import com.axelor.studio.db.repo.AppLoaderRepository;
import com.axelor.text.GroovyTemplates;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.thoughtworks.xstream.XStream;
import groovy.xml.XmlUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLoaderExportServiceImpl implements AppLoaderExportService {

  private static final String[] EXPORT_TEMPLATES =
      new String[] {
        "app-builder",
        "json-model",
        "json-field",
        "menu-builder",
        "action-builder",
        "dashboard-builder",
        "dashlet-builder",
        "chart-builder",
        "selection-builder"
      };

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final List<String> JSON_EXTRACT_TYPES =
      Arrays.asList(new String[] {"integer", "decimal", "boolean", "text"});

  @Inject protected MetaFiles metaFiles;

  @Inject protected AppLoaderRepository appLoaderRepository;

  @Inject protected MetaJsonRecordRepository metaJsonRecordRepository;

  @Inject protected JpaSecurity jpaSecurity;

  @Inject protected MetaJsonModelRepository metaJsonModelRepository;

  @Override
  @Transactional
  public void exportApps(AppLoader appLoader) {

    if (CollectionUtils.isEmpty(appLoader.getExportedAppBuilderSet())) {
      return;
    }

    try {

      File exportDir = Files.createTempDir();

      addAppDataFile(appLoader, exportDir);

      if (CollectionUtils.isNotEmpty(appLoader.getAppDataLoaderList())) {

        addImportConfigFile(appLoader, exportDir);

        for (AppDataLoader dataLoader : appLoader.getAppDataLoaderList()) {

          if (dataLoader.getIsJson()) {
            addJsonModelData(dataLoader, exportDir);
          } else {
            addMetaModelData(dataLoader, exportDir);
          }
        }
      }

      File zipFile = createExportZip(exportDir);

      if (appLoader.getExportMetaFile() != null) {
        metaFiles.upload(zipFile, appLoader.getExportMetaFile());
      } else {
        appLoader.setExportMetaFile(metaFiles.upload(zipFile));
      }

      appLoader.setExportedOn(LocalDateTime.now());
      appLoaderRepository.save(appLoader);

    } catch (IOException | ClassNotFoundException e) {
      TraceBackService.trace(e);
    }
  }

  public Map<String, Object> getExportContext(AppLoader appLoader) {

    List<Long> ids =
        appLoader.getExportedAppBuilderSet().stream()
            .map(it -> it.getId())
            .collect(Collectors.toList());

    Map<String, Object> ctx = new HashMap<>();
    ctx.put("__ids__", ids);

    return ctx;
  }

  protected Map<String, InputStream> getExportTemplateResources() {

    Map<String, InputStream> templateMap = new HashMap<String, InputStream>();

    for (String filePrefix : EXPORT_TEMPLATES) {
      templateMap.put(
          filePrefix + ".xml",
          ResourceUtils.getResourceStream("data-export/" + filePrefix + ".tmpl"));
    }

    return templateMap;
  }

  protected File addImportConfigFile(AppLoader appLoader, File parentDir)
      throws IOException, FileNotFoundException, ClassNotFoundException {

    File configFile = new File(parentDir, "data-config.xml");

    XMLConfig xmlConfig = new XMLConfig();

    for (AppDataLoader dataLoader : appLoader.getAppDataLoaderList()) {
      xmlConfig.getInputs().add(createInput(dataLoader, false));
      xmlConfig.getInputs().add(createInput(dataLoader, true));
    }

    writeXmlConfig(configFile, xmlConfig);

    return configFile;
  }

  @Override
  public void writeXmlConfig(File configFile, XMLConfig xmlConfig) throws IOException {

    XStream xStream = XStreamUtils.createXStream();
    xStream.autodetectAnnotations(true);
    xStream.omitField(XMLBind.class, "bindingsLinked");
    xStream.omitField(XMLBindJson.class, "initialized");

    FileWriter fout = new FileWriter(configFile);
    xStream.toXML(xmlConfig, fout);
    fout.close();
  }

  private XMLInput createInput(AppDataLoader dataLoader, boolean relationalInput)
      throws ClassNotFoundException {

    XMLInput xmlInput;

    if (dataLoader.getIsJson()) {
      xmlInput = createJsonModelInput(dataLoader, relationalInput);
    } else {
      xmlInput = createMetaModelInput(dataLoader, relationalInput);
    }

    return xmlInput;
  }

  protected XMLInput createMetaModelInput(AppDataLoader dataLoader, boolean relationalInput)
      throws ClassNotFoundException {

    XMLInput xmlInput = createXmlInput(dataLoader);
    Mapper modelMapper = Mapper.of(Class.forName(dataLoader.getMetaModel().getFullName()));

    String nodeName = getBindNodeName(xmlInput.getRoot());

    XMLBind xmlBind = new XMLBind();
    xmlBind.setTypeName(dataLoader.getMetaModel().getFullName());
    xmlBind.setNode(nodeName);
    if (!CollectionUtils.isEmpty(dataLoader.getSearchMetaFieldSet())) {
      xmlBind.setSearch(getMetaSearchFields(modelMapper, dataLoader));
      xmlBind.setUpdate(true);
    }

    if (relationalInput) {
      xmlBind.setCreate(false);
    }

    xmlBind.setBindings(getMetaFieldBinding(modelMapper, dataLoader, relationalInput));

    List<XMLBind> rootBindings = new ArrayList<XMLBind>();
    rootBindings.add(xmlBind);
    xmlInput.setBindings(rootBindings);

    return xmlInput;
  }

  private String getBindNodeName(String root) {
    return root.substring(0, root.length() - 1);
  }

  protected XMLInput createXmlInput(AppDataLoader dataLoader) {

    XMLInput xmlInput = new XMLInput();
    String modelName = dataLoader.getModelName();
    xmlInput.setFileName(modelName + ".xml");
    String dasherizeModel = Inflector.getInstance().dasherize(modelName);
    xmlInput.setRoot(dasherizeModel + "s");

    return xmlInput;
  }

  protected XMLInput createJsonModelInput(AppDataLoader dataLoader, boolean relationalInput) {

    XMLInput xmlInput = createXmlInput(dataLoader);
    Map<String, Object> jsonFieldMap = MetaStore.findJsonFields(dataLoader.getModelName());
    fixTargetName(jsonFieldMap);

    String nodeName = getBindNodeName(xmlInput.getRoot());

    XMLBindJson xmlBindJson = new XMLBindJson();
    xmlBindJson.setNode(nodeName);
    xmlBindJson.setJsonModel(dataLoader.getModelName());

    if (!CollectionUtils.isEmpty(dataLoader.getSearchJsonFieldSet())) {
      xmlBindJson.setSearch(getJsonSearchFields(dataLoader, jsonFieldMap));
      xmlBindJson.setUpdate(true);
    }

    if (relationalInput) {
      xmlBindJson.setCreate(false);
    }

    xmlBindJson.setBindings(geJsonFieldBinding(jsonFieldMap, dataLoader, relationalInput));

    List<XMLBind> rootBindings = new ArrayList<XMLBind>();
    rootBindings.add(xmlBindJson);
    xmlInput.setBindings(rootBindings);

    return xmlInput;
  }

  protected void addJsonModelData(AppDataLoader dataLoader, File parentDir) {

    try {

      if (!allowRead(MetaJsonRecord.class)) {
        return;
      }

      String modelName = dataLoader.getJsonModel().getName();
      String dasherizeModel = Inflector.getInstance().dasherize(modelName);
      File dataFile = new File(parentDir, modelName + ".xml");

      FileWriter fileWriter = createHeader(dasherizeModel, dataFile);

      List<FullContext> records = FullContextHelper.filter(modelName, dataLoader.getFilterQuery());

      Map<String, Object> jsonFieldMap = MetaStore.findJsonFields(modelName);

      fixTargetName(jsonFieldMap);

      for (FullContext record : records) {
        if (!allowRead(MetaJsonRecord.class, (Long) record.get("id"))) {
          continue;
        }
        fileWriter.write("<" + dasherizeModel + ">\n");

        for (MetaJsonField jsonField : dataLoader.getJsonFieldSet()) {
          String field = jsonField.getName();
          Map<String, Object> fieldAttrs = (Map<String, Object>) jsonFieldMap.get(field);
          fileWriter.write(
              "\t<"
                  + field
                  + ">"
                  + extractJsonFieldValue(record, fieldAttrs)
                  + "</"
                  + field
                  + ">\n");
        }

        fileWriter.write("</" + dasherizeModel + ">\n\n");
      }

      fileWriter.write("</" + dasherizeModel + "s>\n");

      fileWriter.close();

    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }

  private void fixTargetName(Map<String, Object> jsonFieldMap) {

    // NOTE: Issue in AOP, it always return name as targetname for custom model.
    for (String field : jsonFieldMap.keySet()) {
      Map<String, Object> fieldAttrs = (Map<String, Object>) jsonFieldMap.get(field);
      String targetModel = (String) fieldAttrs.get("jsonTarget");

      if (targetModel != null) {
        MetaJsonField nameField =
            metaJsonModelRepository.findNameField(metaJsonModelRepository.findByName(targetModel));
        String targetName = "id";
        if (nameField != null) {
          targetName = nameField.getName();
        }
        ((Map<String, Object>) jsonFieldMap.get(field)).put("targetName", targetName);
      }
    }
  }

  protected FileWriter createHeader(String dasherizeModel, File dataFile) throws IOException {

    FileWriter fileWriter = new FileWriter(dataFile);
    fileWriter.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    fileWriter.write(
        "<"
            + dasherizeModel
            + "s xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n\n");

    return fileWriter;
  }

  protected void addMetaModelData(AppDataLoader dataLoader, File parentDir) {

    try {
      Class klass = Class.forName(dataLoader.getMetaModel().getFullName());
      if (!allowRead(klass)) {
        return;
      }
      Mapper modelMapper = Mapper.of(klass);

      String modelName = dataLoader.getMetaModel().getName();
      String dasherizeModel = Inflector.getInstance().dasherize(modelName);
      File dataFile = new File(parentDir, modelName + ".xml");

      FileWriter fileWriter = createHeader(dasherizeModel, dataFile);

      List<FullContext> records = FullContextHelper.filter(modelName, dataLoader.getFilterQuery());

      for (FullContext record : records) {

        if (!allowRead(klass, (Long) record.get("id"))) {
          continue;
        }

        fileWriter.write("<" + dasherizeModel + ">\n");

        for (MetaField metaField : dataLoader.getMetaFieldSet()) {
          String field = metaField.getName();
          fileWriter.write(
              "\t<"
                  + field
                  + ">"
                  + extractMetaFieldValue(record, modelMapper.getProperty(field))
                  + "</"
                  + field
                  + ">\n");
        }

        fileWriter.write("</" + dasherizeModel + ">\n\n");
      }

      fileWriter.write("</" + dasherizeModel + "s>\n");

      fileWriter.close();

    } catch (IOException | ClassNotFoundException e) {
      TraceBackService.trace(e);
    }
  }

  protected Object extractJsonFieldValue(FullContext record, Map<String, Object> fieldAttrs) {

    Object value = record.get(fieldAttrs.get("jsonPath"));

    if (value != null) {
      if (value instanceof FullContext) {
        value = ((FullContext) value).get(getTargetName(fieldAttrs));
      } else if (value instanceof Collection) {
        Collection<Object> records = (Collection<Object>) value;
        StringBuilder stringBuilder = new StringBuilder();
        for (Object rec : records) {
          if (stringBuilder.length() > 0) {
            stringBuilder.append(";");
          }
          Object target = ((FullContext) rec).get(getTargetName(fieldAttrs));
          if (target != null) {
            target = target.toString().replace(";", ",");
            stringBuilder.append(target);
          }
        }
        value = stringBuilder.toString();
      }
    }

    if (value instanceof String) {
      value = XmlUtil.escapeXml((String) value);
    }

    if (value == null) {
      value = "";
    }

    return value;
  }

  private String getTargetName(Map<String, Object> fieldAttrs) {

    String targetName = (String) fieldAttrs.get("targetName");
    if (Strings.isNullOrEmpty(targetName)) {
      targetName = "id";
    }

    return targetName;
  }

  protected Object extractMetaFieldValue(FullContext fullContext, Property property) {

    Object value = fullContext.get(property.getName());
    String target = property.getTargetName();
    if (target == null) {
      target = "id";
    }
    if (value != null && property.isReference()) {
      if (property.isCollection()) {

        StringBuilder valBuilder = new StringBuilder();

        for (Object val : (Collection<Object>) value) {
          Object recVal = ((FullContext) val).get(target);
          if (recVal != null) {
            recVal = recVal.toString().replace(";", ",");
          }
          if (valBuilder.length() > 0) {
            valBuilder.append(";");
          }
          valBuilder.append(recVal);
        }

        value = valBuilder.toString();
      } else {
        value = ((FullContext) value).get(property.getTargetName());
      }
    }

    if (value instanceof String) {
      value = XmlUtil.escapeXml((String) value);
    }

    if (value == null) {
      value = "";
    }

    return value;
  }

  protected File createExportZip(File exportDir) throws IOException, FileNotFoundException {

    File zipFile = MetaFiles.createTempFile("app-", ".zip").toFile();

    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
    for (File file : exportDir.listFiles()) {
      ZipEntry zipEntry = new ZipEntry(file.getName());
      zipOut.putNextEntry(zipEntry);
      Files.copy(file, zipOut);
    }
    zipOut.close();

    return zipFile;
  }

  protected void addAppDataFile(AppLoader appLoader, File exportDir) throws IOException {

    Map<String, InputStream> inputStreams = getExportTemplateResources();
    GroovyTemplates templates = new GroovyTemplates();

    for (String xmlFileName : inputStreams.keySet()) {
      log.debug("Exporting file: {}", xmlFileName);
      File file = new File(exportDir, xmlFileName);
      FileWriter writer = new FileWriter(file);
      Map<String, Object> ctx = getExportContext(appLoader);
      templates.from(new InputStreamReader(inputStreams.get(xmlFileName))).make(ctx).render(writer);
      writer.close();
      if (file.length() == 0) {
        file.delete();
      } else {
        long lines = java.nio.file.Files.lines(file.toPath()).count();
        if (lines == 1) {
          file.delete();
        }
      }
    }
  }

  protected boolean allowRead(Class klass, Long... ids) {

    return jpaSecurity.isPermitted(JpaSecurity.CAN_READ, klass, ids);
  }

  protected String getMetaSearchFields(Mapper modelMapper, AppDataLoader appDataLoader)
      throws ClassNotFoundException {

    StringBuilder fields = new StringBuilder();

    for (MetaField field : appDataLoader.getSearchMetaFieldSet()) {

      if (fields.length() != 0) {
        fields.append(" AND ");
      }

      if (field.getRelationship() != null) {
        Property property = modelMapper.getProperty(field.getName());
        fields.append(
            "self."
                + field.getName()
                + "."
                + property.getTargetName()
                + " =  :_"
                + field.getName());
      } else {
        fields.append("self." + field.getName() + " =  :_" + field.getName());
      }
    }

    return fields.toString();
  }

  private List<XMLBind> getMetaFieldBinding(
      Mapper modelMapper, AppDataLoader dataLoader, boolean relationalInput) {

    List<XMLBind> fieldBindings = new ArrayList<XMLBind>();

    for (MetaField field : dataLoader.getMetaFieldSet()) {

      fieldBindings.add(createDummyFieldBinding(field.getName()));

      if (relationalInput && field.getRelationship() == null) {
        continue;
      }

      XMLBind fieldBind = new XMLBind();
      fieldBind.setField(field.getName());
      fieldBind.setNode(field.getName());

      if (field.getRelationship() != null) {
        addRelationalMetaFieldBind(modelMapper, field, fieldBind);
      } else if (field.getTypeName().equals("Boolean")) {
        fieldBind.setAdapter("Boolean");
      }

      fieldBindings.add(fieldBind);
    }

    return fieldBindings;
  }

  private XMLBind createDummyFieldBinding(String name) {

    XMLBind dummyBind = new XMLBind();
    dummyBind.setNode(name);
    dummyBind.setField("_" + name);

    return dummyBind;
  }

  private void addRelationalMetaFieldBind(Mapper modelMapper, MetaField field, XMLBind fieldBind) {

    Property property = modelMapper.getProperty(field.getName());
    if (property.isCollection()) {
      fieldBind.setSearch("self." + property.getTargetName() + " in :_" + field.getName());
      fieldBind.setExpression(field.getName() + "?.split(';') as List");
    } else {
      fieldBind.setSearch("self." + property.getTargetName() + " = :_" + field.getName());
    }
    fieldBind.setUpdate(true);
    fieldBind.setCreate(false);
  }

  protected String getJsonSearchFields(
      AppDataLoader appDataLoader, Map<String, Object> jsonFieldMap) {

    StringBuilder fields = new StringBuilder();

    for (MetaJsonField field : appDataLoader.getSearchJsonFieldSet()) {

      if (fields.length() != 0) {
        fields.append(" AND ");
      }
      Map<String, Object> fieldAttrs = (Map<String, Object>) jsonFieldMap.get(field.getName());
      log.debug("Json search Field name: {}, Field attrs: {}", field, fieldAttrs);
      String jsonFunction = "json_extract_text";

      if (field.getTargetJsonModel() != null || field.getTargetModel() != null) {
        String targetName = getTargetName(fieldAttrs);
        if (targetName.equals("id")) {
          jsonFunction = "json_extract_integer";
        }
        fields.append(
            jsonFunction + "(self.attrs," + "'" + targetName + "') = :_" + field.getName());
      } else {
        if (JSON_EXTRACT_TYPES.contains(field.getType())) {
          jsonFunction = "json_extract_" + field.getType();
        }
        fields.append(
            jsonFunction + "(self.attrs,'" + field.getName() + "') = :_" + field.getName());
      }
    }

    return fields.toString();
  }

  protected List<XMLBind> geJsonFieldBinding(
      Map<String, Object> jsonFieldMap, AppDataLoader dataLoader, boolean relationalInput) {

    List<XMLBind> fieldBindings = new ArrayList<>();

    for (MetaJsonField jsonField : dataLoader.getJsonFieldSet()) {
      Map<String, Object> fieldAttrs = (Map<String, Object>) jsonFieldMap.get(jsonField.getName());
      log.debug("Json Field name: {}, Field attrs: {}", jsonField, fieldAttrs);
      String fieldType = jsonField.getType();

      fieldBindings.add(createDummyFieldBinding(jsonField.getName()));

      if (relationalInput
          && jsonField.getTargetJsonModel() == null
          && jsonField.getTargetModel() == null) {
        continue;
      }

      XMLBind xmlBind = new XMLBind();
      xmlBind.setNode(jsonField.getName());
      xmlBind.setField("$attrs." + jsonField.getName());
      if (jsonField.getTargetJsonModel() != null || jsonField.getTargetModel() != null) {
        addRelationaJsonFieldBind(jsonField, fieldAttrs, xmlBind);
      } else if (fieldType.equals("boolean")) {
        xmlBind.setAdapter("Boolean");
      }

      fieldBindings.add(xmlBind);
    }

    return fieldBindings;
  }

  private void addRelationaJsonFieldBind(
      MetaJsonField jsonField, Map<String, Object> fieldAttrs, XMLBind xmlBind) {

    String jsonFunction = "json_extract_text";
    String targetName = getTargetName(fieldAttrs);
    if (targetName.equals("id")) {
      jsonFunction = "json_extract_integer";
    }

    String operator = "=";
    if (jsonField.getType().contains("to-many")) {
      xmlBind.setExpression(jsonField.getName() + "?.split(';') as List");
      operator = "in";
    }

    if (jsonField.getType().startsWith("json-")) {
      xmlBind.setSearch(
          jsonFunction
              + "(self.attrs, '"
              + targetName
              + "') "
              + operator
              + " :_"
              + jsonField.getName());
    } else {
      xmlBind.setSearch("self." + targetName + " " + operator + " :_" + jsonField.getName());
    }

    xmlBind.setUpdate(true);
    xmlBind.setCreate(false);
  }
}
