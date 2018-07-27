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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.common.Inflector;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AdvancedExportController {

  @Inject private AdvancedExportService advancedExportService;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private AdvancedExportRepository advancedExportRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  private Inflector inflector;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void getModelAllFields(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException {

    AdvancedExport advancedExport = request.getContext().asType(AdvancedExport.class);
    inflector = Inflector.getInstance();

    if (advancedExport.getMetaModel() != null) {
      List<Map<String, Object>> allFieldList = new ArrayList<>();

      for (MetaField field : advancedExport.getMetaModel().getMetaFields()) {
        Map<String, Object> allFieldMap = new HashMap<>();
        allFieldMap.put("currentDomain", advancedExport.getMetaModel().getName());

        if (!Strings.isNullOrEmpty(field.getRelationship())) {
          MetaModel metaModel =
              metaModelRepo.all().filter("self.name = ?", field.getTypeName()).fetchOne();

          Class<?> klass = Class.forName(metaModel.getFullName());
          Mapper mapper = Mapper.of(klass);
          String fieldName = mapper.getNameField() == null ? "id" : mapper.getNameField().getName();
          MetaField metaField =
              metaFieldRepo
                  .all()
                  .filter("self.name = ?1 AND self.metaModel = ?2", fieldName, metaModel)
                  .fetchOne();
          allFieldMap.put("metaField", metaField);
          allFieldMap.put("targetField", field.getName() + "." + metaField.getName());
        } else {
          allFieldMap.put("metaField", field);
          allFieldMap.put("targetField", field.getName());
        }

        if (Strings.isNullOrEmpty(field.getLabel())) {
          allFieldMap.put("title", this.getFieldTitle(inflector, field.getName()));
        } else {
          allFieldMap.put("title", field.getLabel());
        }
        allFieldList.add(allFieldMap);
      }
      response.setAttr("advancedExportLineList", "value", allFieldList);
    }
  }

  public void fillTitle(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    MetaField metaField = (MetaField) context.get("metaField");
    if (metaField != null) {
      if (Strings.isNullOrEmpty(metaField.getLabel())) {
        inflector = Inflector.getInstance();
        response.setValue("title", I18n.get(this.getFieldTitle(inflector, metaField.getName())));
      } else {
        response.setValue("title", I18n.get(metaField.getLabel()));
      }
    } else {
      response.setValue("title", null);
    }
  }

  private String getFieldTitle(Inflector inflector, String fieldName) {
    return inflector.humanize(fieldName);
  }

  @SuppressWarnings("deprecation")
  public void fillTargetField(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    MetaModel parentMetaModel = (MetaModel) context.getParentContext().get("metaModel");
    MetaField metaField = (MetaField) context.get("metaField");

    if (metaField != null) {
      String targetField = "";
      if (context.get("targetField") == null) {
        targetField = metaField.getName();
      } else {
        targetField = advancedExportService.getTargetField(context, metaField, parentMetaModel);
      }
      response.setValue("targetField", targetField);

      if (metaField.getRelationship() != null) {
        response.setValue("currentDomain", metaField.getTypeName());
        response.setValue("metaField", null);
      } else {
        response.setAttr("metaField", "readonly", true);
        response.setAttr("validateFieldSelection", "readonly", true);
        response.setAttr("$viewerMessage", "hidden", false);
        response.setAttr("$isValidate", "value", true);
      }
    }
  }

  public void advancedExportPDF(ActionRequest request, ActionResponse response)
      throws InvalidFormatException {
    try {
      advancedExport(request, response, AdvancedExportService.PDF);
    } catch (DocumentException | ClassNotFoundException | IOException | AxelorException e) {
      TraceBackService.trace(e);
    }
  }

  public void advancedExportExcel(ActionRequest request, ActionResponse response)
      throws InvalidFormatException {
    try {
      advancedExport(request, response, AdvancedExportService.EXCEL);
    } catch (DocumentException | ClassNotFoundException | IOException | AxelorException e) {
      TraceBackService.trace(e);
    }
  }

  public void advancedExportCSV(ActionRequest request, ActionResponse response)
      throws InvalidFormatException {
    try {
      advancedExport(request, response, AdvancedExportService.CSV);
    } catch (DocumentException | ClassNotFoundException | IOException | AxelorException e) {
      TraceBackService.trace(e);
    }
  }

  private void advancedExport(ActionRequest request, ActionResponse response, String fileType)
      throws ClassNotFoundException, InvalidFormatException, DocumentException, IOException,
          AxelorException {
    AdvancedExport advancedExport = request.getContext().asType(AdvancedExport.class);
    advancedExport = advancedExportRepo.find(advancedExport.getId());

    getAdvancedExportFile(request, response, advancedExport, fileType);
  }

  private void getAdvancedExportFile(
      ActionRequest request,
      ActionResponse response,
      AdvancedExport advancedExport,
      String fileType)
      throws ClassNotFoundException, InvalidFormatException, DocumentException, IOException,
          AxelorException {

    if (!advancedExport.getAdvancedExportLineList().isEmpty()) {
      Map<Boolean, MetaFile> exportMap = new HashMap<Boolean, MetaFile>();
      String criteria = createCriteria(request, advancedExport);

      exportMap = advancedExportService.getAdvancedExport(advancedExport, criteria, fileType);

      MetaFile exportFile = getExportFile(response, exportMap);
      downloadExportFile(response, exportFile);
    } else {
      response.setError(I18n.get(IExceptionMessage.ADVANCED_EXPORT_1));
    }
  }

  @SuppressWarnings("unchecked")
  private String createCriteria(ActionRequest request, AdvancedExport advancedExport) {

    if (request.getContext().get("_criteria") != null) {
      if (request.getContext().get("_criteria").toString().startsWith("[")) {
        return request.getContext().get("_criteria").toString();

      } else {
        ObjectMapper mapper = new ObjectMapper();
        ActionRequest parentRequest =
            mapper.convertValue(request.getContext().get("_criteria"), ActionRequest.class);
        Class<? extends Model> klass = (Class<? extends Model>) parentRequest.getBeanClass();
        Filter filter = advancedExportService.getJpaSecurityFilter(advancedExport.getMetaModel());
        Stream<? extends Model> listObj =
            parentRequest
                .getCriteria()
                .createQuery(klass, filter)
                .fetchSteam(advancedExport.getMaxExportLimit());
        return listObj.map(it -> it.getId()).collect(Collectors.toList()).toString();
      }
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  public void callAdvancedExportWizard(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException {

    LOG.debug("Call advanced export wizard for model : {} ", request.getModel());
    MetaModel metaModel =
        metaModelRepo.all().filter("self.fullName = ?", request.getModel()).fetchOne();
    String criteria = "";
    if (request.getContext().get("_ids") != null)
      criteria = request.getContext().get("_ids").toString();
    else {
      Class<? extends Model> klass = (Class<? extends Model>) request.getBeanClass();
      Filter filter = advancedExportService.getJpaSecurityFilter(metaModel);
      int recordCount = (int) request.getCriteria().createQuery(klass, filter).count();
      if (recordCount > 0) criteria = String.valueOf(recordCount);
    }

    if (Strings.isNullOrEmpty(criteria))
      response.setError(I18n.get(IExceptionMessage.ADVANCED_EXPORT_2));
    else {
      response.setView(
          ActionView.define(I18n.get("Advanced export"))
              .model(AdvancedExport.class.getName())
              .add("form", "advanced-export-wizard-form")
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .context("_metaModel", metaModel)
              .context("_criteria", criteria.startsWith("[") ? criteria : request)
              .map());
    }
  }

  @SuppressWarnings({"rawtypes"})
  public void generateExportFile(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException, IOException, DocumentException, AxelorException,
          InvalidFormatException {

    if (request.getContext().get("_xAdvancedExport") == null
        || request.getContext().get("exportFormatSelect") == null) {
      response.setError(I18n.get(IExceptionMessage.ADVANCED_EXPORT_4));
      return;
    }
    AdvancedExport advancedExport =
        advancedExportRepo.find(
            Long.valueOf(
                ((Map) request.getContext().get("_xAdvancedExport")).get("id").toString()));
    String fileType = request.getContext().get("exportFormatSelect").toString();

    getAdvancedExportFile(request, response, advancedExport, fileType);
  }

  private MetaFile getExportFile(ActionResponse response, Map<Boolean, MetaFile> exportMap) {
    MetaFile exportFile = null;
    boolean isReachMaxExportLimit = false;
    for (Map.Entry<Boolean, MetaFile> entry : exportMap.entrySet()) {
      isReachMaxExportLimit = entry.getKey();
      exportFile = entry.getValue();
    }
    if (isReachMaxExportLimit) {
      response.setFlash(I18n.get(IExceptionMessage.ADVANCED_EXPORT_3));
    }
    return exportFile;
  }

  private void downloadExportFile(ActionResponse response, MetaFile exportFile) {
    if (exportFile != null) {
      response.setView(
          ActionView.define(I18n.get("Export file"))
              .model(AdvancedExport.class.getName())
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + exportFile.getId()
                      + "/content/download?v="
                      + exportFile.getVersion())
              .param("download", "true")
              .map());
    }
  }
}
