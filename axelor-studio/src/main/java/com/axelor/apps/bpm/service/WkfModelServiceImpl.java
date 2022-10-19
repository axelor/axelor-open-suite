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
package com.axelor.apps.bpm.service;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.service.dashboard.WkfDashboardCommonService;
import com.axelor.apps.bpm.service.deployment.BpmDeploymentService;
import com.axelor.apps.bpm.translation.ITranslation;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WkfModelServiceImpl implements WkfModelService {

  private static final String IMPORT_CONFIG_PATH = "/data-import/import-wkf-models.xml";

  @Inject protected WkfModelRepository wkfModelRepository;

  @Inject private WkfDashboardCommonService wkfDashboardCommonService;

  @Inject private TranslationService translationService;

  @Override
  @Transactional
  public WkfModel createNewVersion(WkfModel wkfModel) {

    if (wkfModel == null) {
      return null;
    }

    WkfModel newVersion =
        wkfModelRepository
            .all()
            .filter("self.previousVersion.id = ?1", wkfModel.getId())
            .fetchOne();

    if (newVersion == null) {
      newVersion = wkfModelRepository.copy(wkfModel, true);
      newVersion.setPreviousVersion(wkfModel);
    }

    return wkfModelRepository.save(newVersion);
  }

  @Override
  @Transactional
  public WkfModel start(WkfModel wkfModel) {

    wkfModel.setStatusSelect(WkfModelRepository.STATUS_ON_GOING);

    if (wkfModel.getPreviousVersion() != null) {
      WkfModel previousVersion = wkfModel.getPreviousVersion();
      previousVersion.setIsActive(false);
      wkfModel.setPreviousVersion(terminate(previousVersion));
    }

    return wkfModelRepository.save(wkfModel);
  }

  @Override
  @Transactional
  public WkfModel terminate(WkfModel wkfModel) {

    wkfModel.setStatusSelect(WkfModelRepository.STATUS_TERMINATED);

    return wkfModelRepository.save(wkfModel);
  }

  @Override
  @Transactional
  public WkfModel backToDraft(WkfModel wkfModel) {

    wkfModel.setStatusSelect(WkfModelRepository.STATUS_NEW);

    return wkfModelRepository.save(wkfModel);
  }

  @Override
  public List<Long> findVersions(WkfModel wkfModel) {

    List<Long> wkfModelIds = new ArrayList<Long>();

    WkfModel previousModel = wkfModel.getPreviousVersion();

    while (previousModel != null) {
      wkfModelIds.add(previousModel.getId());
      previousModel = previousModel.getPreviousVersion();
    }

    return wkfModelIds;
  }

  @Override
  public void importStandardBPM() {

    List<App> appList = Beans.get(AppRepository.class).all().filter("self.active = true").fetch();

    if (CollectionUtils.isEmpty(appList)) {
      return;
    }

    String configFileName = "/data-import/import-wkf-models.xml";
    File configFile = null;
    try {
      configFile = File.createTempFile("config", ".xml");
      FileOutputStream fout = new FileOutputStream(configFile);
      InputStream inputStream = this.getClass().getResourceAsStream(configFileName);
      IOUtil.copyCompletely(inputStream, fout);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    if (configFile == null) {
      return;
    }

    try {
      String dataFileName = "/data-wkf-models/input/";
      File tempDir = Files.createTempDir();
      File dataFile = new File(tempDir, "wkfModels.xml");

      XMLImporter importer = getXMLImpoter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());

      for (App app : appList) {
        String fileName = dataFileName + app.getCode() + ".xml";
        InputStream dataInputStream = this.getClass().getResourceAsStream(fileName);
        if (dataInputStream == null) {
          continue;
        }

        FileOutputStream fout = new FileOutputStream(dataFile);
        IOUtil.copyCompletely(dataInputStream, fout);
        importer.run();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected XMLImporter getXMLImpoter(String configFile, String dataFile) {

    XMLImporter importer = new XMLImporter(configFile, dataFile);
    final StringBuilder log = new StringBuilder();
    Listener listner =
        new Listener() {

          @Override
          public void imported(Integer imported, Integer total) {}

          @Override
          public void imported(Model arg0) {
            WkfModel wkfModel = (WkfModel) arg0;
            Beans.get(BpmDeploymentService.class).deploy(wkfModel, null);
            wkfModel.setStatusSelect(WkfModelRepository.STATUS_ON_GOING);
          }

          @Override
          public void handle(Model arg0, Exception err) {
            log.append("Error in import: " + err.getStackTrace().toString());
          }
        };
    importer.addListener(listner);

    return importer;
  }

  @Override
  public String importWkfModels(
      MetaFile metaFile, boolean isTranslate, String sourceLanguage, String targetLanguage)
      throws AxelorException {

    if (metaFile == null) {
      return null;
    }

    String extension = Files.getFileExtension(metaFile.getFileName());
    if (extension == null || !extension.equals("xml")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ITranslation.INVALID_WKF_MODEL_XML));
    }
    try {
      InputStream inputStream = getClass().getResourceAsStream(IMPORT_CONFIG_PATH);
      File configFile = File.createTempFile("config", ".xml");
      FileOutputStream fout = new FileOutputStream(configFile);
      IOUtil.copyCompletely(inputStream, fout);

      File xmlFile = MetaFiles.getPath(metaFile).toFile();
      File tempDir = Files.createTempDir();
      File importFile = new File(tempDir, "wkfModels.xml");
      Files.copy(xmlFile, importFile);

      if (isTranslate) {
        importFile = this.translateNodeName(importFile, sourceLanguage, targetLanguage);
      }

      XMLImporter importer =
          new XMLImporter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());
      final StringBuilder log = new StringBuilder();
      Listener listner =
          new Listener() {

            @Override
            public void imported(Integer imported, Integer total) {}

            @Override
            public void imported(Model arg0) {}

            @Override
            public void handle(Model arg0, Exception err) {
              log.append("Error in import: " + err.getStackTrace().toString());
            }
          };

      importer.addListener(listner);

      importer.run();

      FileUtils.forceDelete(configFile);

      FileUtils.forceDelete(tempDir);

      FileUtils.forceDelete(xmlFile);

      MetaFileRepository metaFileRepository = Beans.get(MetaFileRepository.class);
      metaFileRepository.remove(metaFile);

      return log.toString();

    } catch (Exception e) {
      return e.getMessage();
    }
  }

  private File translateNodeName(File importFile, String sourceLanguage, String targetLanguage)
      throws Exception {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(importFile);
    doc.getDocumentElement().normalize();

    NodeList diagramNodeList = doc.getElementsByTagName("diagramXml");
    if (diagramNodeList.item(0) == null) {
      return importFile;
    }

    String diagramXml = diagramNodeList.item(0).getTextContent();
    String[] nodeNames = StringUtils.substringsBetween(diagramXml, "name=\"", "\"");
    for (String node : nodeNames) {
      String translationStr = translationService.getTranslationKey(node, sourceLanguage);
      translationStr = translationService.getTranslation(translationStr, targetLanguage);
      node = node.replace("$", "\\\\$");
      node = node.replace("{", "\\\\{");
      node = node.replace("}", "\\\\}");
      diagramXml = diagramXml.replaceAll(Pattern.quote(node), translationStr);
    }

    diagramNodeList.item(0).setTextContent(diagramXml);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(importFile.getPath()));
    transformer.transform(source, result);

    return importFile;
  }

  @SuppressWarnings({"serial", "unchecked"})
  @Override
  public List<Map<String, Object>> getProcessPerStatus(WkfModel wkfModel) {
    List<Map<String, Object>> dataList = new ArrayList<>();

    List<WkfProcess> processList = wkfDashboardCommonService.findProcesses(wkfModel, null);

    for (WkfProcess process : processList) {
      Map<String, Object> processMap = new HashMap<>();
      List<Map<String, Object>> configList = new ArrayList<>();

      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      wkfDashboardCommonService.sortProcessConfig(processConfigs);

      List<String> _modelList = new ArrayList<>();
      for (WkfProcessConfig processConfig : processConfigs) {

        final boolean isMetaModel = processConfig.getMetaModel() != null;
        final String modelName =
            isMetaModel
                ? processConfig.getMetaModel().getName()
                : processConfig.getMetaJsonModel().getName();

        if (_modelList.contains(modelName)) {
          continue;
        }
        _modelList.add(modelName);

        Map<String, Object> _map =
            wkfDashboardCommonService.computeStatus(isMetaModel, modelName, process, null, null);

        List<Long> recordIdsPerModel = (List<Long>) _map.get("recordIdsPerModel");
        List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statuses");
        Map<String, Object> taskMap = (Map<String, Object>) _map.get("tasks");

        configList.add(
            new HashMap<String, Object>() {
              {
                put("type", "model");
                put(
                    "title",
                    !StringUtils.isBlank(processConfig.getTitle())
                        ? processConfig.getTitle()
                        : modelName);
                put("modelName", modelName);
                put("modelRecordCount", recordIdsPerModel.size());
                put("isMetaModel", isMetaModel);
                put("recordIdsPerModel", recordIdsPerModel);
                put("statuses", statusList);
                put("tasks", taskMap);
              }
            });
      }

      processMap.put(
          "title",
          !StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      processMap.put("itemList", configList);

      dataList.add(processMap);
    }
    return dataList;
  }

  @SuppressWarnings({"serial", "unchecked"})
  @Override
  public List<Map<String, Object>> getProcessPerUser(WkfModel wkfModel) {
    User user = AuthUtils.getUser();
    List<Map<String, Object>> dataList = new ArrayList<>();

    List<WkfProcess> processList = wkfDashboardCommonService.findProcesses(wkfModel, null);

    for (WkfProcess process : processList) {
      Map<String, Object> processMap = new HashMap<>();
      List<Map<String, Object>> configList = new ArrayList<>();
      WkfProcessConfig firstProcessConfig = null;

      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      wkfDashboardCommonService.sortProcessConfig(processConfigs);

      int taskAssignedToMe = 0;
      List<String> _modelList = new ArrayList<>();
      for (WkfProcessConfig processConfig : processConfigs) {

        boolean isDirectCreation = processConfig.getIsDirectCreation();
        firstProcessConfig =
            firstProcessConfig == null
                ? processConfig.getIsStartModel() ? processConfig : null
                : firstProcessConfig;

        boolean isMetaModel = processConfig.getMetaModel() != null;
        String modelName =
            isMetaModel
                ? processConfig.getMetaModel().getName()
                : processConfig.getMetaJsonModel().getName();

        if (_modelList.contains(modelName)) {
          continue;
        }
        _modelList.add(modelName);

        Map<String, Object> _map =
            wkfDashboardCommonService.computeStatus(
                isMetaModel, modelName, process, user, WkfDashboardCommonService.ASSIGNED_ME);
        List<Long> recordIdsPerModel = (List<Long>) _map.get("recordIdsPerModel");

        List<Map<String, Object>> statusList = (List<Map<String, Object>>) _map.get("statuses");

        if (!statusList.isEmpty()) {
          taskAssignedToMe +=
              statusList.stream().map(s -> (int) s.get("statusCount")).reduce(0, Integer::sum);
        }

        Map<String, Object> taskMap = (Map<String, Object>) _map.get("tasks");

        configList.add(
            new HashMap<String, Object>() {
              {
                put("type", "model");
                put(
                    "title",
                    !StringUtils.isBlank(processConfig.getTitle())
                        ? processConfig.getTitle()
                        : modelName);
                put("modelName", modelName);
                put("modelRecordCount", recordIdsPerModel.size());
                put("isMetaModel", isMetaModel);
                put("recordIdsPerModel", recordIdsPerModel);
                put("statuses", statusList);
                put("tasks", taskMap);
              }
            });
        configList.add(
            new HashMap<String, Object>() {
              {
                put("type", "button");
                put("isDirectCreation", isDirectCreation);
                put("modelName", modelName);
                put("isMetaModel", isMetaModel);
              }
            });
      }

      processMap.put(
          "title",
          !StringUtils.isBlank(process.getDescription())
              ? process.getDescription()
              : process.getName());
      processMap.put("taskAssignedToMe", taskAssignedToMe);
      processMap.put("itemList", configList);
      processMap.put("processConfig", firstProcessConfig);

      dataList.add(processMap);
    }

    return dataList;
  }
}
