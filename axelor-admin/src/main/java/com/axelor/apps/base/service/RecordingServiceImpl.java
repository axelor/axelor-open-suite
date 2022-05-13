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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.DataBackup;
import com.axelor.apps.base.db.Recording;
import com.axelor.apps.base.db.SearchConfiguration;
import com.axelor.apps.base.db.repo.DataBackupRepository;
import com.axelor.apps.base.db.repo.RecordingRepository;
import com.axelor.apps.base.exceptions.IExceptionMessages;
import com.axelor.apps.base.service.app.DataBackupCreateService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.annotations.NameColumn;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class RecordingServiceImpl implements RecordingService {

  protected RecordingRepository recordingRepo;
  protected DataBackupRepository dataBackupRepo;
  protected DataBackupCreateService dataBackUpService;
  protected MetaModelRepository metaModelRepo;

  protected static Set<String> excludeFieldNameList =
      ImmutableSet.of(
          "importOrigin",
          "id",
          "updatedBy",
          "createdBy",
          "updatedOn",
          "createdOn",
          "archived",
          "version",
          "attrs");

  protected static final Set<String> META_DOMAINS =
      ImmutableSet.of(
          MetaFile.class.getName(),
          MetaAttachment.class.getName(),
          MetaTranslation.class.getName());

  @Inject
  public RecordingServiceImpl(
      RecordingRepository recordingRepo,
      DataBackupRepository dataBackupRepo,
      DataBackupCreateService dataBackUpService,
      MetaModelRepository metaModelRepo) {
    this.recordingRepo = recordingRepo;
    this.dataBackupRepo = dataBackupRepo;
    this.dataBackUpService = dataBackUpService;
    this.metaModelRepo = metaModelRepo;
  }

  @Override
  @Transactional
  public void addModelIdLog(Recording recording, String modelName, Long recordId) {

    try {
      MetaModel metaModel =
          Beans.get(MetaModelRepository.class)
              .all()
              .filter("self.fullName = ?", modelName)
              .fetchOne();
      if (!isRecordingModel(recording, metaModel)) {
        return;
      }

      String modelIds = recording.getModelIds();

      JSONObject jsonObject =
          Strings.isNullOrEmpty(modelIds) ? new JSONObject() : new JSONObject(modelIds);
      String metaModelFullName = metaModel.getFullName();

      List<String> idsList =
          new ArrayList<>(
              Arrays.asList(
                  jsonObject.containsKey(metaModelFullName)
                      ? jsonObject.getString(metaModelFullName).split(",")
                      : new String[] {recordId.toString()}));

      if (!idsList.contains(recordId.toString())) idsList.add(recordId.toString());

      recording.setModelIds(
          jsonObject
              .put(
                  metaModelFullName,
                  idsList.toString().replaceAll(" ", "").replace("[", "").replace("]", ""))
              .toString());

      recordingRepo.save(recording);
    } catch (JSONException e) {
      TraceBackService.trace(e);
    }
  }

  protected boolean isRecordingModel(Recording recording, MetaModel metaModel) {

    Set<MetaModel> includeModelSet = recording.getIncludeModelSet();
    Set<MetaModel> excludeModelSet = recording.getExcludeModelSet();
    if ((!ObjectUtils.isEmpty(excludeModelSet) && excludeModelSet.contains(metaModel))
        || (!ObjectUtils.isEmpty(includeModelSet) && !includeModelSet.contains(metaModel))
        || StringUtils.isBlank(metaModel.getTableName())
        || metaModel.getFullName().equals(DataBackup.class.getName())
        || metaModel.getFullName().equals(Recording.class.getName())
        || metaModel.getPackageName().equals("com.axelor.studio.db")
        || (metaModel.getPackageName().equals("com.axelor.meta.db")
            && !META_DOMAINS.contains(metaModel.getFullName()))) {
      return false;
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public void stopRecording(Recording recording)
      throws InterruptedException, IOException, JSONException, AxelorException {

    DataBackup dataBackup =
        dataBackupRepo
            .all()
            .filter("self.importId = ?", DataBackupRepository.DATA_BACKUP_RECORDING_IMPORT_ID)
            .fetchOne();

    if (dataBackup != null) {
      recording.setStatusSelect(RecordingRepository.STATUS_STOP);

      JSONObject jsonObject = new JSONObject(recording.getModelIds());

      Set<String> metaModelFNSet = jsonObject.keySet();
      if (metaModelFNSet != null && !metaModelFNSet.isEmpty()) {
        Map<MetaModel, String> metaModelIdListMap = new HashMap<>();
        Map<String, String> metaModelSearchConfigMap = new HashMap<>();
        Set<SearchConfiguration> searchConfigSet = recording.getSearchConfigurationSet();

        for (MetaModel metaModel :
            metaModelRepo.all().filter("self.fullName IN ?", metaModelFNSet).fetch()) {
          metaModelIdListMap.put(metaModel, jsonObject.getString(metaModel.getFullName()));
          manageSearchConfig(metaModel, searchConfigSet, metaModelSearchConfigMap);
        }
        dataBackUpService.create(
            dataBackup, false, metaModelIdListMap, searchConfigSet, metaModelSearchConfigMap);

        recording = recordingRepo.find(recording.getId());
        recording.setRecordingData(dataBackup.getBackupMetaFile());
        recording.setStopDateTime(LocalDateTime.now());
        recording.setModelIds(null);
        recording.setStatusSelect(RecordingRepository.STATUS_STOP);
        recordingRepo.save(recording);
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessages.RECORDING_DATA_BACKUP_MISSING));
    }
  }

  protected void manageSearchConfig(
      MetaModel metaModel,
      Set<SearchConfiguration> searchConfigSet,
      Map<String, String> metaModelSearchConfigMap) {

    if (ObjectUtils.isEmpty(searchConfigSet)) {
      return;
    }

    searchConfigSet.stream()
        .forEach(
            (searchConfig) -> {
              String search = getSearchFromSearchConfiguration(searchConfig);
              if (StringUtils.notBlank(search)) {
                metaModelSearchConfigMap.put(searchConfig.getMetaModel().getFullName(), search);
              }
            });
  }

  protected String getSearchFromSearchConfiguration(SearchConfiguration searchConfiguration) {
    Set<MetaField> searchFields = searchConfiguration.getMetaFieldSet();
    List<String> searchArr = new ArrayList<>();

    for (MetaField metaField : searchFields) {
      String fieldName = metaField.getName();
      if (excludeFieldNameList.contains(fieldName)) {
        continue;
      }

      String fieldSearch = null;
      fieldSearch = String.format("self.%s = :%s", fieldName, fieldName);
      if (StringUtils.notBlank(fieldSearch)) {
        searchArr.add(fieldSearch);
      }
    }

    if (ObjectUtils.notEmpty(searchArr)) {
      return String.join(" AND ", searchArr);
    }

    return null;
  }

  protected String getSearchFromFieldConfiguration(MetaModel metaModel) {

    try {
      List<Field> fields =
          Arrays.asList(Class.forName(metaModel.getFullName()).getDeclaredFields());

      Field uniqueRequiredField = null;
      for (Field field : fields) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && columnAnnotation.unique()) {
          NotNull notNullAnnotation = field.getAnnotation(NotNull.class);
          if (notNullAnnotation != null) {
            uniqueRequiredField = field;
            break;
          }
        }
      }

      if (uniqueRequiredField != null) {
        return String.format(
            "self.%s = %s", uniqueRequiredField.getName(), uniqueRequiredField.getName());
      } else {
        Field nameColumnField = getNameColumnField(fields);
        if (nameColumnField != null) {
          return String.format(
              "self.%s = %s", nameColumnField.getName(), nameColumnField.getName());
        } else if (fields.stream().anyMatch(field -> field.getName().equals("name"))) {
          return "self.name = name";
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  protected Field getNameColumnField(List<Field> fields) {

    Field nameColumnField = null;
    for (Field field : fields) {
      NameColumn nameColumnAnnotation = field.getAnnotation(NameColumn.class);
      if (nameColumnAnnotation != null) {
        nameColumnField = field;
        break;
      }
    }

    return nameColumnField;
  }

  @Override
  public MetaField addNameColumn(MetaModel metaModel, Set<MetaField> metaFields) {

    if (metaModel == null) {
      return null;
    }

    MetaField metaField = null;
    try {
      List<Field> fields =
          Arrays.asList(Class.forName(metaModel.getFullName()).getDeclaredFields());
      Field field = getNameColumnField(fields);
      if (field == null) {
        return null;
      }

      metaField =
          Beans.get(MetaFieldRepository.class)
              .all()
              .filter("self.name = :name AND self.metaModel = :metaModel")
              .bind("name", field.getName())
              .bind("metaModel", metaModel)
              .fetchOne();
      if (ObjectUtils.notEmpty(metaFields) && metaField != null && metaFields.contains(metaField)) {
        return null;
      }
    } catch (SecurityException | ClassNotFoundException e) {
      TraceBackService.trace(e);
    }

    return metaField;
  }
}
