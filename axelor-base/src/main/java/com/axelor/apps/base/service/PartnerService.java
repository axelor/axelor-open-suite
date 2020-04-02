/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PartnerPriceList;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PartnerService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private PartnerRepository partnerRepo;

  private Pattern phoneNumberPattern =
      Pattern.compile("^\\+?(?:[0-9]{2,3}(?:\\s|\\.)?){3,6}[0-9]{2,3}$");

  public Partner createPartner(
      String name,
      String firstName,
      String fixedPhone,
      String mobilePhone,
      EmailAddress emailAddress,
      Currency currency,
      Address deliveryAddress,
      Address mainInvoicingAddress) {
    Partner partner = new Partner();

    partner.setName(name);
    partner.setFirstName(firstName);
    partner.setFullName(this.computeFullName(partner));
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_COMPANY);
    partner.setIsProspect(true);
    partner.setFixedPhone(fixedPhone);
    partner.setMobilePhone(mobilePhone);
    partner.setEmailAddress(emailAddress);
    partner.setCurrency(currency);
    Partner contact = new Partner();
    contact.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
    contact.setIsContact(true);
    contact.setName(name);
    contact.setFirstName(firstName);
    contact.setMainPartner(partner);
    contact.setFullName(this.computeFullName(partner));
    partner.addContactPartnerSetItem(contact);

    if (deliveryAddress == mainInvoicingAddress) {
      addPartnerAddress(partner, mainInvoicingAddress, true, true, true);
    } else {
      addPartnerAddress(partner, deliveryAddress, true, false, true);
      addPartnerAddress(partner, mainInvoicingAddress, true, true, false);
    }

    return partner;
  }

  public void setPartnerFullName(Partner partner) {

    partner.setFullName(this.computeFullName(partner));
  }

  public String computeFullName(Partner partner) {
    if (!Strings.isNullOrEmpty(partner.getName())
        && !Strings.isNullOrEmpty(partner.getFirstName())) {
      return partner.getName() + " " + partner.getFirstName();
    } else if (!Strings.isNullOrEmpty(partner.getName())) {
      return partner.getName();
    } else if (!Strings.isNullOrEmpty(partner.getFirstName())) {
      return partner.getFirstName();
    } else {
      return "" + partner.getId();
    }
  }

  public Map<String, String> getSocialNetworkUrl(
      String name, String firstName, Integer typeSelect) {

    Map<String, String> urlMap = new HashMap<String, String>();
    if (typeSelect == 2) {
      name =
          firstName != null && name != null
              ? firstName + "+" + name
              : name == null ? firstName : name;
    }
    name = name == null ? "" : name;
    urlMap.put(
        "google",
        "<a class='fa fa-google-plus' href='https://www.google.com/?gws_rd=cr#q="
            + name
            + "' target='_blank' />");
    urlMap.put(
        "facebook",
        "<a class='fa fa-facebook' href='https://www.facebook.com/search/more/?q="
            + name
            + "&init=public"
            + "' target='_blank'/>");
    urlMap.put(
        "twitter",
        "<a class='fa fa-twitter' href='https://twitter.com/search?q="
            + name
            + "' target='_blank' />");
    urlMap.put(
        "linkedin",
        "<a class='fa fa-linkedin' href='https://www.linkedin.com/company/"
            + name
            + "' target='_blank' />");
    if (typeSelect == 2) {
      urlMap.put(
          "linkedin",
          "<a class='fa fa-linkedin' href='http://www.linkedin.com/pub/dir/"
              + name.replace("+", "/")
              + "' target='_blank' />");
    }
    urlMap.put(
        "youtube",
        "<a class='fa fa-youtube' href='https://www.youtube.com/results?search_query="
            + name
            + "' target='_blank' />");

    return urlMap;
  }

  public List<Long> findPartnerMails(Partner partner) {
    List<Long> idList = new ArrayList<Long>();

    idList.addAll(this.findMailsFromPartner(partner));

    Set<Partner> contactSet = partner.getContactPartnerSet();
    if (contactSet != null && !contactSet.isEmpty()) {
      for (Partner contact : contactSet) {
        idList.addAll(this.findMailsFromPartner(contact));
      }
    }
    return idList;
  }

  public List<Long> findContactMails(Partner partner) {
    List<Long> idList = new ArrayList<Long>();

    idList.addAll(this.findMailsFromPartner(partner));

    return idList;
  }

  @SuppressWarnings("unchecked")
  public List<Long> findMailsFromPartner(Partner partner) {
    String query =
        "SELECT DISTINCT(email.id) FROM Message as email WHERE email.mediaTypeSelect = 2 AND "
            + "(email.relatedTo1Select = 'com.axelor.apps.base.db.Partner' AND email.relatedTo1SelectId = "
            + partner.getId()
            + ") "
            + "OR (email.relatedTo2Select = 'com.axelor.apps.base.db.Partner' AND email.relatedTo2SelectId = "
            + partner.getId()
            + ")";

    if (partner.getEmailAddress() != null) {
      query += "OR (email.fromEmailAddress.id = " + partner.getEmailAddress().getId() + ")";
    }

    return JPA.em().createQuery(query).getResultList();
  }

  private PartnerAddress createPartnerAddress(Address address, Boolean isDefault) {

    PartnerAddress partnerAddress = new PartnerAddress();
    partnerAddress.setAddress(address);
    partnerAddress.setIsDefaultAddr(isDefault);

    return partnerAddress;
  }

  @Transactional
  public void resetDefaultAddress(Partner partner, String addrTypeQuery) {

    if (partner.getId() != null) {
      PartnerAddressRepository partnerAddressRepo = Beans.get(PartnerAddressRepository.class);
      PartnerAddress partnerAddress =
          partnerAddressRepo
              .all()
              .filter(
                  "self.partner.id = ? AND self.isDefaultAddr = true" + addrTypeQuery,
                  partner.getId())
              .fetchOne();
      if (partnerAddress != null) {
        partnerAddress.setIsDefaultAddr(false);
        partnerAddressRepo.save(partnerAddress);
      }
    }
  }

  public Partner addPartnerAddress(
      Partner partner,
      Address address,
      Boolean isDefault,
      Boolean isInvoicing,
      Boolean isDelivery) {

    PartnerAddress partnerAddress = createPartnerAddress(address, isDefault);

    if (isDefault != null && isDefault) {
      LOG.debug("Add partner address : isDelivery = {}", isDelivery);
      LOG.debug("Add partner address : isInvoicing = {}", isInvoicing);

      String query =
          String.format(
              " AND self.isDeliveryAddr = %s AND self.isInvoicingAddr = %s",
              isDelivery, isInvoicing);
      resetDefaultAddress(partner, query);
    }

    partnerAddress.setIsInvoicingAddr(isInvoicing);
    partnerAddress.setIsDeliveryAddr(isDelivery);
    partnerAddress.setIsDefaultAddr(isDefault);
    partner.addPartnerAddressListItem(partnerAddress);

    return partner;
  }

  public void addContactToPartner(Partner contact) {
    if (contact.getMainPartner() != null) {
      Partner partner = contact.getMainPartner();

      partner.addContactPartnerSetItem(contact);
      savePartner(partner);
    }
  }

  protected Address getAddress(Partner partner, String querySpecific, String queryComman) {

    if (partner != null) {
      PartnerAddressRepository partnerAddressRepo = Beans.get(PartnerAddressRepository.class);
      List<PartnerAddress> partnerAddressList =
          partnerAddressRepo.all().filter(querySpecific, partner.getId()).fetch();
      if (partnerAddressList.isEmpty()) {
        partnerAddressList = partnerAddressRepo.all().filter(queryComman, partner.getId()).fetch();
      }
      if (partnerAddressList.size() == 1) {
        return partnerAddressList.get(0).getAddress();
      }
      for (PartnerAddress partnerAddress : partnerAddressList) {
        if (partnerAddress.getIsDefaultAddr()) {
          return partnerAddress.getAddress();
        }
      }
    }

    return null;
  }

  public Address getInvoicingAddress(Partner partner) {

    return getAddress(
        partner,
        "self.partner.id = ?1 AND self.isInvoicingAddr = true AND self.isDeliveryAddr = false AND self.isDefaultAddr = true",
        "self.partner.id = ?1 AND self.isInvoicingAddr = true");
  }

  public Address getDeliveryAddress(Partner partner) {

    return getAddress(
        partner,
        "self.partner.id = ?1 AND self.isDeliveryAddr = true AND self.isInvoicingAddr = false AND self.isDefaultAddr = true",
        "self.partner.id = ?1 AND self.isDeliveryAddr = true");
  }

  public Address getDefaultAddress(Partner partner) {

    return getAddress(
        partner,
        "self.partner.id = ?1 AND self.isDeliveryAddr = true AND self.isInvoicingAddr = true AND self.isDefaultAddr = true",
        "self.partner.id = ?1 AND self.isDefaultAddr = true");
  }

  @Transactional
  public Partner savePartner(Partner partner) {
    return partnerRepo.save(partner);
  }

  public BankDetails getDefaultBankDetails(Partner partner) {

    for (BankDetails bankDetails : partner.getBankDetailsList()) {
      if (bankDetails.getIsDefault()) {
        return bankDetails;
      }
    }

    return null;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public String getSIRENNumber(Partner partner) throws AxelorException {
    char[] Str = new char[9];
    if (partner.getRegistrationCode() == null || partner.getRegistrationCode().isEmpty()) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PARTNER_2),
          I18n.get(IExceptionMessage.EXCEPTION),
          partner.getName());
    } else {
      String registrationCode = partner.getRegistrationCode();
      // remove whitespace in the registration code before using it
      registrationCode.replaceAll("\\s", "").getChars(0, 9, Str, 0);
    }

    return new String(Str);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void convertToIndividualPartner(Partner partner) {
    partner.setIsContact(false);
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
    addPartnerAddress(partner, partner.getContactAddress(), true, false, false);
    partner.setContactAddress(null);
  }

  /**
   * Check if the partner in view has a duplicate.
   *
   * @param partner a context partner object
   * @return if there is a duplicate partner
   */
  public boolean isThereDuplicatePartner(Partner partner) {
    String newName = this.computeFullName(partner);
    if (Strings.isNullOrEmpty(newName)) {
      return false;
    }
    Long partnerId = partner.getId();
    if (partnerId == null) {
      Partner existingPartner =
          partnerRepo
              .all()
              .filter(
                  "lower(self.fullName) = lower(:newName) "
                      + "and self.partnerTypeSelect = :_partnerTypeSelect")
              .bind("newName", newName)
              .bind("_partnerTypeSelect", partner.getPartnerTypeSelect())
              .fetchOne();
      return existingPartner != null;
    } else {
      Partner existingPartner =
          partnerRepo
              .all()
              .filter(
                  "lower(self.fullName) = lower(:newName) "
                      + "and self.id != :partnerId "
                      + "and self.partnerTypeSelect = :_partnerTypeSelect")
              .bind("newName", newName)
              .bind("partnerId", partnerId)
              .bind("_partnerTypeSelect", partner.getPartnerTypeSelect())
              .fetchOne();
      return existingPartner != null;
    }
  }

  /**
   * Search for the sale price list for the current date in the partner.
   *
   * @param partner
   * @return the sale price list for the partner null if no active price list has been found
   */
  public PriceList getSalePriceList(Partner partner) {
    PartnerPriceList partnerPriceList = partner.getSalePartnerPriceList();
    if (partnerPriceList == null) {
      return null;
    }
    Set<PriceList> priceListSet = partnerPriceList.getPriceListSet();
    if (priceListSet == null) {
      return null;
    }
    LocalDate today = Beans.get(AppBaseService.class).getTodayDate();
    List<PriceList> candidatePriceListList = new ArrayList<>();
    for (PriceList priceList : priceListSet) {
      LocalDate beginDate =
          priceList.getApplicationBeginDate() != null
              ? priceList.getApplicationBeginDate()
              : LocalDate.MIN;
      LocalDate endDate =
          priceList.getApplicationEndDate() != null
              ? priceList.getApplicationEndDate()
              : LocalDate.MAX;
      if (beginDate.compareTo(today) <= 0 && today.compareTo(endDate) <= 0) {
        candidatePriceListList.add(priceList);
      }
    }

    // if we found multiple price list, then the user will have to select one
    if (candidatePriceListList.size() == 1) {
      return candidatePriceListList.get(0);
    } else {
      return null;
    }
  }

  /**
   * Get the partner language code. If null, return the default partner language.
   *
   * @param partner
   * @return
   */
  public String getPartnerLanguageCode(Partner partner) {

    String locale = null;

    if (partner != null && partner.getLanguage() != null) {
      locale = partner.getLanguage().getCode();
    }
    if (!Strings.isNullOrEmpty(locale)) {
      return locale;
    }

    return Beans.get(AppBaseService.class).getDefaultPartnerLanguageCode();
  }

  /**
   * Normalize phone number.
   *
   * @param phoneNumber
   * @return
   */
  public String normalizePhoneNumber(String phoneNumber) {
    return StringUtils.isBlank(phoneNumber) ? null : phoneNumber.replaceAll("\\s|\\.|-", "");
  }

  /**
   * Check phone number.
   *
   * @param phoneNumber
   * @return
   */
  public boolean checkPhoneNumber(String phoneNumber) {
    return StringUtils.isBlank(phoneNumber)
        ? false
        : phoneNumberPattern.matcher(phoneNumber).matches();
  }

  /**
   * Get phone number field name.
   *
   * @param actionName
   * @return
   */
  public String getPhoneNumberFieldName(String actionName) {
    Preconditions.checkNotNull(actionName, I18n.get("Action name cannot be null."));
    return actionName.substring(actionName.lastIndexOf('-') + 1);
  }
}
