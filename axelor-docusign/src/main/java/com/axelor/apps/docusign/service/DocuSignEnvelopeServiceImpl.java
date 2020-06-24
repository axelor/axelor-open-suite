package com.axelor.apps.docusign.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.exceptions.IExceptionMessage;
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
import com.docusign.esign.model.UserInfo;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
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
          if(StringUtils.notEmpty(envelope.getEmailSubject())){
            TemplateMaker maker = new TemplateMaker(Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
            maker.setContext(model);
            maker.setTemplate(envelope.getEmailSubject());

            envelope.setEmailSubject(maker.make());
          }

          envelope.setRelatedTo(metaModel.getFullName());
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

  private DocuSignSigner createDocuSignSigner(
      DocuSignSignerSetting signerSetting, Context scriptContext) {
    DocuSignSigner docuSignSigner = new DocuSignSigner();
    docuSignSigner.setDocuSignSignerSetting(signerSetting);
    docuSignSigner.setName(signerSetting.getName());
    docuSignSigner.setSequence(signerSetting.getSequence());
    docuSignSigner.setIsGroup(signerSetting.getIsGroup());
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

  private DocuSignDocument createDocuSignDocument(
      DocuSignDocumentSetting documentSetting,
      Context scriptContext,
      List<DocuSignSigner> docuSignSignerList) {
    DocuSignDocument docuSignDocument = new DocuSignDocument();
    docuSignDocument.setName(documentSetting.getName());
    docuSignDocument.setDocumentId(documentSetting.getDocumentId());
    docuSignDocument.setFileExtension(documentSetting.getFileExtension());
    docuSignDocument.setDocuSignDocumentSetting(documentSetting);
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

  private DocuSignField createDocuSignField(
      DocuSignFieldSetting docuSignFieldSetting, List<DocuSignSigner> docuSignSignerList) {
    DocuSignField docuSignField = new DocuSignField();
    docuSignField.setDocuSignFieldSetting(docuSignFieldSetting);

    DocuSignSigner docuSignSigner =
        docuSignSignerList.stream()
            .filter(
                signerItem ->
                    signerItem
                        .getDocuSignSignerSetting()
                        .getRecipientId()
                        .equals(docuSignFieldSetting.getDocuSignSignerSetting().getRecipientId()))
            .findAny()
            .orElse(null);
    docuSignField.setDocuSignSigner(docuSignSigner);

    return docuSignField;
  }

  private void checkEventNotification(EnvelopeDefinition envelopeDefinition)
      throws AxelorException {
    EnvelopeEvent envelopeEvent = new EnvelopeEvent();
    envelopeEvent.setEnvelopeEventStatusCode("sent");
    // envelopeEvent.setEnvelopeEventStatusCode("delivered");
    envelopeEvent.setEnvelopeEventStatusCode("completed");
    // envelopeEvent.setEnvelopeEventStatusCode("declined");
    // envelopeEvent.setEnvelopeEventStatusCode("voided");
    List<EnvelopeEvent> envelopeEvents = new ArrayList<>();
    envelopeEvents.add(envelopeEvent);

    RecipientEvent recipientEvent = new RecipientEvent();
    recipientEvent.setRecipientEventStatusCode("delivered");
    recipientEvent.setRecipientEventStatusCode("completed");
    /*
    recipientEvent.setRecipientEventStatusCode("Sent");
    recipientEvent.setRecipientEventStatusCode("Delivered");
    recipientEvent.setRecipientEventStatusCode("Completed");
    recipientEvent.setRecipientEventStatusCode("Declined");
    recipientEvent.setRecipientEventStatusCode("AuthenticationFailed");
    recipientEvent.setRecipientEventStatusCode("AutoResponded");*/
    List<RecipientEvent> recipientEvents = new ArrayList<>();
    recipientEvents.add(recipientEvent);

    EventNotification eventNotification = new EventNotification();
    // String webhookUrl = AppSettings.get().getBaseURL() + "/#/ws/public/docusign/update-envelope";
    // String webhookUrl = "http://83.199.218.106/axelor-erp" +
    // "/ws/public/docusign/update-envelope";
    String webhookUrl = "http://c596af8a.proxy.webhookapp.com";
    eventNotification.setUrl(webhookUrl);
    eventNotification.setLoggingEnabled("false");
    eventNotification.setRequireAcknowledgment("false");
    eventNotification.setIncludeDocuments("false");
    /*
    eventNotification.setLoggingEnabled("true");
    eventNotification.setRequireAcknowledgment("true");
    eventNotification.setUseSoapInterface("false");
    eventNotification.setIncludeCertificateWithSoap("false");
    eventNotification.setSignMessageWithX509Cert("false");
    eventNotification.setIncludeDocuments("true");
    eventNotification.setIncludeEnvelopeVoidReason("true");
    eventNotification.setIncludeTimeZone("true");
    eventNotification.setIncludeSenderAccountAsCustomField("true");
    eventNotification.setIncludeDocumentFields("true");
    eventNotification.setIncludeCertificateOfCompletion("true");*/
    eventNotification.setEnvelopeEvents(envelopeEvents);
    eventNotification.setRecipientEvents(recipientEvents);

    envelopeDefinition.setEventNotification(eventNotification);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public DocuSignEnvelope sendEnvelope(
          DocuSignEnvelope docuSignEnvelope, DocuSignAccount docuSignAccount) throws AxelorException {
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

  private EnvelopeDefinition createEnvelopeDefinition(
          DocuSignEnvelopeSetting envelopeSetting, DocuSignEnvelope docuSignEnvelope)
          throws AxelorException {

    EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
    envelopeDefinition.setEmailSubject(envelopeSetting.getEmailSubject());

    List<DocuSignDocument> docuSignDocumentList = docuSignEnvelope.getDocuSignDocumentList();
    List<Document> documentList = createDocuments(docuSignDocumentList);
    envelopeDefinition.setDocuments(documentList);

    // List<Signer> signerList = createSigners(docuSignDocumentList);
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

    checkEventNotification(envelopeDefinition);

    envelopeDefinition.setStatus(DocuSignEnvelopeRepository.STATUS_SENT);

    return envelopeDefinition;
  }

  private List<Document> createDocuments(List<DocuSignDocument> docuSignDocumentList)
          throws AxelorException {
    List<Document> documentList = null;

    if (CollectionUtils.isNotEmpty(docuSignDocumentList)) {
      documentList = new ArrayList<>();

      for (DocuSignDocument docuSignDocument : docuSignDocumentList) {

        if (ObjectUtils.notEmpty(docuSignDocument.getDocuSignDocumentSetting())
                && ObjectUtils.notEmpty(docuSignDocument.getUnsignedMetaFile())) {
          documentList.add(
                  createDocument(
                          docuSignDocument.getDocuSignDocumentSetting(),
                          docuSignDocument.getUnsignedMetaFile()));
        }
      }
    }
    return documentList;
  }

  private Document createDocument(DocuSignDocument docuSignDocument, MetaFile metaFile)
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

  private List<Signer> createSigners(List<DocuSignSigner> docuSignSignerList)
          throws AxelorException {
    List<Signer> signerList = null;

    if (CollectionUtils.isNotEmpty(docuSignSignerList)) {
      signerList = new ArrayList<>();

      for (DocuSignSigner docuSignSigner : docuSignSignerList) {
        Partner signerPartner = docuSignSigner.getSigner();
        String recipientId = docuSignSigner.getDocuSignSignerSetting().getRecipientId();

        if (!docuSignSigner.getIsInPersonSigner()) {
          Signer signer = new Signer();
          signer.setRecipientId(recipientId);
          signer.setAccessCode(docuSignSigner.getAccessCode());
          if (ObjectUtils.notEmpty(
                  docuSignSigner.getDocuSignSignerSetting().getDocuSignEnvelopeSetting())
                  && docuSignSigner
                  .getDocuSignSignerSetting()
                  .getDocuSignEnvelopeSetting()
                  .getIsOrderedSigners()) {
            signer.setRoutingOrder(String.valueOf(docuSignSigner.getSequence() + 1));
          }
          if (docuSignSigner.getIsGroup()) {
            List<UserInfo> userInfoList = createUserInfoList(docuSignSigner.getSignerSet());
            // signer.setSigningGroupId(docuSignSigner.getDocuSignSignerSetting().getName());
            signer.setSigningGroupName(docuSignSigner.getDocuSignSignerSetting().getName());
            signer.setIsBulkRecipient("true");
            signer.setSigningGroupUsers(userInfoList);
            signer.setEmail("rabearijao.stephane@gmail.com");
            signer.setName("nom test");
          } else {
            if (ObjectUtils.notEmpty(signerPartner.getEmailAddress())) {
              signer.setEmail(signerPartner.getEmailAddress().getAddress());
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_INCONSISTENCY,
                      I18n.get(IExceptionMessage.DOCUSIGN_EMAIL_ADDRESS_EMPTY));
            }
            signer.setName(signerPartner.getSimpleFullName());
          }

          signerList.add(signer);
        }
      }
    }

    return signerList;
  }

  private List<UserInfo> createUserInfoList(Collection<Partner> signerSet) throws AxelorException {
    List<UserInfo> userInfoList = null;
    if (CollectionUtils.isNotEmpty(signerSet)) {
      userInfoList = new ArrayList<>();
      for (Partner signerPartner : signerSet) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(signerPartner.getSimpleFullName());
        if (ObjectUtils.notEmpty(signerPartner.getEmailAddress())) {
          userInfo.setEmail(signerPartner.getEmailAddress().getAddress());
        } else {
          throw new AxelorException(
                  TraceBackRepository.CATEGORY_INCONSISTENCY,
                  I18n.get(IExceptionMessage.DOCUSIGN_EMAIL_ADDRESS_EMPTY));
        }
        userInfoList.add(userInfo);
      }
    }
    return userInfoList;
  }

  private List<InPersonSigner> createInPersonSigners(List<DocuSignSigner> docuSignSignerList)
          throws AxelorException {
    List<InPersonSigner> inPersonSignerList = null;

    if (CollectionUtils.isNotEmpty(docuSignSignerList)) {
      inPersonSignerList = new ArrayList<>();

      for (DocuSignSigner docuSignSigner : docuSignSignerList) {
        Partner signerPartner = docuSignSigner.getSigner();
        String recipientId = docuSignSigner.getDocuSignSignerSetting().getRecipientId();

        if (docuSignSigner.getIsInPersonSigner()) {
          InPersonSigner inPersonSigner = new InPersonSigner();
          inPersonSigner.setRecipientId(recipientId);
          inPersonSigner.setAccessCode(docuSignSigner.getAccessCode());
          if (ObjectUtils.notEmpty(
                  docuSignSigner.getDocuSignSignerSetting().getDocuSignEnvelopeSetting())
                  && docuSignSigner
                  .getDocuSignSignerSetting()
                  .getDocuSignEnvelopeSetting()
                  .getIsOrderedSigners()) {
            inPersonSigner.setRoutingOrder(
                    String.valueOf(docuSignSigner.getDocuSignSignerSetting().getSequence()));
          }
          if (docuSignSigner.getIsGroup()) {
            List<UserInfo> userInfoList = createUserInfoList(docuSignSigner.getSignerSet());
            inPersonSigner.setSigningGroupUsers(userInfoList);
          } else {
            if (ObjectUtils.notEmpty(signerPartner.getEmailAddress())) {
              inPersonSigner.setHostEmail(signerPartner.getEmailAddress().getAddress());
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_INCONSISTENCY,
                      I18n.get(IExceptionMessage.DOCUSIGN_EMAIL_ADDRESS_EMPTY));
            }
            inPersonSigner.setHostName(signerPartner.getSimpleFullName());
          }

          inPersonSignerList.add(inPersonSigner);
        }
      }
    }

    return inPersonSignerList;
  }

  private void updateSigners(
          List<Signer> signerList,
          List<InPersonSigner> inPersonSignerList,
          List<DocuSignDocument> docuSignDocumentList)
          throws AxelorException {

    if (CollectionUtils.isNotEmpty(docuSignDocumentList)) {

      for (DocuSignDocument docuSignDocument : docuSignDocumentList) {

        if (CollectionUtils.isNotEmpty(docuSignDocument.getDocuSignFieldList())) {
          for (DocuSignField docuSignField : docuSignDocument.getDocuSignFieldList()) {
            String recipientId =
                    docuSignField.getDocuSignSigner().getDocuSignSignerSetting().getRecipientId();
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

  private InPersonSigner findInPersonSigner(
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

  private InPersonSigner updateInPersonSigner(
          InPersonSigner inPersonSigner, DocuSignField docuSignField) throws AxelorException {

    if (ObjectUtils.notEmpty(docuSignField.getDocuSignFieldSetting())
            && ObjectUtils.notEmpty(docuSignField.getDocuSignSigner())) {
      DocuSignFieldSetting docuSignFieldSetting = docuSignField.getDocuSignFieldSetting();
      DocuSignSigner docuSignSigner = docuSignField.getDocuSignSigner();
      Partner signerPartner = docuSignSigner.getSigner();
      Company company = docuSignSigner.getCompany();
      Tabs tabs = inPersonSigner.getTabs();
      if (ObjectUtils.isEmpty(tabs)) {
        tabs = new Tabs();
        inPersonSigner.setTabs(tabs);
      }

      String documentId =
              docuSignField.getDocuSignDocument().getDocuSignDocumentSetting().getDocumentId();
      processItem(
              tabs,
              docuSignFieldSetting,
              documentId,
              inPersonSigner.getRecipientId(),
              signerPartner,
              company);
    }

    return inPersonSigner;
  }

  private Signer findSigner(List<Signer> signerList, String recipientId) {
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

  private Signer updateSigner(Signer signer, DocuSignField docuSignField) throws AxelorException {

    if (ObjectUtils.notEmpty(docuSignField.getDocuSignFieldSetting())
            && ObjectUtils.notEmpty(docuSignField.getDocuSignSigner())) {
      DocuSignFieldSetting docuSignFieldSetting = docuSignField.getDocuSignFieldSetting();
      DocuSignSigner docuSignSigner = docuSignField.getDocuSignSigner();
      Partner signerPartner = docuSignSigner.getSigner();
      Company company = docuSignSigner.getCompany();
      Tabs tabs = signer.getTabs();
      if (ObjectUtils.isEmpty(tabs)) {
        tabs = new Tabs();
        signer.setTabs(tabs);
      }

      String documentId =
              docuSignField.getDocuSignDocument().getDocuSignDocumentSetting().getDocumentId();
      processItem(
              tabs, docuSignFieldSetting, documentId, signer.getRecipientId(), signerPartner, company);
    }

    return signer;
  }

  private void processItem(
          Tabs tabs,
          DocuSignFieldSetting docuSignFieldSetting,
          String documentId,
          String recipientId,
          Partner partner,
          Company company)
          throws AxelorException {

    switch (docuSignFieldSetting.getTypeSelect()) {
      case DocuSignFieldSettingRepository.TYPE_SIGN_HERE:
        DocuSignUtils.addSignHere(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_FULL_NAME:
        DocuSignUtils.addFullName(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_EMAIL:
        String email = null;
        if (ObjectUtils.notEmpty(partner) && ObjectUtils.notEmpty(partner.getEmailAddress())) {
          email = partner.getEmailAddress().getAddress();
        }
        DocuSignUtils.addEmail(tabs, docuSignFieldSetting, documentId, recipientId, email);
        break;
      case DocuSignFieldSettingRepository.TYPE_COMPANY:
        String companyName = null;
        if (ObjectUtils.notEmpty(company)) {
          companyName = company.getName();
        }
        DocuSignUtils.addCompany(tabs, docuSignFieldSetting, documentId, recipientId, companyName);
        break;
      case DocuSignFieldSettingRepository.TYPE_CHECKBOX:
        DocuSignUtils.addCheckbox(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_RADIO_GROUP:
        DocuSignUtils.addRadioGroup(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_LIST:
        DocuSignUtils.addList(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_APPROVE:
        DocuSignUtils.addApprove(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      case DocuSignFieldSettingRepository.TYPE_DECLINE:
        DocuSignUtils.addDecline(tabs, docuSignFieldSetting, documentId, recipientId);
        break;
      default:
        throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.DOCUSIGN_PARAM_ITEM_UNKNOWN_TYPE));
    }
  }

  private EnvelopesApi getEnvelopesApi(DocuSignAccount docuSignAccount) throws AxelorException {

    EnvelopesApi envelopesApi = null;
    if (ObjectUtils.notEmpty(docuSignAccount)) {
      ApiClient apiClient = new ApiClient();
      apiClient.addDefaultHeader("Authorization", "Bearer " + docuSignAccount.getAccessToken());
      apiClient.setBasePath(docuSignAccount.getBasePath());
      envelopesApi = new EnvelopesApi(apiClient);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY, IExceptionMessage.DOCUSIGN_ACCOUNT_EMPTY);
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

  private void updateFields(EnvelopesApi envelopesApi, DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {
    try {
      List<DocuSignSigner> docuSignSigners = docuSignEnvelope.getDocuSignSignerList();
      if (CollectionUtils.isNotEmpty(docuSignSigners)) {
        for (DocuSignSigner docuSignSigner : docuSignSigners) {
          if (ObjectUtils.notEmpty(docuSignSigner.getDocuSignSignerSetting())) {
            String recipientId = docuSignSigner.getDocuSignSignerSetting().getRecipientId();
            Tabs tabs =
                envelopesApi.listTabs(
                    docuSignEnvelope
                        .getDocuSignEnvelopeSetting()
                        .getDocuSignAccount()
                        .getAccountId(),
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
      }
    } catch (ApiException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  private void updateField(DocuSignField field, Tabs tabs) throws AxelorException {
    DocuSignFieldSetting fieldSetting = field.getDocuSignFieldSetting();
    int typeSelect = fieldSetting.getTypeSelect();
    switch (typeSelect) {
      case DocuSignFieldSettingRepository.TYPE_SIGN_HERE:
        DocuSignUtils.updateSignHereField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_FULL_NAME:
        DocuSignUtils.updateFullNameField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_EMAIL:
        DocuSignUtils.updateEmailField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_COMPANY:
        DocuSignUtils.updateCompanyField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_CHECKBOX:
        DocuSignUtils.updateCheckboxField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_RADIO_GROUP:
        DocuSignUtils.updateRadioGroupField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_LIST:
        DocuSignUtils.updateListField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_APPROVE:
        DocuSignUtils.updateApproveField(field, fieldSetting, tabs);
        break;
      case DocuSignFieldSettingRepository.TYPE_DECLINE:
        DocuSignUtils.updateDeclineField(field, fieldSetting, tabs);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.DOCUSIGN_PARAM_ITEM_UNKNOWN_TYPE));
    }
  }

  private void downloadDocumentsFile(EnvelopesApi envelopesApi, DocuSignEnvelope docuSignEnvelope)
      throws AxelorException {
    try {
      EnvelopeDocumentsResult result =
          envelopesApi.listDocuments(
              docuSignEnvelope.getDocuSignEnvelopeSetting().getDocuSignAccount().getAccountId(),
              docuSignEnvelope.getEnvelopeId());
      if (ObjectUtils.notEmpty(result)) {
        if (CollectionUtils.isNotEmpty(result.getEnvelopeDocuments())) {
          LOG.debug("Envelope documents : /n" + result.getEnvelopeDocuments().toString());
          for (EnvelopeDocument doc : result.getEnvelopeDocuments()) {
            if (CERTIFICATE_ID.equals(doc.getDocumentId())) {
              if (ObjectUtils.isEmpty(docuSignEnvelope.getCertificateMetaFile())) {
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
            } else {
              DocuSignDocument docuSignDocument =
                  docuSignEnvelope.getDocuSignDocumentList().stream()
                      .filter(
                          d ->
                              doc.getDocumentId()
                                  .equals(d.getDocuSignDocumentSetting().getDocumentId()))
                      .findFirst()
                      .orElse(null);
              if (ObjectUtils.notEmpty(docuSignDocument)
                  && ObjectUtils.isEmpty(docuSignDocument.getSignedMetaFile())) {
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
    } catch (ApiException | IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  private static String addExtension(String fileName, String extension) {
    if (FilenameUtils.isExtension(fileName, extension)) {
      return fileName;
    }
    return String.join(".", fileName, extension);
  }
}
