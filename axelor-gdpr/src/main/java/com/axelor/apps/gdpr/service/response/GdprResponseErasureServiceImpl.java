/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Anonymizer;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AnonymizeService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.apps.gdpr.db.RelationshipAnonymizer;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.db.repo.GDPRResponseRepository;
import com.axelor.apps.gdpr.exception.GdprExceptionMessage;
import com.axelor.apps.gdpr.service.GdprAnonymizeService;
import com.axelor.apps.gdpr.service.GdprErasureLogService;
import com.axelor.apps.gdpr.service.app.AppGdprService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.views.Selection;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import wslite.json.JSONException;

public class GdprResponseErasureServiceImpl implements GdprResponseErasureService {

  protected MetaModelRepository metaModelRepo;
  protected GdprResponseService gdprResponseService;
  protected AppGdprService appGDPRService;
  protected GDPRResponseRepository gdprResponseRepository;
  protected GdprErasureLogService gdprErasureLogService;
  protected AnonymizeService anonymizeService;
  protected GdprAnonymizeService gdprAnonymizeService;
  protected AppBaseService appBaseService;
  protected TemplateMessageService templateMessageService;
  private String modelSelect;

  @Inject
  public GdprResponseErasureServiceImpl(
      MetaModelRepository metaModelRepo,
      GdprResponseService gdprResponseService,
      AppGdprService appGDPRService,
      GDPRResponseRepository gdprResponseRepository,
      GdprErasureLogService gdprErasureLogService,
      AnonymizeService anonymizeService,
      GdprAnonymizeService gdprAnonymizeService,
      AppBaseService appBaseService,
      TemplateMessageService templateMessageService) {
    this.metaModelRepo = metaModelRepo;
    this.gdprResponseService = gdprResponseService;
    this.appGDPRService = appGDPRService;
    this.gdprResponseRepository = gdprResponseRepository;
    this.gdprErasureLogService = gdprErasureLogService;
    this.anonymizeService = anonymizeService;
    this.gdprAnonymizeService = gdprAnonymizeService;
    this.appBaseService = appBaseService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createErasureResponse(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException {

    GDPRResponse gdprResponse = new GDPRResponse();
    modelSelect = gdprRequest.getModelSelect();

    // choosed object
    AuditableModel referenceEntity =
        gdprResponseService.extractReferenceFromModelAndId(modelSelect, gdprRequest.getModelId());

    // get meta model
    MetaModel entityMetaModel = getMetaModelFromFullName(modelSelect);

    Anonymizer anonymizer = appGDPRService.getGdprAnonymizer();

    gdprResponseService
        .getEmailFromPerson(referenceEntity)
        .ifPresent(gdprResponse::setResponseEmailAddress);

    StringBuilder anonymizationResult = new StringBuilder();

    // anonymize datas
    anonymizeEntity(gdprResponse, referenceEntity, entityMetaModel, anonymizationResult, 0);

    if (StringUtils.isBlank(anonymizationResult.toString())) {
      anonymizationResult.append(
          String.format(
              I18n.get(GdprExceptionMessage.ANONYMIZATION_SUCCESS_RESULT),
              entityMetaModel.getName(),
              anonymizer.getName()));
    }
    gdprResponse.setAnonymizationResult(anonymizationResult.toString());

    JPA.merge(referenceEntity);

    gdprRequest.setGdprResponse(gdprResponse);
    gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_CONFIRMED);

    gdprResponseRepository.save(gdprResponse);
  }

  /**
   * anonymize given entity and create logs
   *
   * @param gdprResponse
   * @param reference
   * @param metaModel
   * @throws ClassNotFoundException
   * @throws AxelorException
   */
  protected void anonymizeEntity(
      GDPRResponse gdprResponse,
      AuditableModel reference,
      MetaModel metaModel,
      StringBuilder anonymizationResult,
      int depth)
      throws ClassNotFoundException, AxelorException {
    Mapper mapper = Mapper.of(reference.getClass());

    // search anonymizer config
    List<AnonymizerLine> anonymizerLines = getMetaModelAnonymizerLines(metaModel);

    List<MetaField> metaFields =
        metaModel.getMetaFields().stream()
            .filter(metaField -> !GdprAnonymizeService.excludeFields.contains(metaField.getName()))
            .collect(Collectors.toList());

    // filter on base object to avoid infinite loop
    if (depth > 0) {
      metaFields =
          metaFields.stream()
              .filter(
                  metaField ->
                      !modelSelect.equals(
                          metaField.getPackageName() + "." + metaField.getTypeName()))
              .collect(Collectors.toList());
    }

    for (MetaField metaField : metaFields) {
      anonymizeMetaField(
          gdprResponse, reference, mapper, metaField, anonymizerLines, anonymizationResult, depth);
    }

    reference.setArchived(true);

    Optional<GDPRErasureLog> gdprErasureLogOpt =
        Optional.ofNullable(gdprResponse.getResponseErasureLogList())
            .orElse(Collections.emptyList()).stream()
            .filter(erasureLog -> erasureLog.getModelLog().equals(metaModel))
            .findFirst();

    GDPRErasureLog gdprErasureLog;
    if (gdprErasureLogOpt.isPresent()) {
      gdprErasureLog = gdprErasureLogOpt.get();
      gdprResponse.removeResponseErasureLogListItem(gdprErasureLog);
      gdprErasureLog.setNumberOfrecords(gdprErasureLog.getNumberOfrecords() + 1);
    } else {
      gdprErasureLog = gdprErasureLogService.createErasureLogLine(gdprResponse, metaModel, 1);
    }

    gdprResponse.addResponseErasureLogListItem(gdprErasureLog);
  }

  /**
   * anonymize given metaField
   *
   * @param gdprResponse
   * @param reference
   * @param mapper
   * @param metaField
   * @param anonymizerLines
   * @throws AxelorException
   * @throws ClassNotFoundException
   */
  protected void anonymizeMetaField(
      GDPRResponse gdprResponse,
      AuditableModel reference,
      Mapper mapper,
      MetaField metaField,
      List<AnonymizerLine> anonymizerLines,
      StringBuilder anonymizationResult,
      int depth)
      throws AxelorException, ClassNotFoundException {

    Object currentValue = mapper.get(reference, metaField.getName());
    // no value to anonymize, exit
    if (Objects.isNull(currentValue)) {
      return;
    }

    List<RelationshipAnonymizer> relationshipAnonymizers =
        appGDPRService.getAppGDPR().getRelationsShipAnonymizer();

    Property property = mapper.getProperty(metaField.getName());
    // no relationship field
    if (Objects.isNull(metaField.getRelationship())) {

      Object newValue;
      Optional<AnonymizerLine> anonymizerLine =
          anonymizerLines.stream().filter(al -> al.getMetaField().equals(metaField)).findFirst();

      // if no anonymizer defined,pass
      if (!anonymizerLine.isPresent()) return;

      // handle selection fields
      if (StringUtils.isEmpty(property.getSelection())) {
        newValue =
            anonymizeService.anonymizeValue(
                currentValue,
                property,
                anonymizerLine.map(AnonymizerLine::getFakerApiField).orElse(null));
      } else {
        Selection.Option option = MetaStore.getSelectionList(property.getSelection()).get(0);
        newValue = option.getValue();
      }
      mapper.set(reference, metaField.getName(), newValue);

    } else if (depth < 1
        && metaField.getRelationship().equals("OneToOne")
        && Strings.isNullOrEmpty(metaField.getMappedBy())) {

      anonymizeRelatedObject(gdprResponse, metaField, currentValue, anonymizationResult, depth);

    } else if (depth < 1 && metaField.getRelationship().equals("OneToMany")) {
      // o2m : break link if config, anonymization if config (if not breaking link)
      List<Object> relatedObjects = (List<Object>) currentValue;

      String mappedBy = metaField.getMappedBy();

      if (StringUtils.isBlank(mappedBy)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(GdprExceptionMessage.MODEL_FIELD_NO_MAPPED_BY), metaField.getName()));
      }

      Optional<RelationshipAnonymizer> relationshipAnonymizer =
          relationshipAnonymizers.stream()
              .filter(
                  anonymizer ->
                      anonymizer
                          .getModel()
                          .equals(
                              getMetaModelFromFullName(
                                  metaField.getPackageName() + "." + metaField.getTypeName())))
              .findFirst();

      if (relationshipAnonymizer.isPresent() && property.isRequired()) {
        anonymizationResult.append(
            String.format(
                I18n.get(GdprExceptionMessage.RELATIONSHIP_ANONYMIZER_ONE_TO_MANY_REQUIRED_RESULT),
                metaField.getName(),
                metaField.getMetaModel().getName()));
        anonymizationResult.append("\n");
        return;
      }

      for (Object relatedObject : relatedObjects) {
        if (relationshipAnonymizer.isPresent()) {
          breakO2MRelationship(metaField, relationshipAnonymizers, mappedBy, relatedObject);
        } else {
          anonymizeRelatedObject(
              gdprResponse, metaField, relatedObject, anonymizationResult, depth);
        }
      }
    } else if (depth < 1 && metaField.getRelationship().equals("ManyToOne")) {
      // m2o, no anonymization, break link if config
      MetaModel modelToSearch =
          getMetaModelFromFullName(metaField.getPackageName() + "." + metaField.getTypeName());
      Optional<RelationshipAnonymizer> relationshipAnonymizer =
          relationshipAnonymizers.stream()
              .filter(anonymizer -> anonymizer.getModel().equals(modelToSearch))
              .findAny();
      if (relationshipAnonymizer.isPresent()) {
        breakM2ORelationship(property, reference, mapper, metaField, relationshipAnonymizer.get());
      }
    }
  }

  protected void breakO2MRelationship(
      MetaField metaField,
      List<RelationshipAnonymizer> relationshipAnonymizers,
      String mappedBy,
      Object relatedObject)
      throws ClassNotFoundException, AxelorException {
    Mapper o2mMapper = Mapper.of(relatedObject.getClass());
    Property mappedProperty = o2mMapper.getProperty(mappedBy);

    AuditableModel newObject = null;
    if (mappedProperty.isRequired()) {
      // configuration needed if property is Required
      RelationshipAnonymizer parentRelationshipAnonymizer =
          getRelationshipAnonymizer(metaField.getMetaModel(), relationshipAnonymizers);

      newObject =
          gdprResponseService.extractReferenceFromModelAndId(
              parentRelationshipAnonymizer.getModel().getFullName(),
              (long) parentRelationshipAnonymizer.getModelId());

      // check replacement object configuration
      if (newObject == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(GdprExceptionMessage.RELATIONSHIP_ANONYMIZER_MISSING_REPLACEMENT),
                metaField.getMetaModel().getName()));
      }
    }
    o2mMapper.set(relatedObject, mappedBy, newObject);
  }

  /**
   * break relationship following configuration
   *
   * @param property
   * @param reference
   * @param mapper
   * @param metaField
   * @param relationshipAnonymizer
   * @throws ClassNotFoundException
   */
  protected void breakM2ORelationship(
      Property property,
      AuditableModel reference,
      Mapper mapper,
      MetaField metaField,
      RelationshipAnonymizer relationshipAnonymizer)
      throws ClassNotFoundException, AxelorException {

    AuditableModel newObject = null;
    if (property.isRequired()) {
      newObject =
          gdprResponseService.extractReferenceFromModelAndId(
              relationshipAnonymizer.getModel().getFullName(),
              (long) relationshipAnonymizer.getModelId());

      if (newObject == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(GdprExceptionMessage.RELATIONSHIP_ANONYMIZER_MISSING_REPLACEMENT),
                relationshipAnonymizer.getModel().getName()));
      }
    }
    mapper.set(reference, metaField.getName(), newObject);
  }

  protected void anonymizeRelatedObject(
      GDPRResponse gdprResponse,
      MetaField metaField,
      Object currentValue,
      StringBuilder anonymizationResult,
      int depth)
      throws ClassNotFoundException, AxelorException {

    // choosed object
    AuditableModel relatedEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            metaField.getPackageName() + "." + metaField.getTypeName(),
            ((AuditableModel) currentValue).getId());

    // get meta model
    MetaModel relatedMetaModel =
        getMetaModelFromFullName(metaField.getPackageName() + "." + metaField.getTypeName());

    anonymizeEntity(gdprResponse, relatedEntity, relatedMetaModel, anonymizationResult, ++depth);
  }

  /**
   * get actual model anonymizerLine parameterized in app config
   *
   * @param metaModel
   * @return
   */
  protected List<AnonymizerLine> getMetaModelAnonymizerLines(MetaModel metaModel) {

    return Optional.ofNullable(appGDPRService.getAppGDPR().getAnonymizer())
        .map(Anonymizer::getAnonymizerLineList).orElse(Collections.emptyList()).stream()
        .filter(anonymizerLine -> anonymizerLine.getMetaModel().equals(metaModel))
        .collect(Collectors.toList());
  }

  protected RelationshipAnonymizer getRelationshipAnonymizer(
      MetaModel metaModel, List<RelationshipAnonymizer> relationshipAnonymizers)
      throws AxelorException {
    return relationshipAnonymizers.stream()
        .filter(anonymizer -> anonymizer.getModel().equals(metaModel))
        .findFirst()
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    String.format(
                        I18n.get(GdprExceptionMessage.RELATIONSHIP_ANONYMIZER_MISSING_REPLACEMENT),
                        metaModel.getName())));
  }

  /**
   * @param fullName
   * @return
   */
  protected MetaModel getMetaModelFromFullName(String fullName) {
    return metaModelRepo.all().filter("self.fullName = '" + fullName + "'").fetchOne();
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  @Override
  public void anonymizeTrackingDatas(GDPRRequest gdprRequest)
      throws ClassNotFoundException, IOException {
    AuditableModel referenceEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            gdprRequest.getModelSelect(), gdprRequest.getModelId());

    gdprAnonymizeService.anonymizeTrackingDatas(referenceEntity);
  }

  @Override
  public void sendEmailResponse(GDPRResponse gdprResponse) throws AxelorException {

    Template template = this.appGDPRService.getAppGDPR().getErasureResponseTemplate();

    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(GdprExceptionMessage.MISSING_ERASURE_REQUEST_RESPONSE_MAIL_TEMPLATE));
    }

    try {
      Message message = templateMessageService.generateAndSendMessage(gdprResponse, template);
      gdprResponse.setSendingDateT(appBaseService.getTodayDateTime().toLocalDateTime());
      gdprResponse.setResponseMessage(message);

    } catch (JSONException | IOException | ClassNotFoundException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          GdprExceptionMessage.SENDING_MAIL_ERROR,
          e.getMessage());
    }
  }
}
