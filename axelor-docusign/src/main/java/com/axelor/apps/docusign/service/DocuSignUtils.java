package com.axelor.apps.docusign.service;

import com.axelor.apps.docusign.db.DocuSignField;
import com.axelor.apps.docusign.db.DocuSignFieldSetting;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.docusign.esign.model.Approve;
import com.docusign.esign.model.Checkbox;
import com.docusign.esign.model.Company;
import com.docusign.esign.model.Decline;
import com.docusign.esign.model.Email;
import com.docusign.esign.model.FullName;
import com.docusign.esign.model.List;
import com.docusign.esign.model.ListItem;
import com.docusign.esign.model.Radio;
import com.docusign.esign.model.RadioGroup;
import com.docusign.esign.model.SignHere;
import com.docusign.esign.model.Tabs;
import java.util.ArrayList;
import org.apache.commons.collections.CollectionUtils;

public class DocuSignUtils {

  public static void addSignHere(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    SignHere signHere = new SignHere();
    signHere.setName(fieldSetting.getName());
    signHere.setDocumentId(documentId);
    signHere.setPageNumber(fieldSetting.getPageNumber());
    signHere.setRecipientId(recipientId);
    signHere.setTabLabel(fieldSetting.getTabLabel());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      signHere.setAnchorString(fieldSetting.getAnchor());
      signHere.setAnchorUnits(fieldSetting.getAnchorUnits());
      signHere.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      signHere.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      signHere.setXPosition(fieldSetting.getxPosition());
      signHere.setYPosition(fieldSetting.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getSignHereTabs())) {
      tabs.setSignHereTabs(new ArrayList<>());
    }

