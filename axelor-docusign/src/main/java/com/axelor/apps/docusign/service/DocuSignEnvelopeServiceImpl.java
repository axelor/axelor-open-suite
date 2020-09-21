package com.axelor.apps.docusign.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.docusign.db.DocuSignAccount;
import com.axelor.apps.docusign.db.DocuSignDocument;
import com.axelor.apps.docusign.db.DocuSignDocumentSetting;
import com.axelor.apps.docusign.db.DocuSignEnvelope;
import com.axelor.apps.docusign.db.DocuSignEnvelopeSetting;
import com.axelor.apps.docusign.db.DocuSignField;
import com.axelor.apps.docusign.db.DocuSignFieldSetting;
import com.axelor.apps.docusign.db.DocuSignSigner;
import com.axelor.apps.docusign.db.DocuSignSignerSetting;
import com.axelor.apps.docusign.db.repo.DocuSignEnvelopeRepository;
import com.axelor.apps.docusign.db.repo.DocuSignFieldSettingRepository;
import com.axelor.apps.docusign.exceptions.IExceptionMessage;
import com.axelor.apps.message.service.TemplateContextService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
import com.axelor.tool.template.TemplateMaker;
import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.Envelope;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeDocument;
import com.docusign.esign.model.EnvelopeDocumentsResult;
import com.docusign.esign.model.EnvelopeEvent;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.EventNotification;
import com.docusign.esign.model.InPersonSigner;
import com.docusign.esign.model.RecipientEvent;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocuSignEnvelopeServiceImpl implements DocuSignEnvelopeService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final char TEMPLATE_DELIMITER = '$';

  public static final String CERTIFICATE_ID = "certificate";
  public static final String CERTIFICATE_FILENAME = "Certificate of completion";
  public static final String PDF_EXTENSION = "pdf";

  protected DocuSignEnvelopeRepository docuSignEnvelopeRepo;
  protected TemplateContextService templateContextService;
  protected MetaFiles metaFiles;

  @Inject
  public DocuSignEnvelopeServiceImpl(
      DocuSignEnvelopeRepository docuSignEnvelopeRepo,
      TemplateContextService templateContextService,
      MetaFiles metaFiles) {
    this.docuSignEnvelopeRepo = docuSignEnvelopeRepo;
    this.templateContextService = templateContextService;
    this.metaFiles = metaFiles;
  }

  @Override
  public Map<String, Object> generateEnvelope(
      DocuSignEnvelopeSetting envelopeSetting, Long objectId) throws AxelorException {
    MetaModel metaModel = envelopeSetting.getMetaModel();
    if (ObjectUtils.isEmpty(metaModel)) {
      return null;
    }

    if (ObjectUtils.notEmpty(envelopeSetting)) {

      DocuSignEnvelope envelope = createEnvelope(envelopeSetting, objectId);

      return ActionView.define("Create envelope")
          .model(DocuSignEnvelope.class.getName())
          .add("form", "docusign-envelope-form")
          .param("forceEdit", "true")
          .context("_showRecord", envelope.getId().toString())
          .map();
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public DocuSignEnvelope createEnvelope(DocuSignEnvelopeSetting envelopeSetting, Long objectId)
      throws AxelorException {

    DocuSignEnvelope envelope = new DocuSignEnvelope();
    envelope.setDocuSignEnvelopeSetting(envelopeSetting);
    envelope.setIsOrderedDocuments(envelopeSetting.getIsOrderedDocuments());
    envelope.setIsOrderedSigners(envelopeSetting.getIsOrderedSigners());

    MetaModel metaModel = envelopeSetting.getMetaModel();

    Context scriptContext = null;
    if (ObjectUtils.notEmpty(metaModel) && ObjectUtils.notEmpty(objectId)) {
      try {
        Class<? extends Model> modelClass =
            (Class<? extends Model>) Class.forName(metaModel.getFullName());
        Model model = JPA.find(modelClass, objectId);
        if (ObjectUtils.notEmpty(model)) {
          TemplateMaker maker =
              new TemplateMaker(Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
          maker.setContext(model);
          if (StringUtils.notEmpty(envelopeSetting.getName())) {
            maker.setTemplate(envelopeSetting.getName());
            envelope.setName(maker.make());
          }
          if (StringUtils.notEmpty(envelopeSetting.getEmailSubject())) {
            maker.setTemplate(envelopeSetting.getEmailSubject());
            envelope.setEmailSubject(maker.make());
          }

          envelope.setRelatedToSelect(metaModel.getFullName());
          envelope.setRelatedToId(objectId);
          scriptContext = new Context(Mapper.toMap(model), modelClass);
        }
      } catch (ClassNotFoundException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }
    }

    if (ObjectUtils.notEmpty(envelopeSetting.getDocuSignSignerSettingList())) {
      for (DocuSignSignerSetting signerSetting : envelopeSetting.getDocuSignSignerSettingList()) {
        envelope.addDocuSignSignerListItem(createDocuSignSigner(signerSetting, scriptContext));
      }
    }

    List<DocuSignSigner> docuSignSignerList = envelope.getDocuSignSignerList();
    if (ObjectUtils.notEmpty(envelopeSetting.getDocuSignDocumentSettingList())
        && CollectionUtils.isNotEmpty(docuSignSignerList)) {
      for (DocuSignDocumentSetting documentSetting :
          envelopeSetting.getDocuSignDocumentSettingList()) {
        envelope.addDocuSignDocumentListItem(
            createDocuSignDocument(documentSetting, scriptContext, docuSignSignerList));
      }
    }

    return docuSignEnvelopeRepo.save(envelope);
  }

  protected DocuSignSigner createDocuSignSigner(
      DocuSignSignerSetting signerSetting, Context scriptContext) {
    DocuSignSigner docuSignSigner = new DocuSignSigner();
    docuSignSigner.setName(signerSetting.getName());
    docuSignSigner.setRecipientId(signerSetting.getRecipientId());
    docuSignSigner.setIsRequired(signerSetting.getIsRequired());
    docuSignSigner.setSequence(signerSetting.getSequence());
    docuSignSigner.setIsInPersonSigner(signerSetting.getIsInPersonSigner());

    if (ObjectUtils.notEmpty(scriptContext)) {

      if (ObjectUtils.notEmpty(signerSetting.getSignerDefaultPath())) {
        Object evaluation =
            templateContextService.computeTemplateContext(
                signerSetting.getSignerDefaultPath(), scriptContext);
        if (evaluation instanceof Partner) {
          Partner signerPartner = (Partner) evaluation;
          docuSignSigner.setSigner(signerPartner);
        }
      }

      if (ObjectUtils.notEmpty(signerSetting.getCompanyDefaultPath())) {
        Object evaluation =
            templateContextService.computeTemplateContext(
                signerSetting.getCompanyDefaultPath(), scriptContext);
        if (evaluation instanceof Partner) {
          Company company = (Company) evaluation;
          docuSignSigner.setCompany(company);
        }
      }
    }

    return docuSignSigner;
  }

  protected DocuSignDocument createDocuSignDocument(
      DocuSignDocumentSetting documentSetting,
      Context scriptContext,
      List<DocuSignSigner> docuSignSignerList) {
    DocuSignDocument docuSignDocument = new DocuSignDocument();
    docuSignDocument.setName(documentSetting.getName());
    docuSignDocument.setDocumentId(documentSetting.getDocumentId());
    docuSignDocument.setFileExtension(documentSetting.getFileExtension());
    docuSignDocument.setSequence(documentSetting.getSequence());

    if (ObjectUtils.notEmpty(scriptContext)) {
      if (ObjectUtils.notEmpty(documentSetting.getUnsignedMetaFileDefaultPath())) {
        Object evaluation =
            templateContextService.computeTemplateContext(
                documentSetting.getUnsignedMetaFileDefaultPath(), scriptContext);
        if (evaluation instanceof MetaFile) {
          MetaFile unsignedMetaFile = (MetaFile) evaluation;
          docuSignDocument.setUnsignedMetaFile(unsignedMetaFile);
        }
      }
    }

    if (CollectionUtils.isNotEmpty(documentSetting.getDocuSignFieldSettingList())) {
      documentSetting
          .getDocuSignFieldSettingList()
          .forEach(
              fieldSetting ->
                  docuSignDocument.addDocuSignFieldListItem(
                      createDocuSignField(fieldSetting, docuSignSignerList)));
    }
    return docuSignDocument;
  }

  protected DocuSignField createDocuSignField(
      DocuSignFieldSetting docuSignFieldSetting, List<DocuSignSigner> docuSignSignerList) {
    DocuSignField docuSignField = new DocuSignField();
    docuSignField.setName(docuSignFieldSetting.getName());
    docuSignField.setTypeSelect(docuSignFieldSetting.getTypeSelect());
    docuSignField.setValue(docuSignFieldSetting.getValue());
    docuSignField.setTabLabel(docuSignFieldSetting.getTabLabel());
    docuSignField.setPageNumber(docuSignFieldSetting.getPageNumber());
    docuSignField.setAnchor(docuSignFieldSetting.getAnchor());
    docuSignField.setAnchorUnits(docuSignFieldSetting.getAnchorUnits());
    docuSignField.setAnchorXOffset(docuSignFieldSetting.getAnchorXOffset());
    docuSignField.setAnchorYOffset(docuSignFieldSetting.getAnchorYOffset());
    docuSignField.setxPosition(docuSignFieldSetting.getxPosition());
    docuSignField.setyPosition(docuSignFieldSetting.getyPosition());
    docuSignField.setIsRequired(docuSignFieldSetting.getIsRequired());
    docuSignField.setFont(docuSignFieldSetting.getFont());
    docuSignField.setFontSize(docuSignFieldSetting.getFontSize());
    docuSignField.setFontColor(docuSignFieldSetting.getFontColor());
    docuSignField.setIsBold(docuSignFieldSetting.getIsBold());

    DocuSignSigner docuSignSigner =
        docuSignSignerList.stream()
            .filter(
                signerItem ->
                    ObjectUtils.notEmpty(docuSignFieldSetting.getDocuSignSignerSetting())
                        && signerItem
                            .getRecipientId()
                            .equals(
                                docuSignFieldSetting.getDocuSignSignerSetting().getRecipientId()))
            .findAny()
            .orElse(null);
    docuSignField.setDocuSignSigner(docuSignSigner);

    if (CollectionUtils.isNotEmpty(docuSignFieldSetting.getDocuSignFieldSettingList())) {
      for (DocuSignFieldSetting docuSignFieldSettingChild :
          docuSignFieldSetting.getDocuSignFieldSettingList()) {
        docuSignField.addDocuSignFieldListItem(
            createDocuSignField(docuSignFieldSettingChild, docuSignSignerList));
      }
    }

    return docuSignField;
  }

  protected void checkEventNotification(
      EnvelopeDefinition envelopeDefinition, DocuSignEnvelopeSetting envelopeSetting)
      throws AxelorException {

    EventNotification eventNotification = new EventNotification();
    String webhookUrl = AppSettings.get().getBaseURL() + "/ws/public/docusign/update-envelope";

    eventNotification.setUrl(webhookUrl);
    eventNotification.setLoggingEnabled("true");
    eventNotification.setRequireAcknowledgment("true");

    if (envelopeSetting.getCheckEnvelopeStatus()) {
      List<EnvelopeEvent> envelopeEvents = new ArrayList<>();

      if (envelopeSetting.getCheckEnvelopeStatusDelivered()) {
        EnvelopeEvent envelopeEventDelivered = new EnvelopeEvent();
        envelopeEventDelivered.setEnvelopeEventStatusCode("delivered");
        envelopeEvents.add(envelopeEventDelivered);
      }
      if (envelopeSetting.getCheckEnvelopeStatusCompleted()) {
        EnvelopeEvent envelopeEventCompleted = new EnvelopeEvent();
        envelopeEventCompleted.setEnvelopeEventStatusCode("completed");
        envelopeEvents.add(envelopeEventCompleted);
      }
      if (envelopeSetting.getCheckEnvelopeStatusDeclined()) {
        EnvelopeEvent envelopeEventDeclined = new EnvelopeEvent();
        envelopeEventDeclined.setEnvelopeEventStatusCode("declined");
        envelopeEvents.add(envelopeEventDeclined);
      }
      if (envelopeSetting.getCheckEnvelopeStatusVoided()) {
        EnvelopeEvent envelopeEventVoided = new EnvelopeEvent();
        envelopeEventVoided.setEnvelopeEventStatusCode("voided");
        envelopeEvents.add(envelopeEventVoided);
      }

      eventNotification.setEnvelopeEvents(envelopeEvents);
    }

    if (envelopeSetting.getCheckRecipientStatus()) {
      List<RecipientEvent> recipientEvents = new ArrayList<>();

      if (envelopeSetting.getCheckRecipientStatusDelivered()) {
        RecipientEvent recipientEventDelivered = new RecipientEvent();
        recipientEventDelivered.setRecipientEventStatusCode("delivered");
        recipientEvents.add(recipientEventDelivered);
      }
      if (envelopeSetting.getCheckRecipientStatusCompleted()) {
        RecipientEvent recipientEventCompleted = new RecipientEvent();
        recipientEventCompleted.setRecipientEventStatusCode("completed");
        recipientEvents.add(recipientEventCompleted);
      }
      if (envelopeSetting.getCheckRecipientStatusDeclined()) {
        RecipientEvent recipientEventDeclined = new RecipientEvent();
        recipientEventDeclined.setRecipientEventStatusCode("Declined");
        recipientEvents.add(recipientEventDeclined);
      }

      eventNotification.setRecipientEvents(recipientEvents);
    }

    envelopeDefinition.setEventNotification(eventNotification);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public DocuSignEnvelope sendEnvelope(DocuSignEnvelope docuSignEnvelope) throws AxelorException {
    if (ObjectUtils.notEmpty(docuSignEnvelope)
        && ObjectUtils.notEmpty(docuSignEnvelope.getDocuSignEnvelopeSetting())
        && CollectionUtils.isNotEmpty(docuSignEnvelope.getDocuSignDocumentList())) {
      DocuSignEnvelopeSetting envelopeSetting = docuSignEnvelope.getDocuSignEnvelopeSetting();

      EnvelopeDefinition envelopeDefinition =
          createEnvelopeDefinition(envelopeSetting, docuSignEnvelope);

      EnvelopesApi envelopesApi = getEnvelopesApi(envelopeSetting.getDocuSignAccount());

      try {
        EnvelopeSummary results =
            envelopesApi.createEnvelope(
                envelopeSetting.getDocuSignAccount().getAccountId(), envelopeDefinition);

        if (StringUtils.notEmpty(results.getEnvelopeId())) {
          docuSignEnvelope.setEnvelopeId(results.getEnvelopeId());
          docuSignEnvelope.setStatusSelect(DocuSignEnvelopeRepository.STATUS_SENT);
          docuSignEnvelopeRepo.save(docuSignEnvelope);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.DOCUSIGN_ENVELOPE_ID_NULL));
        }

      } catch (ApiException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }
    }

    return docuSignEnvelope;
  }

  protected EnvelopeDefinition createEnvelopeDefinition(
      DocuSignEnvelopeSetting envelopeSetting, DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {

    EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
    envelopeDefinition.setEmailSubject(envelopeSetting.getEmailSubject());

    List<DocuSignDocument> docuSignDocumentList = docuSignEnvelope.getDocuSignDocumentList();
    List<Document> documentList = createDocuments(docuSignDocumentList);
    envelopeDefinition.setDocuments(documentList);

    List<Signer> signerList = createSigners(docuSignEnvelope.getDocuSignSignerList());
    List<InPersonSigner> inPersonSignerList =
        createInPersonSigners(docuSignEnvelope.getDocuSignSignerList());
    updateSigners(signerList, inPersonSignerList, docuSignDocumentList);
    Recipients recipients = new Recipients();
    if (CollectionUtils.isNotEmpty(signerList)) {
      recipients.setSigners(signerList);
    }
    if (CollectionUtils.isNotEmpty(inPersonSignerList)) {
      recipients.setInPersonSigners(inPersonSignerList);
    }
    envelopeDefinition.setRecipients(recipients);

    if (envelopeSetting.getActiveWebhook()) {
      checkEventNotification(envelopeDefinition, envelopeSetting);
    }

    envelopeDefinition.setStatus(DocuSignEnvelopeRepository.STATUS_SENT);

    return envelopeDefinition;
  }

  protected List<Document> createDocuments(List<DocuSignDocument> docuSignDocumentList)
      throws AxelorException {
    List<Document> documentList = null;

    if (CollectionUtils.isNotEmpty(docuSignDocumentList)) {
      documentList = new ArrayList<>();

      for (DocuSignDocument docuSignDocument : docuSignDocumentList) {

        if (ObjectUtils.notEmpty(docuSignDocument.getUnsignedMetaFile())) {
          documentList.add(
              createDocument(docuSignDocument, docuSignDocument.getUnsignedMetaFile()));
        }
      }
    }
    return documentList;
  }

  protected Document createDocument(DocuSignDocument docuSignDocument, MetaFile metaFile)
      throws AxelorException {
    Document document = null;

    if (ObjectUtils.notEmpty(docuSignDocument) && ObjectUtils.notEmpty(metaFile)) {
      try {
        document = new Document();
        byte[] buffer = Files.readAllBytes(MetaFiles.getPath(metaFile));

        String docBase64 = new String(Base64.getEncoder().encode(buffer));
        document.setDocumentBase64(docBase64);
        document.setName(docuSignDocument.getName());
        document.setFileExtension(docuSignDocument.getFileExtension());
        document.setDocumentId(docuSignDocument.getDocumentId());
        if (ObjectUtils.notEmpty(docuSignDocument.getDocuSignEnvelope())
            && docuSignDocument.getDocuSignEnvelope().getIsOrderedDocuments()) {
          document.setOrder(String.valueOf(docuSignDocument.getSequence()));
        }

      } catch (IOException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }
    }

    return document;
  }

  protected List<Signer> createSigners(List<DocuSignSigner> docuSignSignerList)
      throws AxelorException {
    List<Signer> signerList = null;

    if (CollectionUtils.isNotEmpty(docuSignSignerList)) {
      signerList = new ArrayList<>();

      for (DocuSignSigner docuSignSigner : docuSignSignerList) {
        Partner signerPartner = docuSignSigner.getSigner();
        String recipientId = docuSignSigner.getRecipientId();

        if (!docuSignSigner.getIsInPersonSigner()) {
          Signer signer = new Signer();
          signer.setRecipientId(recipientId);
          signer.setAccessCode(docuSignSigner.getAccessCode());
          if (ObjectUtils.notEmpty(
                  docuSignSigner.getDocuSignEnvelope().getDocuSignEnvelopeSetting())
              && docuSignSigner
                  .getDocuSignEnvelope()
                  .getDocuSignEnvelopeSetting()
                  .getIsOrderedSigners()) {
            signer.setRoutingOrder(String.valueOf(docuSignSigner.getSequence() + 1));
          }
          if (ObjectUtils.notEmpty(signerPartner.getEmailAddress())) {
            signer.setEmail(signerPartner.getEmailAddress().getAddress());
          } else {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.DOCUSIGN_EMAIL_ADDRESS_EMPTY));
          }
          signer.setName(signerPartner.getSimpleFullName());

          signerList.add(signer);
        }
      }
    }

    return signerList;
  }

  protected List<InPersonSigner> createInPersonSigners(List<DocuSignSigner> docuSignSignerList)
      throws AxelorException {
    List<InPersonSigner> inPersonSignerList = null;

    if (CollectionUtils.isNotEmpty(docuSignSignerList)) {
      inPersonSignerList = new ArrayList<>();

      for (DocuSignSigner docuSignSigner : docuSignSignerList) {
        Partner signerPartner = docuSignSigner.getSigner();
        String recipientId = docuSignSigner.getRecipientId();

        if (docuSignSigner.getIsInPersonSigner()) {
          InPersonSigner inPersonSigner = new InPersonSigner();
          inPersonSigner.setRecipientId(recipientId);
          inPersonSigner.setAccessCode(docuSignSigner.getAccessCode());
          if (ObjectUtils.notEmpty(
                  docuSignSigner.getDocuSignEnvelope().getDocuSignEnvelopeSetting())
              && docuSignSigner
                  .getDocuSignEnvelope()
                  .getDocuSignEnvelopeSetting()
                  .getIsOrderedSigners()) {
            inPersonSigner.setRoutingOrder(String.valueOf(docuSignSigner.getSequence() + 1));
          }
          if (ObjectUtils.notEmpty(signerPartner.getEmailAddress())) {
            inPersonSigner.setHostEmail(signerPartner.getEmailAddress().getAddress());
          } else {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.DOCUSIGN_EMAIL_ADDRESS_EMPTY));
          }
          inPersonSigner.setHostName(signerPartner.getSimpleFullName());

          inPersonSignerList.add(inPersonSigner);
        }
      }
    }

    return inPersonSignerList;
  }

  protected void updateSigners(
      List<Signer> signerList,
      List<InPersonSigner> inPersonSignerList,
      List<DocuSignDocument> docuSignDocumentList)
      throws AxelorException {

    if (CollectionUtils.isNotEmpty(docuSignDocumentList)) {

      for (DocuSignDocument docuSignDocument : docuSignDocumentList) {

        if (CollectionUtils.isNotEmpty(docuSignDocument.getDocuSignFieldList())) {
          for (DocuSignField docuSignField : docuSignDocument.getDocuSignFieldList()) {
            if (ObjectUtils.notEmpty(docuSignField.getDocuSignSigner())) {
              String recipientId = docuSignField.getDocuSignSigner().getRecipientId();
              if (docuSignField.getDocuSignSigner().getIsInPersonSigner()) {
                InPersonSigner inPersonSigner = findInPersonSigner(inPersonSignerList, recipientId);
                if (ObjectUtils.notEmpty(inPersonSigner)) {
                  updateInPersonSigner(inPersonSigner, docuSignField);
                } else {
                  throw new AxelorException(
                      TraceBackRepository.CATEGORY_INCONSISTENCY,
                      I18n.get(IExceptionMessage.DOCUSIGN_IN_PERSON_SIGNER_NOT_FOUND));
                }

              } else {
                Signer signer = findSigner(signerList, recipientId);
                if (ObjectUtils.notEmpty(signer)) {
                  updateSigner(signer, docuSignField);
                } else {
                  throw new AxelorException(
                      TraceBackRepository.CATEGORY_INCONSISTENCY,
                      I18n.get(IExceptionMessage.DOCUSIGN_SIGNER_NOT_FOUND));
                }
              }
            }
          }
        }
      }
    }
  }

  protected InPersonSigner findInPersonSigner(
      List<InPersonSigner> inPersonSignerList, String recipientId) {
    InPersonSigner inPersonSigner = null;

    if (CollectionUtils.isNotEmpty(inPersonSignerList) && StringUtils.notEmpty(recipientId)) {
      inPersonSigner =
          inPersonSignerList.stream()
              .filter(signerItem -> recipientId.equals(signerItem.getRecipientId()))
              .findAny()
              .orElse(null);
    }

    return inPersonSigner;
  }

  protected InPersonSigner updateInPersonSigner(
      InPersonSigner inPersonSigner, DocuSignField docuSignField) throws AxelorException {

    if (ObjectUtils.notEmpty(docuSignField.getDocuSignSigner())) {

      DocuSignSigner docuSignSigner = docuSignField.getDocuSignSigner();
      Partner signerPartner = docuSignSigner.getSigner();
      Company company = docuSignSigner.getCompany();
      Tabs tabs = inPersonSigner.getTabs();
      if (ObjectUtils.isEmpty(tabs)) {
        tabs = new Tabs();
        inPersonSigner.setTabs(tabs);
      }

      String documentId = docuSignField.getDocuSignDocument().getDocumentId();
      processItem(
          tabs, docuSignField, documentId, inPersonSigner.getRecipientId(), signerPartner, company);
    }

    return inPersonSigner;
  }

  protected Signer findSigner(List<Signer> signerList, String recipientId) {
    Signer signer = null;

    if (CollectionUtils.isNotEmpty(signerList) && StringUtils.notEmpty(recipientId)) {
      signer =
          signerList.stream()
              .filter(signerItem -> recipientId.equals(signerItem.getRecipientId()))
              .findAny()
              .orElse(null);
    }

    return signer;
  }

  protected Signer updateSigner(Signer signer, DocuSignField docuSignField) throws AxelorException {

    if (ObjectUtils.notEmpty(docuSignField.getDocuSignSigner())) {
      DocuSignSigner docuSignSigner = docuSignField.getDocuSignSigner();
      Partner signerPartner = docuSignSigner.getSigner();
      Company company = docuSignSigner.getCompany();
      Tabs tabs = signer.getTabs();
      if (ObjectUtils.isEmpty(tabs)) {
        tabs = new Tabs();
        signer.setTabs(tabs);
      }

      String documentId = docuSignField.getDocuSignDocument().getDocumentId();
      processItem(tabs, docuSignField, documentId, signer.getRecipientId(), signerPartner, company);
    }

    return signer;
  }

  protected void processItem(
      Tabs tabs,
      DocuSignField docuSignField,
      String documentId,
      String recipientId,
      Partner partner,
      Company company)
      throws AxelorException {

    switch (docuSignField.getTypeSelect()) {
      case DocuSignFieldSettingRepository.TYPE_SIGN_HERE:
        DocuSignUtils.addSignHere(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_FULL_NAME:
        DocuSignUtils.addFullName(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_EMAIL:
        String email = null;
        if (ObjectUtils.notEmpty(partner) && ObjectUtils.notEmpty(partner.getEmailAddress())) {
          email = partner.getEmailAddress().getAddress();
        }
        DocuSignUtils.addEmail(tabs, docuSignField, documentId, recipientId, email);
        break;
      case DocuSignFieldSettingRepository.TYPE_COMPANY:
        String companyName = null;
        if (ObjectUtils.notEmpty(company)) {
          companyName = company.getName();
        }
        DocuSignUtils.addCompany(tabs, docuSignField, documentId, recipientId, companyName);
        break;
      case DocuSignFieldSettingRepository.TYPE_CHECKBOX:
        DocuSignUtils.addCheckbox(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_RADIO_GROUP:
        DocuSignUtils.addRadioGroup(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_LIST:
        DocuSignUtils.addList(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_APPROVE:
        DocuSignUtils.addApprove(tabs, docuSignField, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_DECLINE:
        DocuSignUtils.addDecline(tabs, docuSignField, documentId, recipientId);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.DOCUSIGN_PARAM_ITEM_UNKNOWN_TYPE));
    }
  }

  protected EnvelopesApi getEnvelopesApi(DocuSignAccount docuSignAccount) throws AxelorException {

    EnvelopesApi envelopesApi = null;
    if (ObjectUtils.notEmpty(docuSignAccount)) {
      ApiClient apiClient = new ApiClient();
      apiClient.addDefaultHeader("Authorization", "Bearer " + docuSignAccount.getAccessToken());
      apiClient.setBasePath(docuSignAccount.getBasePath());
      envelopesApi = new EnvelopesApi(apiClient);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.DOCUSIGN_ACCOUNT_EMPTY));
    }

    return envelopesApi;
  }

  @Transactional
  @Override
  public DocuSignEnvelope synchroniseEnvelopeStatus(DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {
    DocuSignEnvelopeSetting envelopeSetting = docuSignEnvelope.getDocuSignEnvelopeSetting();
    if (ObjectUtils.notEmpty(envelopeSetting)) {
      EnvelopesApi envelopesApi = getEnvelopesApi(envelopeSetting.getDocuSignAccount());

      String envelopeId = docuSignEnvelope.getEnvelopeId();
      if (StringUtils.notEmpty(envelopeId)) {
        try {
          Envelope envelope =
              envelopesApi.getEnvelope(
                  envelopeSetting.getDocuSignAccount().getAccountId(), envelopeId);
          String envelopeStatus = envelope.getStatus();
          docuSignEnvelope.setStatusSelect(envelopeStatus);
          LOG.debug("Envelope id : " + envelopeId + " / status : " + envelopeStatus);

          updateFields(envelopesApi, docuSignEnvelope);
          if (DocuSignEnvelopeRepository.STATUS_COMPLETED.equals(envelopeStatus)) {
            downloadDocumentsFile(envelopesApi, docuSignEnvelope);
            if (StringUtils.notEmpty(envelope.getCompletedDateTime())) {
              docuSignEnvelope.setCompletedDateTime(
                  Instant.parse(envelope.getCompletedDateTime())
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime());
            }
          } else if (DocuSignEnvelopeRepository.STATUS_DECLINED.equals(envelopeStatus)) {
            if (StringUtils.notEmpty(envelope.getDeclinedDateTime())) {
              docuSignEnvelope.setDeclinedDateTime(
                  Instant.parse(envelope.getDeclinedDateTime())
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime());
            }
          }
          docuSignEnvelopeRepo.save(docuSignEnvelope);

        } catch (ApiException e) {
          throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
        }
      }

    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.DOCUSIGN_ENVELOPE_SETTING_EMPTY));
    }
    return docuSignEnvelope;
  }

  protected void updateFields(EnvelopesApi envelopesApi, DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {
    try {
      List<DocuSignSigner> docuSignSigners = docuSignEnvelope.getDocuSignSignerList();
      if (CollectionUtils.isNotEmpty(docuSignSigners)) {
        for (DocuSignSigner docuSignSigner : docuSignSigners) {

          String recipientId = docuSignSigner.getRecipientId();
          Tabs tabs =
              envelopesApi.listTabs(
                  docuSignEnvelope.getDocuSignEnvelopeSetting().getDocuSignAccount().getAccountId(),
                  docuSignEnvelope.getEnvelopeId(),
                  recipientId);

          if (ObjectUtils.notEmpty(tabs)) {
            LOG.debug(tabs.toString());
          }
          if (CollectionUtils.isNotEmpty(docuSignSigner.getDocuSignFieldList())) {
            for (DocuSignField field : docuSignSigner.getDocuSignFieldList()) {
              updateField(field, tabs);
            }
          }
        }
      }
    } catch (ApiException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected void updateField(DocuSignField field, Tabs tabs) throws AxelorException {
    int typeSelect = field.getTypeSelect();
    switch (typeSelect) {
      case DocuSignFieldSettingRepository.TYPE_SIGN_HERE:
        DocuSignUtils.updateSignHereField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_FULL_NAME:
        DocuSignUtils.updateFullNameField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_EMAIL:
        DocuSignUtils.updateEmailField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_COMPANY:
        DocuSignUtils.updateCompanyField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_CHECKBOX:
        DocuSignUtils.updateCheckboxField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_RADIO_GROUP:
        DocuSignUtils.updateRadioGroupField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_LIST:
        DocuSignUtils.updateListField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_APPROVE:
        DocuSignUtils.updateApproveField(field, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_DECLINE:
        DocuSignUtils.updateDeclineField(field, tabs);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.DOCUSIGN_PARAM_ITEM_UNKNOWN_TYPE));
    }
  }

  protected void downloadDocumentsFile(EnvelopesApi envelopesApi, DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {
    try {
      EnvelopeDocumentsResult result =
          envelopesApi.listDocuments(
              docuSignEnvelope.getDocuSignEnvelopeSetting().getDocuSignAccount().getAccountId(),
              docuSignEnvelope.getEnvelopeId());
      if (ObjectUtils.notEmpty(result)) {
        if (CollectionUtils.isNotEmpty(result.getEnvelopeDocuments())) {
          for (EnvelopeDocument doc : result.getEnvelopeDocuments()) {
            if (CERTIFICATE_ID.equals(doc.getDocumentId())) {
              if (ObjectUtils.isEmpty(docuSignEnvelope.getCertificateMetaFile())) {
                if (ObjectUtils.notEmpty(docuSignEnvelope.getDocuSignEnvelopeSetting())
                    && ObjectUtils.notEmpty(
                        docuSignEnvelope.getDocuSignEnvelopeSetting().getDocuSignAccount())
                    && StringUtils.notEmpty(
                        docuSignEnvelope
                            .getDocuSignEnvelopeSetting()
                            .getDocuSignAccount()
                            .getAccountId())) {
                  byte[] results =
                      envelopesApi.getDocument(
                          docuSignEnvelope
                              .getDocuSignEnvelopeSetting()
                              .getDocuSignAccount()
                              .getAccountId(),
                          docuSignEnvelope.getEnvelopeId(),
                          CERTIFICATE_ID);
                  if (ObjectUtils.notEmpty(results)) {
                    String fileName = addExtension(CERTIFICATE_FILENAME, PDF_EXTENSION);
                    MetaFile certificateMetaFile =
                        metaFiles.upload(new ByteArrayInputStream(results), fileName);
                    docuSignEnvelope.setCertificateMetaFile(certificateMetaFile);
                  }
                }
              }
            } else {
              DocuSignDocument docuSignDocument =
                  docuSignEnvelope.getDocuSignDocumentList().stream()
                      .filter(
                          d ->
                              ObjectUtils.notEmpty(doc.getDocumentId())
                                  && doc.getDocumentId().equals(d.getDocumentId()))
                      .findFirst()
                      .orElse(null);
              if (ObjectUtils.notEmpty(docuSignDocument)
                  && ObjectUtils.isEmpty(docuSignDocument.getSignedMetaFile())) {
                if (ObjectUtils.notEmpty(docuSignEnvelope.getDocuSignEnvelopeSetting())
                    && ObjectUtils.notEmpty(
                        docuSignEnvelope.getDocuSignEnvelopeSetting().getDocuSignAccount())
                    && StringUtils.notEmpty(
                        docuSignEnvelope
                            .getDocuSignEnvelopeSetting()
                            .getDocuSignAccount()
                            .getAccountId())) {
                  byte[] results =
                      envelopesApi.getDocument(
                          docuSignEnvelope
                              .getDocuSignEnvelopeSetting()
                              .getDocuSignAccount()
                              .getAccountId(),
                          docuSignEnvelope.getEnvelopeId(),
                          doc.getDocumentId());
                  if (ObjectUtils.notEmpty(results)) {
                    String fileName = addExtension(doc.getName(), PDF_EXTENSION);
                    MetaFile signedMetaFile =
                        metaFiles.upload(new ByteArrayInputStream(results), fileName);
                    docuSignDocument.setSignedMetaFile(signedMetaFile);
                  }
                }
              }
            }
          }
        }
      }
    } catch (ApiException | IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected static String addExtension(String fileName, String extension) {
    if (FilenameUtils.isExtension(fileName, extension)) {
      return fileName;
    }
    return String.join(".", fileName, extension);
  }
}
