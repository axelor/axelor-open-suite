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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.LeadStatus;
import com.axelor.apps.crm.db.PartnerStatus;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.MultiRelated;
import com.axelor.message.db.repo.MultiRelatedRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConvertLeadWizardServiceImpl implements ConvertLeadWizardService {

  protected LeadService leadService;

  protected ConvertWizardService convertWizardService;

  protected PartnerService partnerService;

  protected CountryRepository countryRepo;

  protected AppBaseService appBaseService;

  protected AppCrmService appCrmService;

  protected MultiRelatedRepository multiRelatedRepository;

  protected ConvertWizardOpportunityService convertWizardOpportunityService;

  protected PartnerRepository partnerRepository;

  @Inject
  public ConvertLeadWizardServiceImpl(
      LeadService leadService,
      ConvertWizardService convertWizardService,
      PartnerService partnerService,
      CountryRepository countryRepo,
      AppBaseService appBaseService,
      AppCrmService appCrmService,
      MultiRelatedRepository multiRelatedRepository,
      ConvertWizardOpportunityService convertWizardOpportunityService,
      PartnerRepository partnerRepository) {
    this.leadService = leadService;
    this.convertWizardService = convertWizardService;
    this.partnerService = partnerService;
    this.countryRepo = countryRepo;
    this.appBaseService = appBaseService;
    this.appCrmService = appCrmService;
    this.multiRelatedRepository = multiRelatedRepository;
    this.convertWizardOpportunityService = convertWizardOpportunityService;
    this.partnerRepository = partnerRepository;
  }

  /**
   * Create a partner from a lead
   *
   * @return
   * @throws AxelorException
   */
  protected Partner createPartner(Partner partner, Address address) throws AxelorException {

    this.setEmailAddress(partner);

    if (appBaseService.getAppBase().getGeneratePartnerSequence()) {
      partner.setPartnerSeq(leadService.getSequence(partner));
    }

    partnerService.setPartnerFullName(partner);

    this.setAddress(partner, address);

    Company activeCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (activeCompany != null) {
      partner.addCompanySetItem(activeCompany);
      if (partner.getCurrency() == null) {
        partner.setCurrency(activeCompany.getCurrency());
      }
    }
    if (appBaseService.isApp("supplychain")) {
      Beans.get(BaseConvertLeadWizardService.class).setPartnerFields(partner);
    }

    return partner;
  }

  public void setEmailAddress(Partner partner) {

    EmailAddress emailAddress = partner.getEmailAddress();

    if (emailAddress != null) {
      partner.setEmailAddress(this.createEmailAddress(emailAddress.getAddress(), partner));
    }
  }

  protected void setAddress(Partner partner, Address address) throws AxelorException {

    if (address != null) {
      if (!partner.getIsContact()) {
        partnerService.addPartnerAddress(partner, address, true, true, true);
      }
      partner.setMainAddress(address);
    }
  }

  protected EmailAddress createEmailAddress(String address, Partner partner) {
    EmailAddress emailAddress = new EmailAddress();
    emailAddress.setAddress(address);
    emailAddress.setPartner(partner);

    return emailAddress;
  }

  /**
   * Convert lead into a partner
   *
   * @param lead
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected Partner convertLead(Lead lead, Partner partner, List<Partner> contactPartnerList)
      throws AxelorException {

    LeadStatus lostLeadStatus = appCrmService.getLostLeadStatus();
    LeadStatus convertedLeadStatus = appCrmService.getConvertedLeadStatus();

    LeadStatus leadStatus = lead.getLeadStatus();
    if (leadStatus == null || leadStatus.equals(lostLeadStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.LEAD_CONVERT_WRONG_STATUS));
    }

    if (partner != null && ObjectUtils.notEmpty(contactPartnerList)) {
      if (partner.getContactPartnerSet() == null) {
        partner.setContactPartnerSet(new HashSet<>());
      }
      partner.getContactPartnerSet().addAll(contactPartnerList);
      contactPartnerList.stream().forEach(contactPartner -> contactPartner.setMainPartner(partner));
    }

    if (partner != null) {
      lead.setPartner(partner);

      List<MultiRelated> multiRelateds =
          multiRelatedRepository
              .all()
              .filter(
                  "self.relatedToSelect = ?1 and self.relatedToSelectId = ?2",
                  Lead.class.getName(),
                  lead.getId())
              .fetch();

      for (MultiRelated multiRelated : multiRelateds) {
        multiRelated.setRelatedToSelect(Partner.class.getName());
        multiRelated.setRelatedToSelectId(partner.getId());
        if (ObjectUtils.notEmpty(contactPartnerList)) {
          for (Partner contactPartner : contactPartnerList) {
            MultiRelated contactMultiRelated = new MultiRelated();
            contactMultiRelated.setRelatedToSelect(Partner.class.getName());
            contactMultiRelated.setRelatedToSelectId(contactPartner.getId());
            contactMultiRelated.setMessage(multiRelated.getMessage());
            multiRelatedRepository.save(contactMultiRelated);
          }
        }
      }
    }

    for (Event event : lead.getEventList()) {
      event.setPartner(partner);
      if (ObjectUtils.notEmpty(contactPartnerList)) {
        event.setContactPartner(contactPartnerList.get(0));
      }
    }
    lead.setIsConverted(true);
    lead.setLeadStatus(convertedLeadStatus);
    return partner;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Partner generateDataAndConvertLead(
      Lead lead,
      Integer leadToPartnerSelect,
      Integer leadToContactSelect,
      Partner partner,
      Map<String, Object> partnerMap,
      PartnerStatus partnerStatus,
      List<Partner> contactPartnerList,
      Map<String, Object> contactPartnerMap)
      throws AxelorException {

    partner = createPartnerData(leadToPartnerSelect, partner, partnerMap, lead);

    if (partner != null
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      contactPartnerList = createContactData(lead, contactPartnerList, contactPartnerMap);
    }

    partner = this.convertLead(lead, partner, contactPartnerList);

    if (partnerStatus != null) {
      partner.setPartnerStatus(partnerStatus);
    }
    if (lead.getPartner() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.CONVERT_LEAD_ERROR));
    }
    return partner;
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  protected Partner createPartnerData(
      Integer leadToPartnerSelect, Partner partner, Map<String, Object> partnerMap, Lead lead)
      throws AxelorException {

    if (partnerMap != null) {
      Address address = lead.getAddress();
      if (address != null && (address.getAddressL6() == null || address.getCountry() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(CrmExceptionMessage.LEAD_PARTNER_MISSING_ADDRESS));
      }

      partner =
          (Partner)
              convertWizardService.createObject(
                  partnerMap, Mapper.toBean(Partner.class, null), Mapper.of(Partner.class));
      partner = this.createPartner(partner, address);
      partner.setIsProspect(true);
      // TODO check all required fields...
    } else if (partner != null) {
      if (!partner.getIsCustomer()) {
        partner.setIsProspect(true);
      }
    }
    return partner;
  }

  @SuppressWarnings("unchecked")
  protected List<Partner> createContactData(
      Lead lead, List<Partner> contactPartnerList, Map<String, Object> contactPartnerMap)
      throws AxelorException {

    if (contactPartnerMap != null) {
      Partner contactPartner = null;
      Address address = lead.getAddress();
      if (address != null && (address.getAddressL6() == null || address.getCountry() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(CrmExceptionMessage.LEAD_CONTACT_MISSING_ADDRESS));
      }
      contactPartner =
          (Partner)
              convertWizardService.createObject(
                  contactPartnerMap, Mapper.toBean(Partner.class, null), Mapper.of(Partner.class));

      contactPartner = this.createPartner(contactPartner, address);
      contactPartner.setIsContact(true);
      contactPartnerList.add(contactPartner);
      // TODO check all required fields...
    }
    return contactPartnerList;
  }

  @Override
  public List<Partner> convertMapListToPartnerList(List<Map<String, Object>> contactList) {
    List<Partner> partnerList = new ArrayList<>();
    if (contactList != null) {
      for (Map<String, Object> contactMap : contactList) {
        Partner partner = partnerRepository.find(Long.valueOf(contactMap.get("id").toString()));
        partnerList.add(partner);
      }
    }
    return partnerList;
  }
}
