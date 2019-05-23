/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sendinblue.service;

import com.axelor.apps.base.db.AppSendinblue;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AppSendinblueRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.marketing.db.Campaign;
import com.axelor.apps.marketing.db.SendinBlueCampaign;
import com.axelor.apps.marketing.db.repo.CampaignRepository;
import com.axelor.apps.marketing.db.repo.SendinBlueCampaignRepository;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.sendinblue.db.SendinBlueCampaignStat;
import com.axelor.apps.sendinblue.db.SendinBlueContactStat;
import com.axelor.apps.sendinblue.db.SendinBlueEvent;
import com.axelor.apps.sendinblue.db.SendinBlueReport;
import com.axelor.apps.sendinblue.db.repo.SendinBlueCampaignStatRepository;
import com.axelor.apps.sendinblue.db.repo.SendinBlueContactStatRepository;
import com.axelor.apps.sendinblue.db.repo.SendinBlueEventRepository;
import com.axelor.apps.sendinblue.db.repo.SendinBlueReportRepository;
import com.axelor.apps.sendinblue.translation.ITranslation;
import com.axelor.apps.tool.service.TranslationService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.ApiResponse;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.AccountApi;
import sibApi.AttributesApi;
import sibApi.ContactsApi;
import sibApi.EmailCampaignsApi;
import sibApi.SmtpApi;
import sibModel.AddContactToList;
import sibModel.CreateAttribute;
import sibModel.CreateAttribute.TypeEnum;
import sibModel.CreateContact;
import sibModel.CreateEmailCampaign;
import sibModel.CreateEmailCampaignRecipients;
import sibModel.CreateEmailCampaignSender;
import sibModel.CreateList;
import sibModel.CreateModel;
import sibModel.CreateSmtpTemplate;
import sibModel.CreateSmtpTemplateSender;
import sibModel.CreateUpdateFolder;
import sibModel.GetAccount;
import sibModel.GetAttributes;
import sibModel.GetAttributesAttributes;
import sibModel.GetContactCampaignStats;
import sibModel.GetContactCampaignStatsClicked;
import sibModel.GetContactCampaignStatsOpened;
import sibModel.GetContactCampaignStatsUnsubscriptions;
import sibModel.GetEmailCampaign;
import sibModel.GetEmailEventReport;
import sibModel.GetEmailEventReportEvents;
import sibModel.GetExtendedContactDetails;
import sibModel.GetExtendedContactDetailsStatisticsLinks;
import sibModel.GetExtendedContactDetailsStatisticsMessagesSent;
import sibModel.GetExtendedContactDetailsStatisticsUnsubscriptionsAdminUnsubscription;
import sibModel.GetExtendedContactDetailsStatisticsUnsubscriptionsUserUnsubscription;
import sibModel.GetFolderLists;
import sibModel.GetFolders;
import sibModel.GetReports;
import sibModel.GetReportsReports;
import sibModel.GetSmtpTemplateOverview;
import sibModel.UpdateContact;
import sibModel.UpdateEmailCampaign;
import sibModel.UpdateEmailCampaignRecipients;
import sibModel.UpdateEmailCampaignSender;
import sibModel.UpdateSmtpTemplate;
import sibModel.UpdateSmtpTemplateSender;

public class AppSendinBlueServiceImpl implements AppSendinBlueService {

  @Inject SendinBlueCampaignStatRepository sendinBlueCampaignStatRepo;
  @Inject SendinBlueContactStatRepository sendinBlueContactStatRepo;

  @Inject TranslationService translationService;
  @Inject UserService userService;

  protected static final String ATTRIBUTE_CATEGORY = "normal";
  protected static final Integer DATA_FETCH_LIMIT = 100;

  @SuppressWarnings("rawtypes")
  protected static final List<Class> SIMPLE_CLASSES =
      new ArrayList<>(
          Arrays.asList(String.class, Integer.class, Long.class, BigDecimal.class, Boolean.class));

  @SuppressWarnings("rawtypes")
  protected static final List<Class> CALENDAR_CLASSES =
      new ArrayList<>(
          Arrays.asList(
              LocalDate.class, LocalTime.class, LocalDateTime.class, ZonedDateTime.class));

  protected static List<String> attributeList = new ArrayList<>();

  protected static String userLanguage;

  CreateAttribute createAttribute;

  protected AttributesApi apiInstance;
  protected boolean isRelational = false;
  protected List<String> metaModels = new ArrayList<String>(Arrays.asList("Partner", "Lead"));
  protected ArrayList<Long> partnerRecipients, leadRecipients;

