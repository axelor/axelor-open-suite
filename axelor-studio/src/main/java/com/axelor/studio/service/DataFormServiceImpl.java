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
package com.axelor.studio.service;

import com.axelor.apps.base.db.AppBpm;
import com.axelor.apps.base.db.repo.AppBpmRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.db.DataFormLine;
import com.axelor.studio.db.repo.DataFormRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.builder.HtmlFormBuilderService;
import com.axelor.studio.variables.DataFormVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public class DataFormServiceImpl implements DataFormService {

  protected DataFormRepository dataFormRepository;

  protected AppBpmRepository appBpmRepository;

  protected MetaFiles metaFiles;

  protected DataFormMetaModelService metaModelService;

  protected DataFormJsonModelService jsonModelService;

  @Inject
  public DataFormServiceImpl(
      DataFormRepository dataFormRepository,
      AppBpmRepository appBpmRepository,
      MetaFiles metaFiles,
      DataFormMetaModelService metaModelService,
      DataFormJsonModelService jsonModelService) {
    this.dataFormRepository = dataFormRepository;
    this.appBpmRepository = appBpmRepository;
    this.metaFiles = metaFiles;
    this.metaModelService = metaModelService;
    this.jsonModelService = jsonModelService;
  }

  @Override
  public String generateHtmlForm(DataForm dataForm)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    List<DataFormLine> dataFormLineList = dataForm.getDataFormLineList();
    if (ObjectUtils.isEmpty(dataFormLineList)) {
      return null;
    }

    HtmlFormBuilderService htmlFormBuilder =
        new HtmlFormBuilderService(dataForm.getCode(), dataForm.getLanguage());
    Collections.sort(dataFormLineList, Comparator.comparing(DataFormLine::getSequence));

    if (dataForm.getCustom()) {
      jsonModelService.generateHtmlFormForMetaJsonModel(htmlFormBuilder, dataFormLineList);
    } else {
      metaModelService.generateHtmlFormForMetaModel(htmlFormBuilder, dataFormLineList);
    }

    return htmlFormBuilder.build(dataForm);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateFields(DataForm dataForm) throws JsonProcessingException, JAXBException {

    dataForm = dataFormRepository.find(dataForm.getId());

    if (dataForm.getCustom()) {
      jsonModelService.generateFieldsForMetaJsonModel(dataForm);
    } else if (dataForm.getModelFormView() != null) {
      metaModelService.generateFieldsFormView(dataForm);
    } else {
      metaModelService.generateFieldsMetaModel(dataForm);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = Exception.class)
  public <T extends AuditableModel> T createRecord(
      Map<String, List<InputPart>> formDataMap,
      Class<?> klass,
      Boolean custom,
      String jsonModelName)
      throws ClassNotFoundException, IOException, AxelorException, ServletException {

    final Mapper mapper = Mapper.of(klass);
    final JpaRepository<T> repo = JpaRepository.of((Class<T>) klass);
    T bean = repo.create(new HashMap<>());

    if (custom) {
      jsonModelService.createRecordMetaJsonModel(formDataMap, jsonModelName, mapper, bean);
    } else {
      metaModelService.createRecordMetaModel(formDataMap, klass, mapper, bean);
    }

    setPublicUser(mapper, bean);
    return repo.save(bean);
  }

  protected <T extends AuditableModel> void setPublicUser(final Mapper mapper, T bean) {
    AppBpm appBpm = appBpmRepository.all().fetchOne();
    User user = appBpm.getPublicUser();
    mapper.set(bean, DataFormVariables.CREATED_BY, user);
  }

  @Override
  public DataForm getDataForm(Map<String, List<InputPart>> formDataMap)
      throws AxelorException, IOException {
    List<InputPart> modelPart = formDataMap.get(DataFormVariables.MODEL_CODE);
    if (ObjectUtils.isEmpty(modelPart)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.DATAFORM_NOT_EXIST),
          DataFormVariables.NULL);
    }
    String modelCode = modelPart.get(0).getBodyAsString();
    if (StringUtils.isEmpty(modelCode)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.DATAFORM_NOT_EXIST),
          DataFormVariables.EMPTY);
    }
    DataForm dataForm = dataFormRepository.findByCode(modelCode);
    if (ObjectUtils.isEmpty(dataForm)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.DATAFORM_NOT_EXIST),
          modelCode);
    }
    return dataForm;
  }

  @Override
  public void checkRecordToCreate(Map<String, List<InputPart>> formDataMap, String dataFormModel)
      throws AxelorException, IOException {
    List<InputPart> recordToCreatePart = formDataMap.get(DataFormVariables.RECORD_TO_CREATE);
    if (ObjectUtils.isEmpty(recordToCreatePart)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.DATAFORM_MODEL_MISMATCH),
          dataFormModel,
          DataFormVariables.NULL);
    }
    String recordToCreate = recordToCreatePart.get(0).getBodyAsString();
    if (StringUtils.isEmpty(recordToCreate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.DATAFORM_MODEL_MISMATCH),
          dataFormModel,
          DataFormVariables.EMPTY);
    }
    if (!recordToCreate.equals(dataFormModel)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.DATAFORM_MODEL_MISMATCH),
          dataFormModel,
          recordToCreate);
    }
  }

  @Override
  public String getSuccessfulFormSubmissionMessage() {
    AppBpm appBpm = appBpmRepository.all().fetchOne();
    return StringUtils.defaultIfEmpty(
        appBpm.getSuccessfulFormSubmissionMessage(), DataFormVariables.DEFAULT_SUCCESS_MSG);
  }

  @Override
  public String getFailedFormSubmissionMessage() {
    AppBpm appBpm = appBpmRepository.all().fetchOne();
    return StringUtils.defaultIfEmpty(
        appBpm.getFailureFormSubmissionMessage(), DataFormVariables.DEFAULT_ERROR_MSG);
  }

  @Override
  public MetaFile processFileUpload(InputPart value) {
    try {
      InputStream inputStream = value.getBody(InputStream.class, null);
      String fileName = value.getHeaders().getFirst(DataFormVariables.CONTENT_DISPOSITION);
      int startPos = fileName.indexOf(DataFormVariables.FILENAME) + DataFormVariables.START_INDEX;
      int endPos = fileName.indexOf("\"", startPos);
      fileName = fileName.substring(startPos, endPos);
      return metaFiles.upload(inputStream, fileName);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public Long getIdFromInputPart(InputPart inputPart) {
    try {
      return inputPart.getBody(Long.class, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
