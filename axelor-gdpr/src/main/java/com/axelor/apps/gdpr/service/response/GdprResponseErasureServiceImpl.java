package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.base.db.Anonymizer;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.service.app.AnonymizeService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.db.repo.GDPRResponseRepository;
import com.axelor.apps.gdpr.service.GdprAnonymizeService;
import com.axelor.apps.gdpr.service.GdprErasureLogService;
import com.axelor.apps.gdpr.service.app.AppGdprService;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
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
      AppBaseService appBaseService) {
    this.metaModelRepo = metaModelRepo;
    this.gdprResponseService = gdprResponseService;
    this.appGDPRService = appGDPRService;
    this.gdprResponseRepository = gdprResponseRepository;
    this.gdprErasureLogService = gdprErasureLogService;
    this.anonymizeService = anonymizeService;
    this.gdprAnonymizeService = gdprAnonymizeService;
    this.appBaseService = appBaseService;
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

    gdprResponseService
        .getEmailFromPerson(referenceEntity)
        .ifPresent(gdprResponse::setResponseEmailAddress);

    // anonymize datas
    anonymizeEntity(gdprResponse, referenceEntity, entityMetaModel);

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
      GDPRResponse gdprResponse, AuditableModel reference, MetaModel metaModel)
      throws ClassNotFoundException, AxelorException {
    Mapper mapper = Mapper.of(reference.getClass());

    // search anonymizer config
    List<AnonymizerLine> anonymizerLines = getMetaModelAnonymizerLines(metaModel);

    List<MetaField> metaFields =
        metaModel.getMetaFields().stream()
            .filter(
                metaField ->
                    !GdprAnonymizeService.excludeFields.contains(metaField.getName())
                        && !modelSelect.equals(
                            metaField.getPackageName() + "." + metaField.getTypeName()))
            .collect(Collectors.toList());

    for (MetaField metaField : metaFields) {
      anonymizeMetaField(gdprResponse, reference, mapper, metaField, anonymizerLines);
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

      anonymizeRelatedObject(gdprResponse, metaField, currentValue);

    } else if (metaField.getRelationship().equals("OneToMany")) {
      List<Object> relatedObjects = (List<Object>) currentValue;

      for (Object relatedObject : relatedObjects) {
        anonymizeRelatedObject(gdprResponse, metaField, relatedObject);
      }
    }
  }

  protected void anonymizeRelatedObject(
      GDPRResponse gdprResponse, MetaField metaField, Object currentValue)
      throws ClassNotFoundException, AxelorException {

    // choosed object
    AuditableModel relatedEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            metaField.getPackageName() + "." + metaField.getTypeName(),
            ((AuditableModel) currentValue).getId());

    // get meta model
    MetaModel relatedMetaModel =
        getMetaModelFromFullName(metaField.getPackageName() + "." + metaField.getTypeName());

    anonymizeEntity(gdprResponse, relatedEntity, relatedMetaModel);
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
  @Override
  public void anonymizeTrackingDatas(GDPRRequest gdprRequest)
      throws ClassNotFoundException, JSONException, IOException {
    AuditableModel referenceEntity =
        gdprResponseService.extractReferenceFromModelAndId(
            gdprRequest.getModelSelect(), gdprRequest.getModelId());

    gdprAnonymizeService.anonymizeTrackingDatas(referenceEntity);
  }

  @Override
  public boolean sendEmailResponse(GDPRResponse gdprResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException, JSONException {

    Template template =
        Beans.get(TemplateRepository.class)
            .all()
            .filter(
                "self.metaModel = ?",
                metaModelRepo.findByName(gdprResponse.getClass().getSimpleName()))
            .fetchOne();

    if (Objects.nonNull(template)) {
      Message message =
          Beans.get(TemplateMessageServiceImpl.class)
              .generateAndSendMessage(gdprResponse, template);
      gdprResponse.setSendingDateT(appBaseService.getTodayDateTime().toLocalDateTime());
      gdprResponse.setResponseMessage(message);
      return true;
    }

    return false;
  }
}
