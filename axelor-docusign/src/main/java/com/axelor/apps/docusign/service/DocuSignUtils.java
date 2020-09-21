package com.axelor.apps.docusign.service;

import com.axelor.apps.docusign.db.DocuSignField;
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
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    SignHere signHere = new SignHere();
    signHere.setName(docuSignField.getName());
    signHere.setDocumentId(documentId);
    signHere.setPageNumber(docuSignField.getPageNumber());
    signHere.setRecipientId(recipientId);
    signHere.setTabLabel(docuSignField.getTabLabel());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      signHere.setAnchorString(docuSignField.getAnchor());
      signHere.setAnchorUnits(docuSignField.getAnchorUnits());
      signHere.setAnchorYOffset(docuSignField.getAnchorYOffset());
      signHere.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      signHere.setXPosition(docuSignField.getxPosition());
      signHere.setYPosition(docuSignField.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getSignHereTabs())) {
      tabs.setSignHereTabs(new ArrayList<>());
    }

    tabs.getSignHereTabs().add(signHere);
  }

  public static void addFullName(
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    FullName fullName = new FullName();
    fullName.setName(docuSignField.getName());
    fullName.setDocumentId(documentId);
    fullName.setRecipientId(recipientId);
    fullName.setPageNumber(docuSignField.getPageNumber());
    fullName.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      fullName.setBold("true");
    }
    fullName.setFontColor(docuSignField.getFontColor());
    fullName.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      fullName.setAnchorString(docuSignField.getAnchor());
      fullName.setAnchorUnits(docuSignField.getAnchorUnits());
      fullName.setAnchorYOffset(docuSignField.getAnchorYOffset());
      fullName.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      fullName.setXPosition(docuSignField.getxPosition());
      fullName.setYPosition(docuSignField.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getFullNameTabs())) {
      tabs.setFullNameTabs(new ArrayList<>());
    }

    tabs.getFullNameTabs().add(fullName);
  }

  public static void addEmail(
      Tabs tabs,
      DocuSignField docuSignField,
      String documentId,
      String recipientId,
      String emailValue) {
    Email email = new Email();
    email.setName(docuSignField.getName());
    email.setValue(emailValue);
    email.setDocumentId(documentId);
    email.setRecipientId(recipientId);
    email.setPageNumber(docuSignField.getPageNumber());
    email.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      email.setBold("true");
    }
    email.setFontColor(docuSignField.getFontColor());
    email.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      email.setAnchorString(docuSignField.getAnchor());
      email.setAnchorUnits(docuSignField.getAnchorUnits());
      email.setAnchorYOffset(docuSignField.getAnchorYOffset());
      email.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      email.setXPosition(docuSignField.getxPosition());
      email.setYPosition(docuSignField.getyPosition());
    }
    if (docuSignField.getIsRequired()) {
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
      DocuSignField docuSignField,
      String documentId,
      String recipientId,
      String companyName) {
    Company company = new Company();
    company.setName(docuSignField.getName());
    company.setValue(companyName);
    company.setDocumentId(documentId);
    company.setRecipientId(recipientId);
    company.setPageNumber(docuSignField.getPageNumber());
    company.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      company.setBold("true");
    }
    company.setFontColor(docuSignField.getFontColor());
    company.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      company.setAnchorString(docuSignField.getAnchor());
      company.setAnchorUnits(docuSignField.getAnchorUnits());
      company.setAnchorYOffset(docuSignField.getAnchorYOffset());
      company.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      company.setXPosition(docuSignField.getxPosition());
      company.setYPosition(docuSignField.getyPosition());
    }
    if (docuSignField.getIsRequired()) {
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
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    List list = new List();
    list.setDocumentId(documentId);
    list.setRecipientId(recipientId);
    list.setPageNumber(docuSignField.getPageNumber());
    list.setTabLabel(docuSignField.getTabLabel());
    list.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      list.setBold("true");
    }
    list.setFontColor(docuSignField.getFontColor());
    list.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      list.setAnchorString(docuSignField.getAnchor());
      list.setAnchorUnits(docuSignField.getAnchorUnits());
      list.setAnchorYOffset(docuSignField.getAnchorYOffset());
      list.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      list.setXPosition(docuSignField.getxPosition());
      list.setYPosition(docuSignField.getyPosition());
    }
    if (docuSignField.getIsRequired()) {
      list.setRequired("true");
    } else {
      list.setRequired("false");
    }

    if (CollectionUtils.isNotEmpty(docuSignField.getDocuSignFieldList())) {
      java.util.List<ListItem> listItemList = new ArrayList<>();
      for (DocuSignField fieldChild : docuSignField.getDocuSignFieldList()) {
        ListItem item = new ListItem();
        item.setText(fieldChild.getName());
        item.setValue(fieldChild.getValue());

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
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    Checkbox checkbox = new Checkbox();
    checkbox.setName(docuSignField.getName());
    checkbox.setDocumentId(documentId);
    checkbox.setRecipientId(recipientId);
    checkbox.setPageNumber(docuSignField.getPageNumber());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      checkbox.setAnchorString(docuSignField.getAnchor());
      checkbox.setAnchorUnits(docuSignField.getAnchorUnits());
      checkbox.setAnchorYOffset(docuSignField.getAnchorYOffset());
      checkbox.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      checkbox.setXPosition(docuSignField.getxPosition());
      checkbox.setYPosition(docuSignField.getyPosition());
    }
    if (docuSignField.getIsRequired()) {
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
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    RadioGroup radioGroup = new RadioGroup();
    radioGroup.setDocumentId(documentId);
    radioGroup.setRecipientId(recipientId);
    radioGroup.setGroupName(docuSignField.getName());

    if (CollectionUtils.isNotEmpty(docuSignField.getDocuSignFieldList())) {
      java.util.List<Radio> radioList = new ArrayList<>();
      for (DocuSignField docuSignFieldChild : docuSignField.getDocuSignFieldList()) {
        Radio radio = new Radio();
        radio.setPageNumber(docuSignFieldChild.getPageNumber());
        radio.setValue(docuSignFieldChild.getValue());
        if (StringUtils.notEmpty(docuSignFieldChild.getAnchor())) {
          radio.setAnchorString(docuSignFieldChild.getAnchor());
          radio.setAnchorUnits(docuSignField.getAnchorUnits());
          radio.setAnchorYOffset(docuSignField.getAnchorYOffset());
          radio.setAnchorXOffset(docuSignField.getAnchorXOffset());
        } else {
          radio.setXPosition(docuSignFieldChild.getxPosition());
          radio.setYPosition(docuSignFieldChild.getyPosition());
        }
        if (docuSignFieldChild.getIsRequired()) {
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
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    Approve approve = new Approve();
    approve.setButtonText(docuSignField.getName());
    approve.setDocumentId(documentId);
    approve.setRecipientId(recipientId);
    approve.setPageNumber(docuSignField.getPageNumber());
    approve.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      approve.setBold("true");
    }
    approve.setFontColor(docuSignField.getFontColor());
    approve.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      approve.setAnchorString(docuSignField.getAnchor());
      approve.setAnchorUnits(docuSignField.getAnchorUnits());
      approve.setAnchorYOffset(docuSignField.getAnchorYOffset());
      approve.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      approve.setXPosition(docuSignField.getxPosition());
      approve.setYPosition(docuSignField.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getApproveTabs())) {
      tabs.setApproveTabs(new ArrayList<>());
    }

    tabs.getApproveTabs().add(approve);
  }

  public static void addDecline(
      Tabs tabs, DocuSignField docuSignField, String documentId, String recipientId) {
    Decline decline = new Decline();
    decline.setButtonText(docuSignField.getName());
    decline.setDocumentId(documentId);
    decline.setRecipientId(recipientId);
    decline.setPageNumber(docuSignField.getPageNumber());
    decline.setFont(docuSignField.getFont());
    if (docuSignField.getIsBold()) {
      decline.setBold("true");
    }
    decline.setFontColor(docuSignField.getFontColor());
    decline.setFontSize(docuSignField.getFontSize());
    if (StringUtils.notEmpty(docuSignField.getAnchor())) {
      decline.setAnchorString(docuSignField.getAnchor());
      decline.setAnchorUnits(docuSignField.getAnchorUnits());
      decline.setAnchorYOffset(docuSignField.getAnchorYOffset());
      decline.setAnchorXOffset(docuSignField.getAnchorXOffset());
    } else {
      decline.setXPosition(docuSignField.getxPosition());
      decline.setYPosition(docuSignField.getyPosition());
    }

    if (CollectionUtils.isEmpty(tabs.getDeclineTabs())) {
      tabs.setDeclineTabs(new ArrayList<>());
    }

    tabs.getDeclineTabs().add(decline);
  }

  public static void updateSignHereField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getSignHereTabs())) {
      SignHere signHere =
          tabs.getSignHereTabs().stream()
              .filter(
                  x ->
                      ObjectUtils.notEmpty(x.getDocumentId())
                          && ObjectUtils.notEmpty(field.getDocuSignDocument())
                          && x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && ObjectUtils.notEmpty(x.getName())
                          && x.getName().equals(field.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(signHere)) {
        field.setStatus(signHere.getStatus());
      }
    }
  }

  public static void updateFullNameField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getFullNameTabs())) {
      FullName fullName =
          tabs.getFullNameTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getName().equals(field.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(fullName)) {
        field.setStatus(fullName.getStatus());
      }
    }
  }

  public static void updateEmailField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getEmailTabs())) {
      Email email =
          tabs.getEmailTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getName().equals(field.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(email)) {
        field.setValue(email.getValue());
        field.setStatus(email.getStatus());
      }
    }
  }

  public static void updateCompanyField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getCompanyTabs())) {
      Company company =
          tabs.getCompanyTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getName().equals(field.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(company)) {
        field.setValue(company.getValue());
        field.setStatus(company.getStatus());
      }
    }
  }

  public static void updateCheckboxField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getCheckboxTabs())) {
      Checkbox checkbox =
          tabs.getCheckboxTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getName().equals(field.getName()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(checkbox)) {
        field.setValue(checkbox.getSelected());
        field.setStatus(checkbox.getStatus());
      }
    }
  }

  public static void updateRadioGroupField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getRadioGroupTabs())) {
      RadioGroup radioGroup =
          tabs.getRadioGroupTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getGroupName().equals(field.getName()))
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

  public static void updateListField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getListTabs())) {
      List list =
          tabs.getListTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getTabLabel().equals(field.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(list)) {
        field.setValue(list.getValue());
        field.setStatus(list.getStatus());
      }
    }
  }

  public static void updateApproveField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getApproveTabs())) {
      Approve approve =
          tabs.getApproveTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getTabLabel().equals(field.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(approve)) {
        field.setStatus(approve.getStatus());
      }
    }
  }

  public static void updateDeclineField(DocuSignField field, Tabs tabs) {
    if (CollectionUtils.isNotEmpty(tabs.getDeclineTabs())) {
      Decline decline =
          tabs.getDeclineTabs().stream()
              .filter(
                  x ->
                      x.getDocumentId().equals(field.getDocuSignDocument().getDocumentId())
                          && x.getTabLabel().equals(field.getTabLabel()))
              .findFirst()
              .orElse(null);
      if (ObjectUtils.notEmpty(decline)) {
        field.setStatus(decline.getStatus());
      }
    }
  }
}