    tabs.getSignHereTabs().add(signHere);
  }

  public static void addFullName(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    FullName fullName = new FullName();
    fullName.setName(fieldSetting.getName());
    fullName.setDocumentId(documentId);
    fullName.setRecipientId(recipientId);
    fullName.setPageNumber(fieldSetting.getPageNumber());
    fullName.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      fullName.setBold("true");
    }
    fullName.setFontColor(fieldSetting.getFontColor());
    fullName.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      fullName.setAnchorString(fieldSetting.getAnchor());
      fullName.setAnchorUnits(fieldSetting.getAnchorUnits());
      fullName.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      fullName.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      fullName.setXPosition(fieldSetting.getxPosition());
      fullName.setYPosition(fieldSetting.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getFullNameTabs())) {
      tabs.setFullNameTabs(new ArrayList<>());
    }

    tabs.getFullNameTabs().add(fullName);
  }

  public static void addEmail(
      Tabs tabs,
      DocuSignFieldSetting fieldSetting,
      String documentId,
      String recipientId,
      String emailValue) {
    Email email = new Email();
    email.setName(fieldSetting.getName());
    email.setValue(emailValue);
    email.setDocumentId(documentId);
    email.setRecipientId(recipientId);
    email.setPageNumber(fieldSetting.getPageNumber());
    email.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      email.setBold("true");
    }
    email.setFontColor(fieldSetting.getFontColor());
    email.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      email.setAnchorString(fieldSetting.getAnchor());
      email.setAnchorUnits(fieldSetting.getAnchorUnits());
      email.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      email.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      email.setXPosition(fieldSetting.getxPosition());
      email.setYPosition(fieldSetting.getyPosition());
    }
    if (fieldSetting.getIsRequired()) {
      email.setRequired("true");
    } else {
      email.setRequired("false");
    }

    if (CollectionUtils.isEmpty(tabs.getEmailTabs())) {
      tabs.setEmailTabs(new ArrayList<>());
    }

    tabs.getEmailTabs().add(email);
  }

  public static void addCompany(
      Tabs tabs,
      DocuSignFieldSetting fieldSetting,
      String documentId,
      String recipientId,
      String companyName) {
    Company company = new Company();
    company.setName(fieldSetting.getName());
    company.setValue(companyName);
    company.setDocumentId(documentId);
    company.setRecipientId(recipientId);
    company.setPageNumber(fieldSetting.getPageNumber());
    company.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      company.setBold("true");
    }
    company.setFontColor(fieldSetting.getFontColor());
    company.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      company.setAnchorString(fieldSetting.getAnchor());
      company.setAnchorUnits(fieldSetting.getAnchorUnits());
      company.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      company.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      company.setXPosition(fieldSetting.getxPosition());
      company.setYPosition(fieldSetting.getyPosition());
    }
    if (fieldSetting.getIsRequired()) {
      company.setRequired("true");
    } else {
      company.setRequired("false");
    }

    if (CollectionUtils.isEmpty(tabs.getCompanyTabs())) {
      tabs.setCompanyTabs(new ArrayList<>());
    }

    tabs.getCompanyTabs().add(company);
  }

  public static void addList(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    List list = new List();
    list.setDocumentId(documentId);
    list.setRecipientId(recipientId);
    list.setPageNumber(fieldSetting.getPageNumber());
    list.setTabLabel(fieldSetting.getTabLabel());
    list.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      list.setBold("true");
    }
    list.setFontColor(fieldSetting.getFontColor());
    list.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      list.setAnchorString(fieldSetting.getAnchor());
      list.setAnchorUnits(fieldSetting.getAnchorUnits());
      list.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      list.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      list.setXPosition(fieldSetting.getxPosition());
      list.setYPosition(fieldSetting.getyPosition());
    }
    if (fieldSetting.getIsRequired()) {
      list.setRequired("true");
    } else {
      list.setRequired("false");
    }

    if (CollectionUtils.isNotEmpty(fieldSetting.getDocuSignFieldSettingList())) {
      java.util.List<ListItem> listItemList = new ArrayList<>();
      for (DocuSignFieldSetting fieldSettingChild : fieldSetting.getDocuSignFieldSettingList()) {
        ListItem item = new ListItem();
        item.setText(fieldSettingChild.getName());
        item.setValue(fieldSettingChild.getValue());

        listItemList.add(item);
      }
      list.setListItems(listItemList);
    }

    if (CollectionUtils.isEmpty(tabs.getListTabs())) {
      tabs.setListTabs(new ArrayList<>());
    }

    tabs.getListTabs().add(list);
  }

  public static void addCheckbox(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    Checkbox checkbox = new Checkbox();
    checkbox.setName(fieldSetting.getName());
    checkbox.setDocumentId(documentId);
    checkbox.setRecipientId(recipientId);
    checkbox.setPageNumber(fieldSetting.getPageNumber());
    /*checkbox.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      checkbox.setBold("true");
    }
    checkbox.setFontColor(fieldSetting.getFontColor());
    checkbox.setFontSize(fieldSetting.getFontSize());*/
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      checkbox.setAnchorString(fieldSetting.getAnchor());
      checkbox.setAnchorUnits(fieldSetting.getAnchorUnits());
      checkbox.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      checkbox.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      checkbox.setXPosition(fieldSetting.getxPosition());
      checkbox.setYPosition(fieldSetting.getyPosition());
    }
    if (fieldSetting.getIsRequired()) {
      checkbox.setRequired("true");
    } else {
      checkbox.setRequired("false");
    }

    if (CollectionUtils.isEmpty(tabs.getCheckboxTabs())) {
      tabs.setCheckboxTabs(new ArrayList<>());
    }

    tabs.getCheckboxTabs().add(checkbox);
  }

  public static void addRadioGroup(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    RadioGroup radioGroup = new RadioGroup();
    radioGroup.setDocumentId(documentId);
    radioGroup.setRecipientId(recipientId);
    radioGroup.setGroupName(fieldSetting.getName());

    if (CollectionUtils.isNotEmpty(fieldSetting.getDocuSignFieldSettingList())) {
      java.util.List<Radio> radioList = new ArrayList<>();
      for (DocuSignFieldSetting fieldSettingChild : fieldSetting.getDocuSignFieldSettingList()) {
        Radio radio = new Radio();
        radio.setPageNumber(fieldSettingChild.getPageNumber());
        radio.setValue(fieldSettingChild.getValue());
        if (StringUtils.notEmpty(fieldSettingChild.getAnchor())) {
          radio.setAnchorString(fieldSettingChild.getAnchor());
          radio.setAnchorUnits(fieldSetting.getAnchorUnits());
          radio.setAnchorYOffset(fieldSetting.getAnchorYOffset());
          radio.setAnchorXOffset(fieldSetting.getAnchorXOffset());
        } else {
          radio.setXPosition(fieldSettingChild.getxPosition());
          radio.setYPosition(fieldSettingChild.getyPosition());
        }
        if (fieldSettingChild.getIsRequired()) {
          radio.setRequired("true");
        } else {
          radio.setRequired("false");
        }

        radioList.add(radio);
      }
      radioGroup.setRadios(radioList);
    }

    if (CollectionUtils.isEmpty(tabs.getRadioGroupTabs())) {
      tabs.setRadioGroupTabs(new ArrayList<>());
    }

    tabs.getRadioGroupTabs().add(radioGroup);
  }

  public static void addApprove(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    Approve approve = new Approve();
    approve.setButtonText(fieldSetting.getName());
    approve.setDocumentId(documentId);
    approve.setRecipientId(recipientId);
    approve.setPageNumber(fieldSetting.getPageNumber());
    approve.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      approve.setBold("true");
    }
    approve.setFontColor(fieldSetting.getFontColor());
    approve.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      approve.setAnchorString(fieldSetting.getAnchor());
      approve.setAnchorUnits(fieldSetting.getAnchorUnits());
      approve.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      approve.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      approve.setXPosition(fieldSetting.getxPosition());
      approve.setYPosition(fieldSetting.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getApproveTabs())) {
      tabs.setApproveTabs(new ArrayList<>());
    }

    tabs.getApproveTabs().add(approve);
  }

  public static void addDecline(
      Tabs tabs, DocuSignFieldSetting fieldSetting, String documentId, String recipientId) {
    Decline decline = new Decline();
    decline.setButtonText(fieldSetting.getName());
    decline.setDocumentId(documentId);
    decline.setRecipientId(recipientId);
    decline.setPageNumber(fieldSetting.getPageNumber());
    decline.setFont(fieldSetting.getFont());
    if (fieldSetting.getIsBold()) {
      decline.setBold("true");
    }
    decline.setFontColor(fieldSetting.getFontColor());
    decline.setFontSize(fieldSetting.getFontSize());
    if (StringUtils.notEmpty(fieldSetting.getAnchor())) {
      decline.setAnchorString(fieldSetting.getAnchor());
      decline.setAnchorUnits(fieldSetting.getAnchorUnits());
      decline.setAnchorYOffset(fieldSetting.getAnchorYOffset());
      decline.setAnchorXOffset(fieldSetting.getAnchorXOffset());
    } else {
      decline.setXPosition(fieldSetting.getxPosition());
      decline.setYPosition(fieldSetting.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getDeclineTabs())) {
      tabs.setDeclineTabs(new ArrayList<>());
    }

    tabs.getDeclineTabs().add(decline);
  }

  public static void updateSignHereField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getSignHereTabs())) {
      SignHere signHere =
          tabs.getSignHereTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(signHere)) {
        field.setStatus(signHere.getStatus());
      }
    }
  }

  public static void updateFullNameField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getFullNameTabs())) {
      FullName fullName =
          tabs.getFullNameTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(fullName)) {
        field.setStatus(fullName.getStatus());
      }
    }
  }

  public static void updateEmailField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getEmailTabs())) {
      Email email =
          tabs.getEmailTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(email)) {
        field.setValue(email.getValue());
        field.setStatus(email.getStatus());
      }
    }
  }

  public static void updateCompanyField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getCompanyTabs())) {
      Company company =
          tabs.getCompanyTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(company)) {
        field.setValue(company.getValue());
        field.setStatus(company.getStatus());
      }
    }
  }

  public static void updateCheckboxField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getCheckboxTabs())) {
      Checkbox checkbox =
          tabs.getCheckboxTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(checkbox)) {
        field.setValue(checkbox.getSelected());
        field.setStatus(checkbox.getStatus());
      }
    }
  }

  public static void updateRadioGroupField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getRadioGroupTabs())) {
      RadioGroup radioGroup =
          tabs.getRadioGroupTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getGroupName().equals(fieldSetting.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(radioGroup)) {
        java.util.List<Radio> radioList = radioGroup.getRadios();
        if (CollectionUtils.isNotEmpty(radioList)) {
          Radio radioSelected =
              radioList.stream()
                  .filter(x -> "true".equals(x.getSelected()))
                  .findFirst()
                  .orElse(null);
          if (ObjectUtils.notEmpty(radioSelected)) {
            field.setValue(radioSelected.getValue());
          }
        }
      }
    }
  }

  public static void updateListField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getListTabs())) {
      List list =
          tabs.getListTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getTabLabel().equals(fieldSetting.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(list)) {
        field.setValue(list.getValue());
        field.setStatus(list.getStatus());
      }
    }
  }

  public static void updateApproveField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getApproveTabs())) {
      Approve approve =
          tabs.getApproveTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getTabLabel().equals(fieldSetting.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(approve)) {
        field.setStatus(approve.getStatus());
      }
    }
  }

  public static void updateDeclineField(
      DocuSignField field, DocuSignFieldSetting fieldSetting, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getDeclineTabs())) {
      Decline decline =
          tabs.getDeclineTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId()
                              .equals(fieldSetting.getDocuSignDocumentSetting().getDocumentId())
                          && x.getTabLabel().equals(fieldSetting.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(decline)) {
        field.setStatus(decline.getStatus());
      }
    }
  }
}
