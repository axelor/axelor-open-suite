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
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessRepository;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.apps.bpm.translation.ITranslation;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.Query;
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

  private static final String TASK_TODAY = "taskToday";
  private static final String TASK_NEXT = "taskNext";
  private static final String LATE_TASK = "lateTask";

  @Inject protected WkfModelRepository wkfModelRepository;

  @Inject private WkfProcessRepository wkfProcessRepo;

  @Inject private WkfInstanceService wkfInstanceService;

  @Inject private WkfTaskConfigRepository wkfTaskConfigRepo;

  @Inject private MetaJsonRecordRepository metaJsonRecordRepo;

  @Inject private MetaModelRepository metaModelRepo;

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
          public void imported(Model arg0) {}

          @Override
          public void handle(Model arg0, Exception err) {
            log.append("Error in import: " + err.getStackTrace().toString());
          }
        };
    importer.addListener(listner);

    return importer;
  }

  private Query createCommonRecordQuery(String tableName, String condition, String status) {

    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append("SELECT record.id FROM " + tableName + " record ");
    queryBuilder.append(
        "LEFT JOIN act_hi_actinst activity ON record.process_instance_id = activity.proc_inst_id_ ");
    queryBuilder.append("WHERE (activity.act_name_ = :status OR activity.act_id_ = :status)");

    if (!StringUtils.isEmpty(condition)) {
      queryBuilder.append(" AND " + condition);
    }

    Query query = JPA.em().createNativeQuery(queryBuilder.toString());
    query.setParameter("status", status);
    return query;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getStatusPerMonthRecord(
      String tableName, String status, String month, String jsonModel) {

    String condition = "TO_CHAR(activity.start_time_,'yyyy-MM') = :month";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("month", month);

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getStatusPerDayRecord(
      String tableName, String status, String day, String jsonModel) {

    String condition = "TO_CHAR(activity.start_time_,'MM/dd/yyyy') = :day";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("day", day.toString());

    return query.getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Long> getTimespentPerStatusRecord(
      String tableName, String status, LocalDate fromDate, LocalDate toDate, String jsonModel) {

    String condition = "DATE(activity.start_time_) BETWEEN :fromDate AND :toDate";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);

    return query.getResultList();
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

    List<WkfProcess> processList = getProcesses(wkfModel);

    for (WkfProcess process : processList) {
      Map<String, Object> processMap = new HashMap<>();
      List<Map<String, Object>> configList = new ArrayList<>();

      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      this.sortProcessConfig(processConfigs);

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

        Map<String, Object> _map = this.computeStatus(isMetaModel, modelName, process, null);

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

  @Override
  public void sortProcessConfig(List<WkfProcessConfig> processConfigs) {
    processConfigs.sort(Comparator.comparing(WkfProcessConfig::getId));
  }

  @SuppressWarnings("serial")
  private Map<String, Object> computeStatus(
      boolean isMetaModel, String modelName, WkfProcess process, User user) {

    List<WkfTaskConfig> taskConfigs = getTaskConfigs(process, modelName, isMetaModel, user, true);

    Object obj[] = computeTaskConfig(taskConfigs, modelName, isMetaModel, user, true);

    return new HashMap<String, Object>() {
      {
        put("recordIdsPerModel", obj[0]);
        put("statuses", obj[1]);
        put("tasks", obj[2]);
      }
    };
  }

  @SuppressWarnings("serial")
  @Override
  public Object[] computeTaskConfig(
      List<WkfTaskConfig> taskConfigs,
      String modelName,
      boolean isMetaModel,
      User user,
      boolean withTask) {

    List<Map<String, Object>> statusList = new ArrayList<>();
    List<Long> recordIdsPerModel = new ArrayList<>();
    Map<String, Object> taskMap = new HashMap<String, Object>();

    taskConfigs.forEach(
        config -> {
          List<String> processInstanceIds =
              wkfInstanceService.findProcessInstanceByNode(
                  config.getName(), config.getProcessId(), config.getType(), false);

          List<Long> recordStatusIds = new ArrayList<>();

          if (!isMetaModel) {
            List<MetaJsonRecord> jsonModelrecords =
                this.getMetaJsonRecords(config, processInstanceIds, modelName, user, null);

            recordStatusIds.addAll(
                jsonModelrecords.stream()
                    .map(record -> record.getId())
                    .collect(Collectors.toList()));

            if (withTask) {
              this.getTasks(config, processInstanceIds, modelName, isMetaModel, user, taskMap);
            }

          } else {
            List<Model> metaModelRecords =
                this.getMetaModelRecords(config, processInstanceIds, modelName, user, null);

            recordStatusIds.addAll(
                metaModelRecords.stream()
                    .map(record -> record.getId())
                    .collect(Collectors.toList()));

            if (withTask) {
              this.getTasks(config, processInstanceIds, modelName, isMetaModel, user, taskMap);
            }
          }

          if (CollectionUtils.isNotEmpty(recordStatusIds)) {
            recordIdsPerModel.addAll(recordStatusIds);
            statusList.add(
                new HashMap<String, Object>() {
                  {
                    put(
                        "title",
                        !StringUtils.isBlank(config.getDescription())
                            ? config.getDescription()
                            : config.getName());
                    put("isMetaModel", isMetaModel);
                    put("modelName", modelName);
                    put("statusCount", recordStatusIds.size());
                    put("statusRecordIds", recordStatusIds);
                  }
                });
          }
        });

    if (withTask) {
      taskMap.put("isMetaModel", isMetaModel);
      taskMap.put("modelName", modelName);
    }

    return new Object[] {recordIdsPerModel, statusList, taskMap};
  }

  @SuppressWarnings({"serial", "unchecked"})
  @Override
  public List<Map<String, Object>> getProcessPerUser(WkfModel wkfModel) {
    User user = AuthUtils.getUser();
    List<Map<String, Object>> dataList = new ArrayList<>();

    List<WkfProcess> processList = getProcesses(wkfModel);

    for (WkfProcess process : processList) {
      Map<String, Object> processMap = new HashMap<>();
      List<Map<String, Object>> configList = new ArrayList<>();
      WkfProcessConfig firstProcessConfig = null;

      List<WkfProcessConfig> processConfigs = process.getWkfProcessConfigList();
      this.sortProcessConfig(processConfigs);

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

        Map<String, Object> _map = this.computeStatus(isMetaModel, modelName, process, user);
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

  @Override
  public List<WkfProcess> getProcesses(WkfModel wkfModel) {
    return wkfProcessRepo
        .all()
        .filter("self.wkfModel.id = ?", wkfModel.getId())
        .order("-id")
        .fetch();
  }

  private List<MetaJsonRecord> getMetaJsonRecords(
      WkfTaskConfig config,
      List<String> processInstanceIds,
      String modelName,
      User user,
      String type) {

    LocalDate toDate = LocalDate.now();
    String filter =
        "self.processInstanceId IN (:processInstanceIds) AND self.jsonModel = :jsonModel";

    String userPath = config.getUserPath();
    if (user != null) {
      if (Strings.isNullOrEmpty(userPath)) {
        return new ArrayList<>();
      }
      filter += " AND self.attrs." + userPath + ".id = '" + user.getId() + "'";
    }

    if (type != null) {
      String deadLinePath = config.getDeadlineFieldPath();
      if (Strings.isNullOrEmpty(deadLinePath)) {
        return new ArrayList<>();
      }
      filter += " AND self.attrs." + deadLinePath;
      switch (type) {
        case TASK_TODAY:
          filter += " = '" + toDate + "'";
          break;

        case TASK_NEXT:
          filter +=
              " > '"
                  + toDate
                  + "' AND self.attrs."
                  + config.getDeadlineFieldPath()
                  + " < '"
                  + toDate.plusDays(7)
                  + "'";
          break;

        case LATE_TASK:
          filter += " < '" + toDate + "'";
          break;
      }
    }

    return metaJsonRecordRepo
        .all()
        .filter(filter)
        .bind("processInstanceIds", processInstanceIds)
        .bind("jsonModel", modelName)
        .fetch();
  }

  @SuppressWarnings("unchecked")
  private Object[] getMetaModelRecordFilter(WkfTaskConfig config, String modelName, User user) {
    MetaModel metaModel = metaModelRepo.findByName(modelName);
    String model = metaModel.getFullName();
    String filter = "self.processInstanceId IN (:processInstanceIds)";
    Class<Model> klass = null;
    try {
      klass = (Class<Model>) Class.forName(model);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (user != null) {
      String path = config.getUserPath();
      Property property = Mapper.of(klass).getProperty(path.split("\\.")[0]);
      if (property == null) {
        filter += " AND self.attrs." + path + ".id = '" + user.getId() + "'";
      } else {
        filter += " AND self." + path + ".id = " + user.getId();
      }
    }
    return new Object[] {klass, filter};
  }

  @SuppressWarnings("unchecked")
  private List<Model> getMetaModelRecords(
      WkfTaskConfig config,
      List<String> processInstanceIds,
      String modelName,
      User user,
      String type) {

    LocalDate toDate = LocalDate.now();
    Object obj[] = this.getMetaModelRecordFilter(config, modelName, user);
    Class<Model> klass = (Class<Model>) obj[0];
    String filter = (String) obj[1];

    if (type != null) {
      String deadLinePath = config.getDeadlineFieldPath();
      if (Strings.isNullOrEmpty(deadLinePath)) {
        return new ArrayList<>();
      }
      Property property = Mapper.of(klass).getProperty(deadLinePath.split("\\.")[0]);
      if (property == null) {
        filter += " AND self.attrs." + deadLinePath;
        if (type.equals(TASK_NEXT)) {
          filter +=
              " > '"
                  + toDate
                  + "' AND self.attrs."
                  + deadLinePath
                  + " < '"
                  + toDate.plusDays(7)
                  + "'";
        }
      } else {
        filter += " AND self." + deadLinePath;
        if (type.equals(TASK_NEXT)) {
          filter +=
              " > '" + toDate + "' AND self." + deadLinePath + " < '" + toDate.plusDays(7) + "'";
        }
      }

      if (type.equals(TASK_TODAY)) {
        filter += " = '" + toDate + "'";
      } else if (type.equals(LATE_TASK)) {
        filter += " < '" + toDate + "'";
      }
    }

    return JPA.all(klass).filter(filter).bind("processInstanceIds", processInstanceIds).fetch();
  }

  @Override
  public List<WkfTaskConfig> getTaskConfigs(
      WkfProcess process, String modelName, boolean isMetaModel, User user, boolean withTask) {

    String filter = "self.processId = :processId";

    if (user != null) {
      filter += " AND self.userPath IS NOT NULL";
    } else if (!withTask) {
      filter += " AND self.userPath IS NULL";
    }

    if (user != null) {
      filter +=
          isMetaModel
              ? " AND self.modelName = '" + modelName + "'"
              : " AND self.jsonModelName = '" + modelName + "'";
    }

    return wkfTaskConfigRepo.all().filter(filter).bind("processId", process.getProcessId()).fetch();
  }

  @SuppressWarnings({"unchecked"})
  private void getTasks(
      WkfTaskConfig config,
      List<String> processInstanceIds,
      String modelName,
      boolean isMetaModel,
      User user,
      Map<String, Object> taskMap) {
    List<Long> taskTodayIds = new ArrayList<>();
    List<Long> taskNextIds = new ArrayList<>();
    List<Long> lateTaskIds = new ArrayList<>();

    if (!isMetaModel) {
      List<MetaJsonRecord> taskTodayList =
          this.getMetaJsonRecords(config, processInstanceIds, modelName, user, TASK_TODAY);
      taskTodayIds.addAll(taskTodayList.stream().map(t -> t.getId()).collect(Collectors.toList()));

      List<MetaJsonRecord> taskNextList =
          this.getMetaJsonRecords(config, processInstanceIds, modelName, user, TASK_NEXT);
      taskNextIds.addAll(taskNextList.stream().map(t -> t.getId()).collect(Collectors.toList()));

      List<MetaJsonRecord> lateTaskList =
          this.getMetaJsonRecords(config, processInstanceIds, modelName, user, LATE_TASK);
      lateTaskIds.addAll(lateTaskList.stream().map(t -> t.getId()).collect(Collectors.toList()));

    } else {
      List<Model> taskTodayList =
          this.getMetaModelRecords(config, processInstanceIds, modelName, user, TASK_TODAY);
      taskTodayIds.addAll(taskTodayList.stream().map(t -> t.getId()).collect(Collectors.toList()));

      List<Model> taskNextList =
          this.getMetaModelRecords(config, processInstanceIds, modelName, user, TASK_NEXT);
      taskNextIds.addAll(taskNextList.stream().map(t -> t.getId()).collect(Collectors.toList()));

      List<Model> lateTaskList =
          this.getMetaModelRecords(config, processInstanceIds, modelName, user, LATE_TASK);
      lateTaskIds.addAll(lateTaskList.stream().map(t -> t.getId()).collect(Collectors.toList()));
    }

    if (taskMap.containsKey("taskTodayIds")) {
      taskTodayIds.addAll((List<Long>) taskMap.get("taskTodayIds"));
    }
    if (taskMap.containsKey("taskTodayIds")) {
      taskNextIds.addAll((List<Long>) taskMap.get("taskNextIds"));
    }
    if (taskMap.containsKey("taskTodayIds")) {
      lateTaskIds.addAll((List<Long>) taskMap.get("lateTaskIds"));
    }

    taskMap.put("taskTodayIds", taskTodayIds);
    taskMap.put("taskNextIds", taskNextIds);
    taskMap.put("lateTaskIds", lateTaskIds);
  }
}
