package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.LeadStatus;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MultiRelated;
import com.axelor.apps.message.db.repo.MultiRelatedRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConvertLeadWizardServiceImpl implements ConvertLeadWizardService {

  protected LeadService leadService;

  protected ConvertWizardService convertWizardService;

  protected AddressService addressService;

  protected PartnerService partnerService;

  protected CountryRepository countryRepo;

  protected AppBaseService appBaseService;

  protected AppCrmService appCrmService;

  protected MultiRelatedRepository multiRelatedRepository;

  protected ConvertWizardOpportunityService convertWizardOpportunityService;

  @Inject
  public ConvertLeadWizardServiceImpl(
      LeadService leadService,
      ConvertWizardService convertWizardService,
      AddressService addressService,
      PartnerService partnerService,
      CountryRepository countryRepo,
      AppBaseService appBaseService,
      AppCrmService appCrmService,
      MultiRelatedRepository multiRelatedRepository,
      ConvertWizardOpportunityService convertWizardOpportunityService) {
    super();
    this.leadService = leadService;
    this.convertWizardService = convertWizardService;
    this.addressService = addressService;
    this.partnerService = partnerService;
    this.countryRepo = countryRepo;
    this.appBaseService = appBaseService;
    this.appCrmService = appCrmService;
    this.multiRelatedRepository = multiRelatedRepository;
    this.convertWizardOpportunityService = convertWizardOpportunityService;
  }

  /**
   * Create a partner from a lead
   *
   * @param lead
   * @return
   * @throws AxelorException
   */
  protected Partner createPartner(Partner partner, Address primaryAddress) throws AxelorException {

    this.setEmailAddress(partner);

    if (appBaseService.getAppBase().getGeneratePartnerSequence()) {
      partner.setPartnerSeq(leadService.getSequence());
    }

    partnerService.setPartnerFullName(partner);

    this.setAddress(partner, primaryAddress);

    Company activeCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (activeCompany != null) {
      partner.addCompanySetItem(activeCompany);
      if (partner.getCurrency() == null) {
        partner.setCurrency(activeCompany.getCurrency());
      }
    }
    Beans.get(BaseConvertLeadWizardService.class).setPartnerFields(partner);

    return partner;
  }

  public void setEmailAddress(Partner partner) {

    EmailAddress emailAddress = partner.getEmailAddress();

    if (emailAddress != null) {
      partner.setEmailAddress(this.createEmailAddress(emailAddress.getAddress(), null, partner));
    }
  }

  protected void setAddress(Partner partner, Address primaryAddress) {

    if (primaryAddress != null) {
      primaryAddress.setFullName(addressService.computeFullName(primaryAddress));
      if (!partner.getIsContact()) {
        partnerService.addPartnerAddress(partner, primaryAddress, true, true, true);
      }
      partner.setMainAddress(primaryAddress);
    }
  }

  @SuppressWarnings("unchecked")
  public Address createPrimaryAddress(Lead lead) {

    String addressL4 = lead.getPrimaryAddress();
    if (addressL4 == null) {
      return null;
    }
    String addressL5 = lead.getPrimaryState() != null ? lead.getPrimaryState().getName() : null;
    String addressL6 =
        lead.getPrimaryPostalCode()
            + " "
            + (lead.getPrimaryCity() != null ? lead.getPrimaryCity().getName() : "");

    Country addressL7Country = lead.getPrimaryCountry();

    Address address =
        addressService.getAddress(null, null, addressL4, addressL5, addressL6, addressL7Country);

    if (address == null) {
      address =
          addressService.createAddress(
              null, null, addressL4, addressL5, addressL6, addressL7Country);
    }

    return address;
  }

  protected EmailAddress createEmailAddress(String address, Lead lead, Partner partner) {
    EmailAddress emailAddress = new EmailAddress();
    emailAddress.setAddress(address);
    emailAddress.setLead(lead);
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
  protected Lead convertLead(Lead lead, Partner partner, Partner contactPartner)
      throws AxelorException {

    LeadStatus lostLeadStatus = appCrmService.getLostLeadStatus();
    LeadStatus convertedLeadStatus = appCrmService.getConvertedLeadStatus();

    LeadStatus leadStatus = lead.getLeadStatus();
    if (leadStatus == null || leadStatus.equals(lostLeadStatus)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(CrmExceptionMessage.LEAD_CONVERT_WRONG_STATUS));
    }

    if (partner != null && contactPartner != null) {
      if (partner.getContactPartnerSet() == null) {
        partner.setContactPartnerSet(new HashSet<>());
      }
      partner.getContactPartnerSet().add(contactPartner);
      contactPartner.setMainPartner(partner);
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
        if (contactPartner != null) {
          MultiRelated contactMultiRelated = new MultiRelated();
          contactMultiRelated.setRelatedToSelect(Partner.class.getName());
          contactMultiRelated.setRelatedToSelectId(contactPartner.getId());
          contactMultiRelated.setMessage(multiRelated.getMessage());
          multiRelatedRepository.save(contactMultiRelated);
        }
      }
    }

    for (Event event : lead.getEventList()) {
      event.setPartner(partner);
      event.setContactPartner(contactPartner);
    }
    lead.setIsConverted(true);
    lead.setLeadStatus(convertedLeadStatus);
    return lead;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Lead convertLeadWithContext(Context context) throws AxelorException {
    Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");

    Lead lead = Beans.get(LeadRepository.class).find(((Integer) leadContext.get("id")).longValue());
    Integer leadToPartnerSelect = (Integer) context.get("leadToPartnerSelect");
    Integer leadToContactSelect = (Integer) context.get("leadToContactSelect");
    Opportunity opportunity = null;
    if (context.containsKey("isCreateOpportunity")
        && (Boolean) context.get("isCreateOpportunity")) {
      opportunity =
          (Opportunity)
              convertWizardService.createObject(
                  (Map<String, Object>) context.get("opportunity"),
                  Mapper.toBean(Opportunity.class, null),
                  Mapper.of(Opportunity.class));
    }

    Partner partner = null;
    Partner contactPartner = null;

    if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
      partner =
          (Partner)
              convertWizardService.createObject(
                  (Map<String, Object>) context.get("partner"),
                  Mapper.toBean(Partner.class, null),
                  Mapper.of(Partner.class));
    } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
      Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectPartner");
      partner =
          Beans.get(PartnerRepository.class)
              .find(((Integer) selectPartnerContext.get("id")).longValue());
    }

    if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      contactPartner =
          (Partner)
              convertWizardService.createObject(
                  (Map<String, Object>) context.get("contactPartner"),
                  Mapper.toBean(Partner.class, null),
                  Mapper.of(Partner.class));
    } else if (leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      Map<String, Object> selectContactContext = (Map<String, Object>) context.get("selectContact");
      contactPartner =
          Beans.get(PartnerRepository.class)
              .find(((Integer) selectContactContext.get("id")).longValue());
    }

    lead =
        this.generateDataAndConvertLeadAndGenerateOpportunity(
            lead, leadToPartnerSelect, leadToContactSelect, partner, contactPartner, opportunity);
    return lead;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Lead generateDataAndConvertLeadAndGenerateOpportunity(
      Lead lead,
      Integer leadToPartnerSelect,
      Integer leadToContactSelect,
      Partner partner,
      Partner contactPartner,
      Opportunity opportunity)
      throws AxelorException {

    partner = createPartnerData(leadToPartnerSelect, partner, lead);

    if (partner != null) {
      contactPartner = createContactData(leadToContactSelect, lead, partner, contactPartner);
    }

    lead = this.convertLead(lead, partner, contactPartner);
    if (opportunity != null) {
      convertWizardOpportunityService.createOpportunity(opportunity, partner);
    }
    return lead;
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  protected Partner createPartnerData(Integer leadToPartnerSelect, Partner partner, Lead lead)
      throws AxelorException {

    if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
      Address primaryAddress = this.createPrimaryAddress(lead);
      if (primaryAddress != null
          && (primaryAddress.getAddressL6() == null
              || primaryAddress.getAddressL7Country() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(CrmExceptionMessage.LEAD_PARTNER_MISSING_ADDRESS));
      }
      partner = this.createPartner(partner, primaryAddress);
      // TODO check all required fields...
    } else if (leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
      if (!partner.getIsCustomer()) {
        partner.setIsProspect(true);
      }
    }
    return partner;
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  protected Partner createContactData(
      Integer leadToContactSelect, Lead lead, Partner partner, Partner contactPartner)
      throws AxelorException {

    if (leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
      Address primaryAddress = this.createPrimaryAddress(lead);
      if (primaryAddress != null
          && (primaryAddress.getAddressL6() == null
              || primaryAddress.getAddressL7Country() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(CrmExceptionMessage.LEAD_CONTACT_MISSING_ADDRESS));
      }

      contactPartner = this.createPartner(contactPartner, primaryAddress);
      contactPartner.setIsContact(true);
      // TODO check all required fields...
    } else if (leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT
        && partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {

    }

    return contactPartner;
  }
}
