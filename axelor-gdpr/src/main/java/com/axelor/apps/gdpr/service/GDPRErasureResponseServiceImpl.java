package com.axelor.apps.gdpr.service;

import com.axelor.apps.base.db.Anonymizer;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.service.app.AnonymizeService;
import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.repo.GDPRErasureResponseRepository;
import com.axelor.apps.gdpr.service.app.AppGDPRService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.TemplateMessageServiceImpl;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.views.Selection;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import wslite.json.JSONException;

public class GDPRErasureResponseServiceImpl implements GDPRErasureResponseService {

  protected MetaModelRepository metaModelRepo;
  protected GDPRResponseService gdprResponseService;
  protected AppGDPRService appGDPRService;
  protected GDPRErasureResponseRepository gdprErasureResponseRepository;
  protected GDPRErasureLogService gdprErasureLogService;
  protected AnonymizeService anonymizeService;
  protected GDPRAnonymizeService gdprAnonymizeService;
  private String modelSelect;

  @Inject
  public GDPRErasureResponseServiceImpl(
      MetaModelRepository metaModelRepo,
      GDPRResponseService gdprResponseService,
      AppGDPRService appGDPRService,
      GDPRErasureResponseRepository gdprErasureResponseRepository,
      GDPRErasureLogService gdprErasureLogService,
      AnonymizeService anonymizeService,
      GDPRAnonymizeService gdprAnonymizeService) {
    this.metaModelRepo = metaModelRepo;
    this.gdprResponseService = gdprResponseService;
    this.appGDPRService = appGDPRService;
    this.gdprErasureResponseRepository = gdprErasureResponseRepository;
    this.gdprErasureLogService = gdprErasureLogService;
    this.anonymizeService = anonymizeService;
    this.gdprAnonymizeService = gdprAnonymizeService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public GDPRErasureResponse createErasureResponse(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException {

    GDPRErasureResponse gdprErasureResponse = new GDPRErasureResponse();
    modelSelect = gdprRequest.getModelSelect();

    // choosed object
    AuditableModel referenceEntity =
        gdprResponseService.extractReferenceFromModelAndId(modelSelect, gdprRequest.getModelId());

    // get meta model
    MetaModel entityMetaModel = getMetaModelFromFullName(modelSelect);

    gdprResponseService
        .getEmailFromPerson(referenceEntity)
        .ifPresent(gdprErasureResponse::setResponseEmailAddress);

    // anonymize datas
    anonymizeEntity(gdprErasureResponse, referenceEntity, entityMetaModel);

    JPA.merge(referenceEntity);

    gdprErasureResponseRepository.save(gdprErasureResponse);
    gdprRequest.setErasureResponse(gdprErasureResponse);

    return gdprErasureResponse;
  }

  /**
   * anonymize given entity and create logs
   *
   * @param gdprErasureResponse
   * @param reference
   * @param metaModel
   * @throws ClassNotFoundException
   * @throws AxelorException
   */
  protected void anonymizeEntity(
      GDPRErasureResponse gdprErasureResponse, AuditableModel reference, MetaModel metaModel)
      throws ClassNotFoundException, AxelorException {
    Mapper mapper = Mapper.of(reference.getClass());

    // search anonymizer config
    List<AnonymizerLine> anonymizerLines = getMetaModelAnonymizerLines(metaModel);

    List<MetaField> metaFields =
        metaModel.getMetaFields().stream()
            .filter(
                metaField ->
                    !GDPRAnonymizeService.excludeFields.contains(metaField.getName())
                        && !modelSelect.equals(
                            metaField.getPackageName() + "." + metaField.getTypeName()))
            .collect(Collectors.toList());

    for (MetaField metaField : metaFields) {
      anonymizeMetaField(gdprErasureResponse, reference, mapper, metaField, anonymizerLines);
    }

    reference.setArchived(true);

    Optional<GDPRErasureLog> gdprErasureLogOpt =
        Optional.ofNullable(gdprErasureResponse.getResponseErasureLogList())
            .orElse(Collections.emptyList()).stream()
            .filter(erasureLog -> erasureLog.getModelLog().equals(metaModel))
            .findFirst();

    GDPRErasureLog gdprErasureLog;
    if (gdprErasureLogOpt.isPresent()) {
      gdprErasureLog = gdprErasureLogOpt.get();
      gdprErasureResponse.removeResponseErasureLogListItem(gdprErasureLog);
      gdprErasureLog.setNumberOfrecords(gdprErasureLog.getNumberOfrecords() + 1);
    } else {
      gdprErasureLog =
          gdprErasureLogService.createErasureLogLine(gdprErasureResponse, metaModel, 1);
    }

    gdprErasureResponse.addResponseErasureLogListItem(gdprErasureLog);
  }

  /**
   * anonymize given metaField
   *
   * @param gdprErasureResponse
   * @param reference
   * @param mapper
   * @param metaField
   * @param anonymizerLines
   * @throws AxelorException
   * @throws ClassNotFoundException
   */
  protected void anonymizeMetaField(
      GDPRErasureResponse gdprErasureResponse,
      AuditableModel reference,
      Mapper mapper,
      MetaField metaField,
      List<AnonymizerLine> anonymizerLines)
      throws AxelorException, ClassNotFoundException {

    Object currentValue = mapper.get(reference, metaField.getName());
    // no value to anonymize, exit
    if (Objects.isNull(currentValue)) {
      return;
    }

    // no relationship field
    if (Objects.isNull(metaField.getRelationship())) {

      Object newValue;
      Optional<AnonymizerLine> anonymizerLine =
          anonymizerLines.stream().filter(al -> al.getMetaField().equals(metaField)).findFirst();

      // if no anonymizer defined,pass
      if (!anonymizerLine.isPresent()) return;

      // handle selection fields
      Property property = mapper.getProperty(metaField.getName());
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

    } else if (metaField.getRelationship().equals("OneToOne")) {

      anonymizeRelatedObject(gdprErasureResponse, metaField, currentValue);

    } else if (metaField.getRelationship().equals("OneToMany")) {
      List<Object> relatedObjects = (List<Object>) currentValue;

      for (Object relatedObject : relatedObjects) {
        anonymizeRelatedObject(gdprErasureResponse, metaField, relatedObject);
      }
    }
  }

  protected void anonymizeRelatedObject(
      GDPRErasureResponse gdprErasureResponse, MetaField metaField, Object currentValue)
      throws ClassNotFoundException, AxelorException {

    // choosed object
    AuditableModel relatedEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            metaField.getPackageName() + "." + metaField.getTypeName(),
            ((AuditableModel) currentValue).getId());

    // get meta model
    MetaModel relatedMetaModel =
        getMetaModelFromFullName(metaField.getPackageName() + "." + metaField.getTypeName());

    anonymizeEntity(gdprErasureResponse, relatedEntity, relatedMetaModel);
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

  /**
   * @param fullName
   * @return
   */
  protected MetaModel getMetaModelFromFullName(String fullName) {
    return metaModelRepo.all().filter("self.fullName = '" + fullName + "'").fetchOne();
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void anonymizeTrackingDatas(GDPRRequest gdprRequest)
      throws ClassNotFoundException, JSONException, IOException {
    AuditableModel referenceEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            gdprRequest.getModelSelect(), gdprRequest.getModelId());

    gdprAnonymizeService.anonymizeTrackingDatas(referenceEntity);
  }

  @Override
  public boolean sendEmailResponse(GDPRErasureResponse gdprErasureResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException, JSONException {

    Template template =
        Beans.get(TemplateRepository.class)
            .all()
            .filter(
                "self.metaModel = ?",
                metaModelRepo.findByName(gdprErasureResponse.getClass().getSimpleName()))
            .fetchOne();

    if (Objects.nonNull(template)) {
      Message message =
          Beans.get(TemplateMessageServiceImpl.class)
              .generateAndSendMessage(gdprErasureResponse, template);
      gdprErasureResponse.setSendingDateTime(LocalDateTime.now());
      gdprErasureResponse.setResponseMessage(message);
      return true;
    }

    return false;
  }
}