  public ApiKeyAuth getApiKeyAuth() throws AxelorException {
    AppSendinblue appSendinblue = Beans.get(AppSendinblueRepository.class).all().fetchOne();
    String apiKeyStr = appSendinblue.getApiKey();

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
    apiKey.setApiKey(apiKeyStr);

    AccountApi apiInstance = new AccountApi();
    try {
      GetAccount result = apiInstance.getAccount();
      if (result == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ITranslation.AUTHENTICATE_ERROR));
      }
    } catch (ApiException e) {
      if (e.getMessage().contains("Unauthorized")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ITranslation.AUTHENTICATE_ERROR));
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getLocalizedMessage());
    }

    return apiKey;
  }

  public void exportContactFields() throws AxelorException {
    getApiKeyAuth();
    apiInstance = new AttributesApi();
    createAttribute = new CreateAttribute();
    try {
      GetAttributes result = apiInstance.getAttributes();
      List<GetAttributesAttributes> attributes = result.getAttributes();
      attributeList =
          attributes.stream().map(GetAttributesAttributes::getName).collect(Collectors.toList());
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
    for (String metaModel : metaModels) {
      exportMetaModelFields(metaModel);
    }
  }

  protected void exportMetaModelFields(String metaModelName) {
    MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(metaModelName);
    if (metaModel != null) {
      if (metaModelName.equals("Lead")) {
        createContactAttribute("isLead", CreateAttribute.TypeEnum.BOOLEAN);
      }
      List<MetaField> modelFields =
          Beans.get(MetaFieldRepository.class)
              .all()
              .filter("self.metaModel = ?", metaModel)
              .fetch();
      for (MetaField metaField : modelFields) {
        isRelational = false;
        TypeEnum fieldType = getAttributeType(metaField);
        if (fieldType != null) {
          String attributeName;
          if (isRelational) {
            attributeName = metaField.getName() + "Name";
          } else {
            attributeName = metaField.getName();
          }
          createContactAttribute(attributeName, fieldType);
        }
      }
    }
  }

  protected void createContactAttribute(String attributeName, TypeEnum fieldType) {
    createAttribute.setType(fieldType);
    try {
      if (!attributeList.contains(attributeName.toUpperCase())) {
        apiInstance.createAttribute(ATTRIBUTE_CATEGORY, attributeName, createAttribute);
        attributeList.add(attributeName.toUpperCase());
      }
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  protected TypeEnum getAttributeType(MetaField metaField) {
    String fieldType = metaField.getTypeName();
    switch (fieldType) {
      case "String":
        return CreateAttribute.TypeEnum.TEXT;
      case "Boolean":
        return CreateAttribute.TypeEnum.BOOLEAN;
      case "LocalDateTime":
      case "LocalDate":
      case "LocalTime":
      case "ZonedDateTime":
        return CreateAttribute.TypeEnum.DATE;
      case "Integer":
      case "Long":
      case "BigDecimal":
        return CreateAttribute.TypeEnum.FLOAT;
      default:
        String relationship = metaField.getRelationship();
        if (relationship != null
            && (relationship.equals("ManyToOne") || relationship.equals("OneToOne"))) {
          try {
            String classStr = metaField.getPackageName() + "." + metaField.getTypeName();
            @SuppressWarnings("unchecked")
            Class<Model> klass = (Class<Model>) Class.forName(classStr);
            List<Field> fields = FieldUtils.getFieldsListWithAnnotation(klass, NameColumn.class);
            if (fields != null && !fields.isEmpty()) {
              isRelational = true;
              return CreateAttribute.TypeEnum.TEXT;
            }
          } catch (ClassNotFoundException e) {
            TraceBackService.trace(e);
          }
        }
        return null;
    }
  }

  @Override
  public void exportContacts(AppSendinblue appSendinblue) throws AxelorException {
    getApiKeyAuth();
    userLanguage = userService.getUser().getLanguage();
    ContactsApi apiInstance = new ContactsApi();
    Long folderId = createFolder(apiInstance);
    Long listId = null;
    if (appSendinblue.getPartnerSet() != null && !appSendinblue.getPartnerSet().isEmpty()) {
      if (folderId != null) {
        listId = createList("Partner", apiInstance, folderId);
      }
      exportMetaModel(appSendinblue.getPartnerSet(), apiInstance, listId);
    }
    if (appSendinblue.getLeadSet() != null && !appSendinblue.getLeadSet().isEmpty()) {
      if (folderId != null) {
        listId = createList("Lead", apiInstance, folderId);
      }
      exportMetaModel(appSendinblue.getLeadSet(), apiInstance, listId);
    }
  }

  private void exportMetaModel(Object contactSet, ContactsApi apiInstance, Long listId) {
    try {
      @SuppressWarnings("unchecked")
      Set<AuditableModel> objSet = (Set<AuditableModel>) contactSet;
      AuditableModel obj = objSet.stream().findFirst().get();
      Mapper metaModelMapper = Mapper.of(obj.getClass());
      Property[] properties = metaModelMapper.getProperties();
      for (AuditableModel dataObject : objSet) {
        exportContactDataObject(
            dataObject,
            properties,
            metaModelMapper,
            obj.getClass().getSimpleName(),
            apiInstance,
            listId);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private Long createFolder(ContactsApi apiInstance) {
    try {
      Long folderId = getABSFolderId(apiInstance);
      if (folderId != null) {
        return folderId;
      }

      CreateUpdateFolder folder = new CreateUpdateFolder();
      folder.setName("ABS");
      CreateModel result = apiInstance.createFolder(folder);
      return result.getId();

    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  private Long getABSFolderId(ContactsApi apiInstance) {
    try {
      GetFolders folders = apiInstance.getFolders(2L, 0L);
      List<Object> folderList = folders.getFolders();
      for (Object object : folderList) {
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) object;
        if (obj.containsKey("name") && obj.get("name").equals("ABS") && obj.containsKey("id")) {
          Integer id = ((Double) obj.get("id")).intValue();
          return Long.parseLong(id.toString());
        }
      }
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  private Long createList(String metaModelName, ContactsApi apiInstance, Long folderId) {
    try {
      GetFolderLists lists =
          apiInstance.getFolderLists(
              folderId, null, null); // Long.parseLong(DATA_FETCH_LIMIT.toString()), 0L);
      List<Object> listList = lists.getLists();
      for (Object object : listList) {
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) object;
        if (obj.containsKey("name")
            && obj.get("name").equals(metaModelName + "List")
            && obj.containsKey("id")) {
          Integer id = ((Double) obj.get("id")).intValue();
          return Long.parseLong(id.toString());
        }
      }

      CreateList list = new CreateList();
      list.setName(metaModelName + "List");
      list.setFolderId(folderId);
      CreateModel result = apiInstance.createList(list);
      return result.getId();
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  private void exportContactDataObject(
      Object dataObject,
      Property[] properties,
      Mapper metaModelMapper,
      String metaModelName,
      ContactsApi apiInstance,
      Long listId) {
    EmailAddress emailAddress = (EmailAddress) metaModelMapper.get(dataObject, "emailAddress");
    if (emailAddress != null) {
      String emailAddressStr = emailAddress.getAddress();
      if (!StringUtils.isBlank(emailAddressStr) && isValid(emailAddressStr)) {
        List<Long> listIds = null;
        Map<String, Object> attributes = getAttributes(properties, metaModelName, dataObject);
        GetExtendedContactDetails existEmail = null;
        try {
          existEmail = apiInstance.getContactInfo(emailAddressStr);
          listIds = existEmail.getListIds();
          if (!listIds.contains(listId)) {
            listIds.add(listId);
          }
        } catch (ApiException e) {
        }

        if (existEmail == null) {
          createContact(dataObject, emailAddressStr, attributes, apiInstance, listId);
        } else {
          updateContact(emailAddressStr, attributes, apiInstance, listIds);
        }
      }
    }
  }

  private Map<String, Object> getAttributes(
      Property[] properties, String metaModelName, Object dataObject) {
    Map<String, Object> attributes = new HashMap<>();
    if (metaModelName.equals("Lead")) {
      attributes.put("isLead", true);
    }
    for (Property property : properties) {
      addAttribute(property, dataObject, attributes);
    }
    return attributes;
  }

  @Transactional
  public void createContact(
      Object dataObject,
      String emailAddressStr,
      Map<String, Object> attributes,
      ContactsApi apiInstance,
      Long listId) {
    CreateContact createContact = new CreateContact();
    createContact.setEmail(emailAddressStr);
    createContact.setAttributes(attributes);
    if (listId != null) {
      createContact.setListIds(new ArrayList<Long>(Arrays.asList(listId)));
    }
    try {
      CreateModel result = apiInstance.createContact(createContact);
      try {
        Method setMethod = dataObject.getClass().getMethod("setSendinBlueId", Long.class);
        setMethod.invoke(dataObject, result.getId());
        JPA.save((AuditableModel) dataObject);
      } catch (Exception e) {
      }
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  private void updateContact(
      String emailAddressStr,
      Map<String, Object> attributes,
      ContactsApi apiInstance,
      List<Long> listIds) {
    UpdateContact updateContact = new UpdateContact();
    updateContact.setAttributes(attributes);
    if (listIds != null && !listIds.isEmpty()) {
      updateContact.setListIds(listIds);
    }
    try {
      apiInstance.updateContact(emailAddressStr, updateContact);
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  private boolean isValid(String email) {
    String emailRegex =
        "^[a-zA-Z0-9_+&*-]+(?:\\."
            + "[a-zA-Z0-9_+&*-]+)*@"
            + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
            + "A-Z]{2,7}$";
    Pattern pat = Pattern.compile(emailRegex);
    if (email == null) return false;
    return pat.matcher(email).matches();
  }

  private void addAttribute(Property property, Object dataObject, Map<String, Object> attributes) {
    Object value = property.get(dataObject);
    if (value != null) {
      if (SIMPLE_CLASSES.contains(property.getJavaType())) {
        value = translationService.getValueTranslation(value.toString(), userLanguage);
        attributes.put(property.getName(), value);
      } else if (CALENDAR_CLASSES.contains(property.getJavaType())) {
        try {
          attributes.put(
              property.getName(), getFormatedDate(value, property.getJavaType().getSimpleName()));
        } catch (Exception e) {
          TraceBackService.trace(e);
        }
      } else {
        List<Field> fields =
            FieldUtils.getFieldsListWithAnnotation(property.getJavaType(), NameColumn.class);
        if (fields != null && !fields.isEmpty()) {
          Field field = fields.get(0);
          try {
            field.setAccessible(true);
            Object nameColumnValue = field.get(value);
            field.setAccessible(false);
            if (nameColumnValue != null) {
              nameColumnValue =
                  translationService.getValueTranslation(nameColumnValue.toString(), userLanguage);
              attributes.put(property.getName() + "Name", nameColumnValue);
            }
          } catch (IllegalArgumentException | IllegalAccessException e) {
            TraceBackService.trace(e);
          }
        }
      }
    }
  }

  private String getFormatedDate(Object value, String type) throws ParseException {
    try {
      Date outputDate = null;
      String outputStr = null;
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      switch (type) {
        case "LocalDateTime":
          LocalDateTime ldtValue = (LocalDateTime) value;
          outputDate = Date.from(ldtValue.atZone(ZoneId.systemDefault()).toInstant());
          break;
        case "LocalDate":
          LocalDate ldValue = (LocalDate) value;
          outputDate = Date.from(ldValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
          break;
        case "ZonedDateTime":
          ZonedDateTime zdtValue = (ZonedDateTime) value;
          outputDate = Date.from(zdtValue.toInstant());
          break;
      }
      outputStr = sdf.format(outputDate);
      return outputStr;
    } catch (Exception e) {
    }
    return null;
  }

  public void exportTemplate() throws AxelorException {
    getApiKeyAuth();
    userLanguage = userService.getUser().getLanguage();
    SmtpApi apiInstance = new SmtpApi();
    Query<Template> templateQuery =
        Beans.get(TemplateRepository.class)
            .all()
            .filter(
                "self.mediaTypeSelect IN (4) AND self.metaModel.id IN (SELECT id from MetaModel WHERE name IN ('Partner','Lead'))");
    if (templateQuery != null) {
      int totalTemplate = (int) templateQuery.count();
      List<Template> templates;
      int offset = 0;
      while (totalTemplate > 0) {
        templates = templateQuery.fetch(DATA_FETCH_LIMIT, offset);
        if (templates != null) {
          totalTemplate = templates.size();
          if (!templates.isEmpty()) {
            offset += totalTemplate;
            for (Template dataObject : templates) {
              exportTemplateDataObject(dataObject, apiInstance);
            }
          }
        }
      }
    }
  }

  private void exportTemplateDataObject(Template dataObject, SmtpApi apiInstance) {
    if (dataObject != null) {
      if (Optional.ofNullable(dataObject.getSendinBlueId()).orElse(0L) != 0) {
        GetSmtpTemplateOverview result = null;
        try {
          result = apiInstance.getSmtpTemplate(dataObject.getSendinBlueId());
        } catch (ApiException e) {
        }
        if (result != null) {
          updateTemplate(dataObject, apiInstance);
        } else {
          createTemplate(dataObject, apiInstance);
        }
      } else {
        createTemplate(dataObject, apiInstance);
      }
    }
  }

  @Transactional
  public void createTemplate(Template dataObject, SmtpApi apiInstance) {
    if (StringUtils.isBlank(dataObject.getFromAdress())) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(ITranslation.TEMPLATE_SENDER_ERROR)));
      return;
    }
    CreateSmtpTemplateSender sender = new CreateSmtpTemplateSender();
    sender.setEmail(dataObject.getFromAdress());
    sender.setName(dataObject.getFromAdress());

    CreateSmtpTemplate smtpTemplate = new CreateSmtpTemplate();

    smtpTemplate.setTemplateName(
        translationService.getValueTranslation(dataObject.getName(), userLanguage));
    smtpTemplate.setSubject(
        translationService.getValueTranslation(dataObject.getSubject(), userLanguage));
    smtpTemplate.setToField(dataObject.getToRecipients());
    smtpTemplate.setReplyTo(dataObject.getReplyToRecipients());
    smtpTemplate.setSender(sender);
    smtpTemplate.setIsActive(true);

    String content = getSendinBlueContent(dataObject.getContent());
    smtpTemplate.setHtmlContent(content);

    try {
      CreateModel result = apiInstance.createSmtpTemplate(smtpTemplate);
      dataObject.setSendinBlueId(result.getId());
      Beans.get(TemplateRepository.class).save(dataObject);
    } catch (ApiException e) {
      TraceBackService.trace(e);
    } catch (Exception e) {
    }
  }

  private void updateTemplate(Template dataObject, SmtpApi apiInstance) {
    if (StringUtils.isBlank(dataObject.getFromAdress())) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(ITranslation.TEMPLATE_SENDER_ERROR)));
      return;
    }
    UpdateSmtpTemplateSender sender = new UpdateSmtpTemplateSender();
    sender.setEmail(dataObject.getFromAdress());
    sender.setName(dataObject.getFromAdress());

    UpdateSmtpTemplate smtpTemplate = new UpdateSmtpTemplate();
    smtpTemplate.setTemplateName(
        translationService.getValueTranslation(dataObject.getName(), userLanguage));
    smtpTemplate.setSubject(
        translationService.getValueTranslation(dataObject.getSubject(), userLanguage));
    smtpTemplate.setToField(dataObject.getToRecipients());
    smtpTemplate.setReplyTo(dataObject.getReplyToRecipients());
    smtpTemplate.setSender(sender);
    smtpTemplate.setIsActive(true);

    String content = getSendinBlueContent(dataObject.getContent());
    smtpTemplate.setHtmlContent(content);

    try {
      apiInstance.updateSmtpTemplate(dataObject.getSendinBlueId(), smtpTemplate);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private String getSendinBlueContent(String content) {
    String patternStr = "(\\$)(Partner|Lead)(.+?)(\\$)";
    Pattern pat = Pattern.compile(patternStr);
    Matcher matcher = pat.matcher(content);
    while (matcher.find()) {
      String field = matcher.group(3);
      String[] fields = field.substring(1).split("\\.");
      if (fields != null && fields.length > 1) {
        String mainField = fields[0];
        MetaField metaField =
            Beans.get(MetaFieldRepository.class)
                .all()
                .filter("self.metaModel.name = ? AND self.name = ?", matcher.group(2), mainField)
                .fetchOne();
        if (metaField != null) {
          try {
            Class<?> klass =
                Class.forName(metaField.getPackageName() + "." + metaField.getTypeName());
            List<Field> nameColumnFields =
                FieldUtils.getFieldsListWithAnnotation(klass, NameColumn.class);
            if (nameColumnFields != null && !nameColumnFields.isEmpty()) {
              if (nameColumnFields.get(0).getName().equals(fields[1])) {
                field = "." + mainField + "Name";
              }
            }
          } catch (ClassNotFoundException e) {
          }
        }
      }

      String value = matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4);
      String variable = "{{ Contact" + field.toUpperCase() + " }}";
      content = content.replace(value, variable);
    }
    return translationService.getValueTranslation(content, userLanguage);
  }

  public void exportCampaign() throws AxelorException {
    getApiKeyAuth();
    userLanguage = userService.getUser().getLanguage();
    EmailCampaignsApi apiInstance = new EmailCampaignsApi();
    Query<Campaign> campaignQuery = Beans.get(CampaignRepository.class).all();
    if (campaignQuery != null) {
      int totalCampaign = (int) campaignQuery.count();
      List<Campaign> campaigns;
      int offset = 0;
      while (totalCampaign > 0) {
        campaigns = campaignQuery.fetch(DATA_FETCH_LIMIT, offset);
        if (campaigns != null) {
          totalCampaign = campaigns.size();
          if (!campaigns.isEmpty()) {
            offset += totalCampaign;
            for (Campaign dataObject : campaigns) {
              exportCampaignDataObject(dataObject, apiInstance);
            }
          }
        }
      }
    }
  }

  @Transactional
  public void exportCampaignDataObject(Campaign dataObject, EmailCampaignsApi apiInstance) {
    if (dataObject != null) {
      SendinBlueCampaign sendinBlueCampaign;
      List<SendinBlueCampaign> sendinBlueCampaigns = dataObject.getSendinBlueCampaignList();

      partnerRecipients = getRecipients(dataObject, SendinBlueCampaignRepository.PARTNER_CAMPAIGN);
      leadRecipients = getRecipients(dataObject, SendinBlueCampaignRepository.LEAD_CAMPAIGN);

      for (int i = 1; i <= 4; i++) {
        sendinBlueCampaign = exportPartnerCampaign(sendinBlueCampaigns, dataObject, i, apiInstance);
        if (sendinBlueCampaign != null && !sendinBlueCampaigns.contains(sendinBlueCampaign)) {
          sendinBlueCampaign.setCampaign(dataObject);
          sendinBlueCampaigns.add(sendinBlueCampaign);
        }
      }
      dataObject.setSendinBlueCampaignList(sendinBlueCampaigns);
      Beans.get(CampaignRepository.class).save(dataObject);
    }
  }

  private SendinBlueCampaign exportPartnerCampaign(
      List<SendinBlueCampaign> sendinBlueCampaigns,
      Campaign dataObject,
      Integer campaignType,
      EmailCampaignsApi apiInstance) {
    Optional<SendinBlueCampaign> partnerCampaign =
        sendinBlueCampaigns
            .stream()
            .filter(campaign -> campaign.getCampaignType() == campaignType)
            .findFirst();
    if (partnerCampaign.isPresent()) {
      SendinBlueCampaign sendinBlueCampaign = partnerCampaign.get();
      if (Optional.ofNullable(sendinBlueCampaign.getSendinBlueId()).orElse(0L) != 0) {
        ApiResponse<GetEmailCampaign> result = null;
        try {
          result = apiInstance.getEmailCampaignWithHttpInfo(sendinBlueCampaign.getSendinBlueId());
        } catch (Exception e) {
        }
        if (result != null) {
          updatePartnerCampaign(sendinBlueCampaign, dataObject, campaignType, apiInstance);
          return sendinBlueCampaign;
        }
      }
    }
    return createPartnerCampaign(dataObject, campaignType, apiInstance);
  }

  private SendinBlueCampaign createPartnerCampaign(
      Campaign dataObject, Integer campaignType, EmailCampaignsApi apiInstance) {

    CreateEmailCampaignSender sender = null;
    EmailAccount emailAccount = dataObject.getEmailAccount();
    if (emailAccount != null) {
      sender = new CreateEmailCampaignSender();
      sender.setEmail(emailAccount.getLogin());
      sender.setName(emailAccount.getName());
    }

    if (sender == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(ITranslation.CAMPAIGN_SENDER_ERROR)));
      return null;
    }

    String content = getContent(dataObject, campaignType);
    if (content == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(ITranslation.CAMPAIGN_TEMPLATE_ERROR)));
      return null;
    }

    CreateEmailCampaignRecipients recipients = null;
    ArrayList<Long> Ids = getRecipient(campaignType);
    if (Ids != null) {
      recipients = new CreateEmailCampaignRecipients();
      recipients.setListIds(Ids);
    }
    if (recipients == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(ITranslation.CAMPAIGN_RECIPIENT_ERROR)));
      return null;
    }

    CreateEmailCampaign emailCampaign = new CreateEmailCampaign();
    emailCampaign.setSubject(
        translationService.getValueTranslation(dataObject.getName(), userLanguage));
    emailCampaign.setSender(sender);
    emailCampaign.setName(
        translationService.getValueTranslation(dataObject.getName(), userLanguage)
            + "_"
            + campaignType);
    emailCampaign.setRecipients(recipients);
    emailCampaign.setHtmlContent(content);
    return createCampaign(emailCampaign, apiInstance, campaignType);
  }

  private SendinBlueCampaign createCampaign(
      CreateEmailCampaign emailCampaign, EmailCampaignsApi apiInstance, Integer campaignType) {
    try {
      CreateModel result = apiInstance.createEmailCampaign(emailCampaign);
      SendinBlueCampaign sendinBlueCampaign = new SendinBlueCampaign();
      sendinBlueCampaign.setSendinBlueId(result.getId());
      sendinBlueCampaign.setCampaignType(campaignType);
      return sendinBlueCampaign;
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  private String getContent(Campaign dataObject, Integer campaignType) {
    Template template = null;
    switch (campaignType) {
      case SendinBlueCampaignRepository.PARTNER_CAMPAIGN:
        template = dataObject.getPartnerTemplate();
        break;
      case SendinBlueCampaignRepository.PARTNER_REMINDER_CAMPAIGN:
        template = dataObject.getPartnerReminderTemplate();
        break;
      case SendinBlueCampaignRepository.LEAD_CAMPAIGN:
        template = dataObject.getLeadTemplate();
        break;
      case SendinBlueCampaignRepository.LEAD_REMINDER_CAMPAIGN:
        template = dataObject.getLeadReminderTemplate();
        break;
    }

    if (template != null) {
      return getSendinBlueContent(template.getContent());
    }
    return null;
  }

  private ArrayList<Long> getRecipient(Integer campaignType) {
    switch (campaignType) {
      case SendinBlueCampaignRepository.PARTNER_CAMPAIGN:
      case SendinBlueCampaignRepository.PARTNER_REMINDER_CAMPAIGN:
        return partnerRecipients;
      case SendinBlueCampaignRepository.LEAD_CAMPAIGN:
      case SendinBlueCampaignRepository.LEAD_REMINDER_CAMPAIGN:
        return leadRecipients;
    }
    return null;
  }

  private ArrayList<Long> getRecipients(Campaign dataObject, Integer campaignType) {
    String listName = "";
    List<String> emails = null;
    switch (campaignType) {
      case SendinBlueCampaignRepository.PARTNER_CAMPAIGN:
        emails = getPartnerEmails(dataObject.getPartnerSet());
        listName = "#Partner";
        break;
      case SendinBlueCampaignRepository.LEAD_CAMPAIGN:
        emails = getLeadEmails(dataObject.getLeadSet());
        listName = "#Lead";
        break;
    }

    if (emails != null && !emails.isEmpty()) {
      ContactsApi contactApi = new ContactsApi();
      Long folderId = createFolder(contactApi);
      Long listId = createList(dataObject.getId() + listName, contactApi, folderId);

      AddContactToList contactEmails = new AddContactToList();
      contactEmails.setEmails(emails);
      try {
        contactApi.addContactToList(listId, contactEmails);
      } catch (ApiException e) {
      }
      return new ArrayList<Long>(Arrays.asList(listId));
    }

    return null;
  }

  private List<String> getPartnerEmails(Set<Partner> partnerSet) {
    List<String> emails = null;
    if (partnerSet != null && !partnerSet.isEmpty()) {
      emails = new ArrayList<>();
      for (Partner partner : partnerSet) {
        EmailAddress emailAddress = partner.getEmailAddress();
        if (emailAddress != null && !StringUtils.isBlank(emailAddress.getAddress())) {
          emails.add(emailAddress.getAddress());
        }
      }
      return emails;
    }
    return null;
  }

  private List<String> getLeadEmails(Set<Lead> leadSet) {
    List<String> emails = null;
    if (leadSet != null && !leadSet.isEmpty()) {
      emails = new ArrayList<>();
      for (Lead lead : leadSet) {
        EmailAddress emailAddress = lead.getEmailAddress();
        if (emailAddress != null && !StringUtils.isBlank(emailAddress.getAddress())) {
          emails.add(emailAddress.getAddress());
        }
      }
      return emails;
    }
    return null;
  }

  private void updatePartnerCampaign(
      SendinBlueCampaign sendinBlueCampaign,
      Campaign dataObject,
      Integer campaignType,
      EmailCampaignsApi apiInstance) {

    UpdateEmailCampaignSender sender = null;
    EmailAccount emailAccount = dataObject.getEmailAccount();
    if (emailAccount != null) {
      sender = new UpdateEmailCampaignSender();
      sender.setEmail(emailAccount.getLogin());
      sender.setName(emailAccount.getName());
    }
    if (sender == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(ITranslation.CAMPAIGN_SENDER_ERROR)));
      return;
    }

    String content = getContent(dataObject, campaignType);
    if (content == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(ITranslation.CAMPAIGN_TEMPLATE_ERROR)));
      return;
    }

    UpdateEmailCampaignRecipients recipients = null;
    ArrayList<Long> ids = getRecipient(campaignType);
    if (ids != null) {
      recipients = new UpdateEmailCampaignRecipients();
      recipients.setListIds(ids);
    }
    if (recipients == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(ITranslation.CAMPAIGN_RECIPIENT_ERROR)));
      return;
    }

    UpdateEmailCampaign updateEmailCampaign = new UpdateEmailCampaign();

    updateEmailCampaign.setHtmlContent(content);
    updateEmailCampaign.setSender(sender);
    updateEmailCampaign.setRecipients(recipients);
    updateEmailCampaign.setSubject(
        translationService.getValueTranslation(dataObject.getName(), userLanguage));
    updateEmailCampaign.setName(
        translationService.getValueTranslation(dataObject.getName(), userLanguage)
            + "_"
            + campaignType);

    try {
      apiInstance.updateEmailCampaign(sendinBlueCampaign.getSendinBlueId(), updateEmailCampaign);
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  public void importCampaignReport() throws AxelorException {
    getApiKeyAuth();
    LocalDate localDate = null;
    org.threeten.bp.LocalDate startDate = null;
    org.threeten.bp.LocalDate endDate = null;
    Optional<SendinBlueReport> maxDateReport =
        Beans.get(SendinBlueReportRepository.class)
            .all()
            .fetchStream()
            .max(Comparator.comparing(SendinBlueReport::getFromDate));
    if (maxDateReport.isPresent()) {
      localDate = maxDateReport.get().getFromDate();
      if (localDate.compareTo(Beans.get(AppBaseService.class).getTodayDate()) < 0) {
        localDate = localDate.plusDays(1L);
      }

      if (localDate != null) {
        startDate = org.threeten.bp.LocalDate.parse(localDate.toString());
        if (startDate != null) {
          LocalDate today = Beans.get(AppBaseService.class).getTodayDate();
          if (today == null) {
            endDate = org.threeten.bp.LocalDate.now();
          } else {
            endDate = org.threeten.bp.LocalDate.parse(today.toString());
          }
        }
      }
    }
    SmtpApi apiInstance = new SmtpApi();
    try {
      GetReports result = apiInstance.getSmtpReport(null, null, startDate, endDate, null, null);
      if (result != null && result.getReports() != null && !result.getReports().isEmpty()) {
        for (GetReportsReports report : result.getReports()) {
          createSendinBlueReport(report);
        }
      }
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  @Transactional
  public void createSendinBlueReport(GetReportsReports report) {
    SendinBlueReport sendinBlueReport = new SendinBlueReport();
    sendinBlueReport.setFromDate(LocalDate.parse(report.getDate().toString()));
    sendinBlueReport.setRequests(report.getRequests());
    sendinBlueReport.setDelivered(report.getDelivered());
    sendinBlueReport.setHardBounces(report.getHardBounces());
    sendinBlueReport.setSoftBounces(report.getSoftBounces());
    sendinBlueReport.setClicks(report.getClicks());
    sendinBlueReport.setUniqueClicks(report.getUniqueClicks());
    sendinBlueReport.setOpens(report.getOpens());
    sendinBlueReport.setUniqueOpens(report.getUniqueOpens());
    sendinBlueReport.setSpamReports(report.getSpamReports());
    sendinBlueReport.setBlocked(report.getBlocked());
    sendinBlueReport.setInvalid(report.getInvalid());
    Beans.get(SendinBlueReportRepository.class).save(sendinBlueReport);
  }

  @Override
  public List<Map<String, Object>> getReport(LocalDate fromDate, LocalDate toDate) {
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    List<String> eventType =
        new ArrayList<>(
            Arrays.asList(
                "requests",
                "delivered",
                "clicks",
                "opens",
                "hardBounces",
                "softBounces",
                "uniqueClicks",
                "uniqueOpens",
                "spamReports",
                "blocked",
                "invalid",
                "unsubscribed"));
    javax.persistence.Query q =
        JPA.em()
            .createQuery(
                "SELECT "
                    + "SUM(requests) AS requests , "
                    + "SUM(delivered) AS delivered ,"
                    + "SUM(clicks) AS clicks ,"
                    + "SUM(opens) AS opens ,"
                    + "SUM(hardBounces) AS hardBounces ,"
                    + "SUM(softBounces) AS softBounces ,"
                    + "SUM(uniqueClicks) AS uniqueClicks ,"
                    + "SUM(uniqueOpens) AS uniqueOpens ,"
                    + "SUM(spamReports) AS spamReports ,"
                    + "SUM(blocked) AS blocked ,"
                    + "SUM(invalid) AS invalid ,"
                    + "SUM(unsubscribed) AS unsubscribed "
                    + "FROM SendinBlueReport "
                    + "WHERE fromDate BETWEEN DATE(:fromDate) AND DATE(:toDate) ");
    q.setParameter("fromDate", fromDate);
    q.setParameter("toDate", toDate);

    if (q.getResultList() != null && !q.getResultList().isEmpty()) {
      Object[] result = (Object[]) q.getResultList().get(0);
      for (int i = 0; i < eventType.size(); i++) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("total", (Long) result[i]);
        dataMap.put("eventType", eventType.get(i));
        dataList.add(dataMap);
      }
    }

    return dataList;
  }

  @Override
  public void importEvents() throws AxelorException {
    getApiKeyAuth();
    LocalDateTime localDateTime = null;
    org.threeten.bp.LocalDate startDate = null;
    org.threeten.bp.LocalDate endDate = null;
    Optional<SendinBlueEvent> maxDateReport =
        Beans.get(SendinBlueEventRepository.class)
            .all()
            .fetchStream()
            .max(Comparator.comparing(SendinBlueEvent::getEventDate));
    if (maxDateReport.isPresent()) {
      localDateTime = maxDateReport.get().getEventDate();
      if (localDateTime.compareTo(Beans.get(AppBaseService.class).getTodayDate().atStartOfDay())
          < 0) {
        localDateTime = localDateTime.plusDays(1L);
      }
      if (localDateTime != null) {
        startDate = org.threeten.bp.LocalDate.parse(localDateTime.toLocalDate().toString());
        if (startDate != null) {
          LocalDate today = Beans.get(AppBaseService.class).getTodayDate();
          if (today == null) {
            endDate = org.threeten.bp.LocalDate.now();
          } else {
            endDate = org.threeten.bp.LocalDate.parse(today.toString());
          }
        }
      }
    }
    SmtpApi apiInstance = new SmtpApi();
    try {

      GetEmailEventReport result =
          apiInstance.getEmailEventReport(
              null, null, startDate, endDate, null, null, null, null, null, null);
      if (result != null && result.getEvents() != null && !result.getEvents().isEmpty()) {
        for (GetEmailEventReportEvents event : result.getEvents()) {
          createSendinBlueEvent(event);
        }
      }
    } catch (ApiException e) {
      TraceBackService.trace(e);
    }
  }

  @Transactional
  public void createSendinBlueEvent(GetEmailEventReportEvents event) {
    SendinBlueEvent sendinBlueEvent = new SendinBlueEvent();
    sendinBlueEvent.setEmail(event.getEmail());
    sendinBlueEvent.setFromEmail(event.getFrom());
    sendinBlueEvent.setMessageId(event.getMessageId());
    sendinBlueEvent.setEvent(event.getEvent().getValue());
    sendinBlueEvent.setTag(event.getTag());
    sendinBlueEvent.setIp(event.getIp());
    sendinBlueEvent.setReason(event.getReason());
    sendinBlueEvent.setSubject(event.getSubject());
    sendinBlueEvent.setEventDate(LocalDateTime.parse(event.getDate().toLocalDateTime().toString()));

    EmailAddress emailAddress =
        Beans.get(EmailAddressRepository.class).findByAddress(event.getEmail());
    if (emailAddress != null) {
      sendinBlueEvent.setEmailAddress(emailAddress);
      sendinBlueEvent.setPartner(emailAddress.getPartner());
      sendinBlueEvent.setLead(emailAddress.getLead());
    }
    Beans.get(SendinBlueEventRepository.class).save(sendinBlueEvent);
  }

  @Override
  public void importContactStat() throws AxelorException {
    getApiKeyAuth();
    ContactsApi apiInstance = new ContactsApi();
    List<EmailAddress> emailAddresses = Beans.get(EmailAddressRepository.class).all().fetch();
    if (emailAddresses != null && !emailAddresses.isEmpty()) {
      for (EmailAddress emailAddress : emailAddresses) {
        if (emailAddress.getPartner() == null && emailAddress.getLead() == null) {
          continue;
        }
        String email = emailAddress.getAddress();
        try {
          GetContactCampaignStats contactStatObj = apiInstance.getContactStats(email);
          if (contactStatObj != null) {
            List<GetContactCampaignStatsClicked> clickedList = contactStatObj.getClicked();
            if (clickedList != null && !clickedList.isEmpty()) {
              importClicked(clickedList, emailAddress);
            }
            List<GetExtendedContactDetailsStatisticsMessagesSent> compaintList =
                contactStatObj.getComplaints();
            if (compaintList != null && !compaintList.isEmpty()) {
              importComplaints(compaintList, emailAddress);
            }
            List<GetExtendedContactDetailsStatisticsMessagesSent> hardBounceList =
                contactStatObj.getHardBounces();
            if (hardBounceList != null && !hardBounceList.isEmpty()) {
              importHardBounce(hardBounceList, emailAddress);
            }
            List<GetExtendedContactDetailsStatisticsMessagesSent> messageSentList =
                contactStatObj.getMessagesSent();
            if (messageSentList != null && !messageSentList.isEmpty()) {
              importMessageSent(messageSentList, emailAddress);
            }
            List<GetContactCampaignStatsOpened> openedList = contactStatObj.getOpened();
            if (openedList != null && !openedList.isEmpty()) {
              importOpened(openedList, emailAddress);
            }
            List<GetExtendedContactDetailsStatisticsMessagesSent> softBounceList =
                contactStatObj.getSoftBounces();
            if (softBounceList != null && !softBounceList.isEmpty()) {
              importSoftBounce(softBounceList, emailAddress);
            }
            GetContactCampaignStatsUnsubscriptions unsubscriptions =
                contactStatObj.getUnsubscriptions();
            if (unsubscriptions != null) {
              importUnsubscriptions(unsubscriptions, emailAddress);
            }
          }
        } catch (ApiException e) {
        }
      }
    }
  }

  @Transactional
  public void importClicked(
      List<GetContactCampaignStatsClicked> clickedList, EmailAddress emailAddress) {
    for (GetContactCampaignStatsClicked clickedObj : clickedList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(clickedObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        List<GetExtendedContactDetailsStatisticsLinks> clickedLinklist = clickedObj.getLinks();
        for (GetExtendedContactDetailsStatisticsLinks clickedLink : clickedLinklist) {
          SendinBlueContactStat contactStat = new SendinBlueContactStat();
          contactStat.setEventCount(clickedLink.getCount());
          contactStat.setEventDateTime(
              LocalDateTime.parse(clickedLink.getEventTime().toLocalDateTime().toString()));
          contactStat.setEventType(SendinBlueEventRepository.CLICKS);
          contactStat.setSendinBlueCampaign(sendinBlueCampaign);
          contactStat.setEmailAddress(emailAddress);
          sendinBlueContactStatRepo.save(contactStat);
        }
      }
    }
  }

  @Transactional
  public void importComplaints(
      List<GetExtendedContactDetailsStatisticsMessagesSent> compaintList,
      EmailAddress emailAddress) {
    for (GetExtendedContactDetailsStatisticsMessagesSent compaintObj : compaintList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(compaintObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(compaintObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.COMPLAINTS);
        contactStat.setSendinBlueCampaign(sendinBlueCampaign);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Transactional
  public void importHardBounce(
      List<GetExtendedContactDetailsStatisticsMessagesSent> hardBounceList,
      EmailAddress emailAddress) {
    for (GetExtendedContactDetailsStatisticsMessagesSent hardBounceObj : hardBounceList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(hardBounceObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(hardBounceObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.HARD_BOUNCES);
        contactStat.setSendinBlueCampaign(sendinBlueCampaign);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Transactional
  public void importMessageSent(
      List<GetExtendedContactDetailsStatisticsMessagesSent> messageSentList,
      EmailAddress emailAddress) {
    for (GetExtendedContactDetailsStatisticsMessagesSent messageSentObj : messageSentList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(messageSentObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(messageSentObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.MESSAGE_SENT);
        contactStat.setSendinBlueCampaign(sendinBlueCampaign);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Transactional
  public void importOpened(
      List<GetContactCampaignStatsOpened> openedList, EmailAddress emailAddress) {
    for (GetContactCampaignStatsOpened openedObj : openedList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(openedObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(openedObj.getCount());
        contactStat.setEventDateTime(
            LocalDateTime.parse(openedObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.OPENED);
        contactStat.setSendinBlueCampaign(sendinBlueCampaign);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Transactional
  public void importSoftBounce(
      List<GetExtendedContactDetailsStatisticsMessagesSent> softBounceList,
      EmailAddress emailAddress) {
    for (GetExtendedContactDetailsStatisticsMessagesSent softBounceObj : softBounceList) {
      SendinBlueCampaign sendinBlueCampaign =
          Beans.get(SendinBlueCampaignRepository.class)
              .findBySendinBlueId(softBounceObj.getCampaignId());
      if (sendinBlueCampaign != null) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(softBounceObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.SOFT_BOUNCES);
        contactStat.setSendinBlueCampaign(sendinBlueCampaign);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Transactional
  public void importUnsubscriptions(
      GetContactCampaignStatsUnsubscriptions unsubscriptions, EmailAddress emailAddress) {
    List<GetExtendedContactDetailsStatisticsUnsubscriptionsAdminUnsubscription>
        adminUnsubscriptionList = unsubscriptions.getAdminUnsubscription();
    if (adminUnsubscriptionList != null && !adminUnsubscriptionList.isEmpty()) {
      for (GetExtendedContactDetailsStatisticsUnsubscriptionsAdminUnsubscription
          adminUnsubscriptionObj : adminUnsubscriptionList) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(
                adminUnsubscriptionObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.ADMIN_UNSUBSCRIPTION);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }

    List<GetExtendedContactDetailsStatisticsUnsubscriptionsUserUnsubscription>
        userUnsubscriptionList = unsubscriptions.getUserUnsubscription();
    if (userUnsubscriptionList != null && !userUnsubscriptionList.isEmpty()) {
      for (GetExtendedContactDetailsStatisticsUnsubscriptionsUserUnsubscription
          userUnsubscriptionObj : userUnsubscriptionList) {
        SendinBlueContactStat contactStat = new SendinBlueContactStat();
        contactStat.setEventCount(1L);
        contactStat.setEventDateTime(
            LocalDateTime.parse(userUnsubscriptionObj.getEventTime().toLocalDateTime().toString()));
        contactStat.setEventType(SendinBlueEventRepository.USER_UNSUBSCRIPTION);
        contactStat.setEmailAddress(emailAddress);
        sendinBlueContactStatRepo.save(contactStat);
      }
    }
  }

  @Override
  public void importCampaignStat() throws AxelorException {
    getApiKeyAuth();
    EmailCampaignsApi apiInstance = new EmailCampaignsApi();
    List<SendinBlueCampaign> campaigns =
        Beans.get(SendinBlueCampaignRepository.class).all().fetch();
    SendinBlueCampaignStat sendinBlueCampaignStat = null;
    for (SendinBlueCampaign sendinBlueCampaign : campaigns) {
      sendinBlueCampaignStat =
          sendinBlueCampaignStatRepo.findBySendinBlueCampaign(sendinBlueCampaign);
      if (sendinBlueCampaignStat == null) {
        sendinBlueCampaignStat = new SendinBlueCampaignStat();
      }
      createCampaignStat(sendinBlueCampaignStat, sendinBlueCampaign, apiInstance);
    }
  }

  @Transactional
  public void createCampaignStat(
      SendinBlueCampaignStat sendinBlueCampaignStat,
      SendinBlueCampaign sendinBlueCampaign,
      EmailCampaignsApi apiInstance) {
    try {
      Long defaultValue = new Long(0L);
      GetEmailCampaign campaign =
          apiInstance.getEmailCampaign(sendinBlueCampaign.getSendinBlueId());
      if (campaign != null && campaign.getStatistics() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) campaign.getStatistics();
        if (stats.containsKey("globalStats")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> globalStats =
              (Map<String, Object>) stats.getOrDefault("globalStats", 0L);
          if (globalStats != null) {
            try {
              sendinBlueCampaignStat.setUniqueClicks(
                  getLongValue(globalStats.getOrDefault("uniqueClicks", defaultValue)));
              sendinBlueCampaignStat.setClickers(
                  getLongValue(globalStats.getOrDefault("clickers", defaultValue)));
              sendinBlueCampaignStat.setComplaints(
                  getLongValue(globalStats.getOrDefault("complaints", defaultValue)));
              sendinBlueCampaignStat.setDelivered(
                  getLongValue(globalStats.getOrDefault("delivered", defaultValue)));
              sendinBlueCampaignStat.setSent(
                  getLongValue(globalStats.getOrDefault("sent", defaultValue)));
              sendinBlueCampaignStat.setSoftBounces(
                  getLongValue(globalStats.getOrDefault("softBounces", defaultValue)));
              sendinBlueCampaignStat.setHardBounces(
                  getLongValue(globalStats.getOrDefault("hardBounces", defaultValue)));
              sendinBlueCampaignStat.setUniqueViews(
                  getLongValue(globalStats.getOrDefault("uniqueViews", defaultValue)));
              sendinBlueCampaignStat.setUnsubscriptions(
                  getLongValue(globalStats.getOrDefault("unsubscriptions", defaultValue)));
              sendinBlueCampaignStat.setViewed(
                  getLongValue(globalStats.getOrDefault("viewed", defaultValue)));
              sendinBlueCampaignStat.setSendinBlueCampaign(sendinBlueCampaign);
              sendinBlueCampaignStatRepo.save(sendinBlueCampaignStat);
            } catch (Exception e) {
              TraceBackService.trace(e);
            }
          }
        }
      }
    } catch (Exception e) {
    }
  }

  private Long getLongValue(Object value) {
    if (value == null || value.toString().equals("0.0")) {
      return new Long(0L);
    }
    return new Long(value.toString().substring(0, value.toString().indexOf(".")));
  }
}
