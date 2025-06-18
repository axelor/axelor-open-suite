/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerConvertService;
import com.axelor.apps.base.service.PartnerPriceListDomainService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.partner.api.PartnerGenerateService;
import com.axelor.apps.base.service.partner.registrationnumber.PartnerRegistrationCodeViewService;
import com.axelor.apps.base.service.partner.registrationnumber.RegistrationNumberValidator;
import com.axelor.apps.base.service.partner.registrationnumber.factory.PartnerRegistrationValidatorFactoryService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.birt.core.exception.BirtException;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PartnerController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void setPartnerSequence(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    if (partner.getPartnerSeq() == null) {
      String seq =
          Beans.get(SequenceService.class)
              .getSequenceNumber(SequenceRepository.PARTNER, Partner.class, "partnerSeq", partner);
      if (seq == null)
        throw new AxelorException(
            partner,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.PARTNER_1));
      else response.setValue("partnerSeq", seq);
    }
  }

  /**
   * Fonction appeler par le bouton imprimer
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void printContactPhonebook(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PrintingTemplate contactPhoneBookTemplate =
        Beans.get(AppBaseService.class).getAppBase().getContactPhoneBookPrintTemplate();
    if (ObjectUtils.isEmpty(contactPhoneBookTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    String name = I18n.get("Phone Book");
    String fileLink =
        Beans.get(PrintingTemplatePrintService.class).getPrintLink(contactPhoneBookTemplate, null);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  /**
   * Fonction appeler par le bouton imprimer
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void printCompanyPhonebook(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PrintingTemplate companyPhoneBookTemplate =
        Beans.get(AppBaseService.class).getAppBase().getCompanyPhoneBookPrintTemplate();
    if (ObjectUtils.isEmpty(companyPhoneBookTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    String name = I18n.get("Company PhoneBook");
    String fileLink =
        Beans.get(PrintingTemplatePrintService.class).getPrintLink(companyPhoneBookTemplate, null);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  /* Fonction appeler par le bouton imprimer
   *
   * @param request
   * @param response
   * @return
   */
  public void printClientSituation(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    Partner partner = context.asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());

    PrintingTemplate clientSituationPrintTemplate =
        Beans.get(AppBaseService.class).getAppBase().getClientSituationPrintTemplate();
    if (ObjectUtils.isEmpty(clientSituationPrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    String name = I18n.get("Customer Situation");

    PrintingGenFactoryContext factoryContext = new PrintingGenFactoryContext(partner);
    factoryContext.setContext(getParamsMap(context));

    String fileLink =
        Beans.get(PrintingTemplatePrintService.class)
            .getPrintLink(clientSituationPrintTemplate, factoryContext);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getParamsMap(Context context) {
    Map<String, Object> params = new HashMap<>();
    LinkedHashMap<String, Object> companyMap =
        (LinkedHashMap<String, Object>) context.get("company");
    Object companyId = companyMap != null ? companyMap.get("id") : null;
    params.put("CompanyId", companyId);
    params.put(
        "TradingNameId",
        (Object)
            (context.get("tradingName") != null
                ? ((TradingName) context.get("tradingName")).getId()
                : null));
    params.put(
        "FromDate",
        context.get("fromDate") != null ? Date.valueOf(context.get("fromDate").toString()) : null);
    params.put(
        "ToDate",
        context.get("toDate") != null ? Date.valueOf(context.get("toDate").toString()) : null);
    params.put("InvoiceStatus", context.get("invoiceStatus"));
    params.put("SaleOrderStatus", context.get("saleOrderStatus"));
    params.put("StockMoveStatus", context.get("stockMoveStatus"));
    return params;
  }

  @CallMethod
  public Company getActiveCompany() {
    Company company = Beans.get(UserService.class).getUser().getActiveCompany();
    if (company == null) {
      List<Company> companyList = Beans.get(CompanyRepository.class).all().fetch();
      if (companyList.size() == 1) {
        company = companyList.get(0);
      }
    }
    return company;
  }

  public void setSocialNetworkUrl(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    Map<String, String> urlMap =
        Beans.get(PartnerService.class)
            .getSocialNetworkUrl(
                partner.getName(), partner.getFirstName(), partner.getPartnerTypeSelect());
    response.setAttr("googleLabel", "title", urlMap.get("google"));
    response.setAttr("linkedinLabel", "title", urlMap.get("linkedin"));
  }

  public void findSentMails(ActionRequest request, ActionResponse response) {
    try {
      this.findMails(request, response, MessageRepository.TYPE_SENT);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Deprecated
  public void findReceivedMails(ActionRequest request, ActionResponse response) {
    try {
      this.findMails(request, response, MessageRepository.TYPE_RECEIVED);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private void findMails(ActionRequest request, ActionResponse response, int emailType) {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    List<Long> idList = Beans.get(PartnerService.class).findPartnerMails(partner, emailType);

    response.setView(
        ActionView.define(I18n.get("Emails"))
            .model(Message.class.getName())
            .add("cards", "message-cards")
            .add("grid", "message-grid")
            .add("form", "message-form")
            .domain("self.id IN (:ids)")
            .context("ids", !CollectionUtils.isEmpty(idList) ? idList : null)
            .map());
  }

  public void addContactToPartner(ActionRequest request, ActionResponse response) {
    try {
      final Context context = request.getContext();
      final Partner contact = context.asType(Partner.class);
      final Context parentContext = context.getParent();

      if (parentContext != null
          && Partner.class.isAssignableFrom(parentContext.getContextClass())
          && Objects.equals(parentContext.asType(Partner.class), contact.getMainPartner())) {
        return;
      }

      Beans.get(PartnerService.class)
          .addContactToPartner(Beans.get(PartnerRepository.class).find(contact.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkIbanValidity(ActionRequest request, ActionResponse response)
      throws AxelorException {

    List<BankDetails> bankDetailsList =
        request.getContext().asType(Partner.class).getBankDetailsList();
    List<String> ibanInError = Lists.newArrayList();

    if (bankDetailsList != null && !bankDetailsList.isEmpty()) {
      for (BankDetails bankDetails : bankDetailsList) {
        Bank bank = bankDetails.getBank();
        if (bankDetails.getIban() != null
            && bank != null
            && bank.getBankDetailsTypeSelect() == BankRepository.BANK_IDENTIFIER_TYPE_IBAN) {
          LOG.debug("checking iban code : {}", bankDetails.getIban());
          try {
            Beans.get(BankDetailsService.class).validateIban(bankDetails.getIban());
          } catch (IbanFormatException
              | InvalidCheckDigitException
              | UnsupportedCountryException e) {
            ibanInError.add(bankDetails.getIban());
          }
        }
      }
    }
    if (!ibanInError.isEmpty()) {
      response.setError(
          String.format(
              I18n.get(BaseExceptionMessage.BANK_DETAILS_2),
              StringHtmlListBuilder.formatMessage(ibanInError)));
    }
  }

  public void convertToIndividualPartner(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Partner partner = request.getContext().asType(Partner.class);
    Long id = partner.getId();
    if (id == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PARTNER_3));
    }
    partner = Beans.get(PartnerRepository.class).find(id);
    Beans.get(PartnerConvertService.class).convertToIndividualPartner(partner);
    response.setView(
        ActionView.define(I18n.get("Partner"))
            .model(Partner.class.getName())
            .add("form", "partner-form")
            .add("grid", "partner-grid")
            .context("_showRecord", id)
            .map());
  }

  /**
   * Called from partner view on name change and onLoad. Call {@link
   * PartnerService#isThereDuplicatePartner(Partner)}
   *
   * @param request
   * @param response
   */
  public void checkPartnerName(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      response.setAttr(
          "duplicatePartnerText",
          "hidden",
          !Beans.get(PartnerService.class).isThereDuplicatePartner(partner));
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void checkPartnerNameArchived(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      Partner partnerArchived =
          Beans.get(PartnerService.class).isThereDuplicatePartnerInArchive(partner);
      if (partnerArchived != null) {
        response.setValue("$duplicatePartnerInArchiveText", partnerArchived.getPartnerSeq());
        response.setAttr("$duplicatePartnerInArchiveText", "hidden", false);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void showPartnerOnMap(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      response.setView(
          ActionView.define(partner.getFullName())
              .add(
                  "html",
                  Beans.get(AppBaseService.class).getAppBase().getMapApiSelect()
                          == AppBaseRepository.MAP_API_GOOGLE
                      ? Beans.get(MapService.class).getMapURI("partner", partner.getId())
                      : Beans.get(MapService.class).getOsmMapURI("partner", partner.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void modifyRegistrationCode(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      RegistrationNumberValidator validator =
          Beans.get(PartnerRegistrationValidatorFactoryService.class)
              .getRegistrationNumberValidator(partner);
      if (validator == null) {
        return;
      }
      validator.setRegistrationCodeValidationValues(partner);
      response.setValues(partner);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void getHideFieldOnPartnerTypeSelect(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      PartnerRegistrationCodeViewService partnerRegistrationCodeViewService =
          Beans.get(PartnerRegistrationCodeViewService.class);
      String registrationCodeTitle =
          partnerRegistrationCodeViewService.getRegistrationCodeTitleFromTemplate(partner);
      boolean isNicHidden = partnerRegistrationCodeViewService.isNicHidden(partner);
      boolean isSirenHidden = partnerRegistrationCodeViewService.isSirenHidden(partner);
      boolean isTaxNbrHidden = partnerRegistrationCodeViewService.isTaxNbrHidden(partner);
      response.setAttr(
          "registrationCode",
          "title",
          !Strings.isNullOrEmpty(registrationCodeTitle)
              ? registrationCodeTitle
              : I18n.get("Registration number"));
      response.setAttr("siren", "hidden", isSirenHidden);
      response.setAttr("nic", "hidden", isNicHidden);
      response.setAttr("taxNbr", "hidden", isTaxNbrHidden);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void checkRegistrationCode(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException {
    Partner partner = request.getContext().asType(Partner.class);
    RegistrationNumberValidator validator =
        Beans.get(PartnerRegistrationValidatorFactoryService.class)
            .getRegistrationNumberValidator(partner);
    boolean notValidRegistrationCode =
        validator != null && !validator.isRegistrationCodeValid(partner);
    response.setAttr("isValidRegistrationCode", "hidden", !notValidRegistrationCode);
    if (notValidRegistrationCode) {
      response.setAttr(
          "isValidRegistrationCode",
          "title",
          I18n.get(BaseExceptionMessage.PARTNER_INVALID_REGISTRATION_CODE));
    }
  }

  public void setPositiveBalance(ActionRequest request, ActionResponse response) {
    BigDecimal balance =
        Optional.ofNullable(request.getContext().get("balance"))
            .map(b -> new BigDecimal(b.toString()))
            .orElse(BigDecimal.ZERO);

    Company company =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);

    response.setValue(
        "$positiveBalanceBtn",
        Beans.get(CurrencyScaleService.class).getCompanyScaledValue(company, balance.abs()));
  }

  public void setParentPartnerDomain(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    List<Partner> parentPartnerList = Beans.get(PartnerService.class).getParentPartnerList(partner);
    if (ObjectUtils.notEmpty(parentPartnerList)) {
      response.setAttr(
          "parentPartner",
          "domain",
          String.format(
              "self.id IN (%s)",
              parentPartnerList.stream()
                  .map(Partner::getId)
                  .map(String::valueOf)
                  .collect(Collectors.joining(","))));
    }
  }

  public void checkIfRegistrationCodeExists(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    String message = Beans.get(PartnerService.class).checkIfRegistrationCodeExists(partner);
    if (StringUtils.isEmpty(message)) {
      return;
    }
    if (Beans.get(AppBaseService.class).getAppBase().getIsRegistrationCodeCheckBlocking()) {
      response.setError(message);
    } else {
      response.setAlert(message);
    }
  }

  @ErrorException
  public void apiSireneFetchData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String siret = request.getContext().get("siretNumber").toString();

    Object partnerId = request.getContext().get("_id");
    Partner partner;
    if (partnerId != null) {
      partner = JPA.find(Partner.class, Long.parseLong(partnerId.toString()));
    } else {
      partner = new Partner();
    }

    Beans.get(PartnerGenerateService.class).configurePartner(partner, siret);

    if (partnerId != null) {
      response.setValues(partner);
      response.setCanClose(true);
    } else {
      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Partner"))
              .model(Partner.class.getName())
              .add("form", "partner-form")
              .add("grid", "partner-grid")
              .context("_showRecord", partner.getId());

      response.setView(actionViewBuilder.map());
      response.setCanClose(true);
    }
  }

  public void getSalePartnerPriceListDomain(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    response.setAttr(
        "salePartnerPriceList",
        "domain",
        Beans.get(PartnerPriceListDomainService.class).getSalePartnerPriceListDomain(partner));
  }

  public void getPurchasePartnerPriceListDomain(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    response.setAttr(
        "purchasePartnerPriceList",
        "domain",
        Beans.get(PartnerPriceListDomainService.class).getPurchasePartnerPriceListDomain(partner));
  }
}
