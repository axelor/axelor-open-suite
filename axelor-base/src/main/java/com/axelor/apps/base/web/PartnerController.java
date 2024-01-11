/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
              .getSequenceNumber(SequenceRepository.PARTNER, Partner.class, "partnerSeq");
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
  public void showEnvelope(ActionRequest request, ActionResponse response) throws AxelorException {
    Partner partner = request.getContext().asType(Partner.class);

    String name = I18n.get("Partner") + " " + partner.getPartnerSeq();

    String fileLink =
        ReportFactory.createReport(IReport.PARTNER, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(partner))
            .addParam("Timezone", getTimezone(partner.getUser()))
            .addParam("PartnerId", partner.getId())
            .generate()
            .getFileLink();

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
  public void printContactPhonebook(ActionRequest request, ActionResponse response)
      throws AxelorException {
    User user = AuthUtils.getUser();

    String name = I18n.get("Phone Book");

    String fileLink =
        ReportFactory.createReport(IReport.PHONE_BOOK, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam("Timezone", getTimezone(user))
            .addParam("UserId", user.getId())
            .generate()
            .getFileLink();

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
    User user = AuthUtils.getUser();

    String name = I18n.get("Company PhoneBook");

    String fileLink =
        ReportFactory.createReport(IReport.COMPANY_PHONE_BOOK, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam("Timezone", getTimezone(user))
            .addParam("UserId", user.getId())
            .generate()
            .getFileLink();

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

    Partner partner = request.getContext().asType(Partner.class);

    User user = AuthUtils.getUser();

    String name = I18n.get("Customer Situation");
    String fileLink =
        ReportFactory.createReport(IReport.CLIENT_SITUATION, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(partner))
            .addParam("Timezone", getTimezone(user))
            .addParam("UserId", user.getId())
            .addParam("PartnerId", partner.getId())
            .addParam(
                "PartnerPic",
                partner.getPicture() != null
                    ? MetaFiles.getPath(partner.getPicture()).toString()
                    : "")
            .generate()
            .getFileLink();

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  protected String getTimezone(User user) {
    if (user == null || user.getActiveCompany() == null) {
      return null;
    }
    return user.getActiveCompany().getTimezone();
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

      Function<String, String> addLi = s -> "<li>".concat(s).concat("</li>");

      response.setError(
          String.format(
              I18n.get(BaseExceptionMessage.BANK_DETAILS_2),
              "<ul>" + Joiner.on("").join(Iterables.transform(ibanInError, addLi)) + "<ul>"));
    }
  }

  public void convertToIndividualPartner(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Partner partner = request.getContext().asType(Partner.class);
    if (partner.getId() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PARTNER_3));
    }
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    Beans.get(PartnerService.class).convertToIndividualPartner(partner);
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
      PartnerService partnerService = Beans.get(PartnerService.class);
      if (partnerService.isRegistrationCodeValid(partner)) {
        String taxNbr = partnerService.getTaxNbrFromRegistrationCode(partner);
        String nic = partnerService.getNicFromRegistrationCode(partner);
        String siren = partnerService.getSirenFromRegistrationCode(partner);

        response.setValue("taxNbr", taxNbr);
        response.setValue("nic", nic);
        response.setValue("siren", siren);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void checkRegistrationCode(ActionRequest request, ActionResponse response) {
    Partner partner = request.getContext().asType(Partner.class);
    PartnerService partnerService = Beans.get(PartnerService.class);
    if (!partnerService.isRegistrationCodeValid(partner)) {
      response.setError(I18n.get(BaseExceptionMessage.PARTNER_INVALID_REGISTRATION_CODE));
    }
  }
}
