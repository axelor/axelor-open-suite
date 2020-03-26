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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.auth.AuthUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;

public class ConvertLeadWizardService {

  @Inject private LeadService leadService;

  @Inject private ConvertWizardService convertWizardService;

  @Inject private AddressService addressService;

  @Inject private PartnerService partnerService;

  @Inject private CountryRepository countryRepo;

  @Inject private AppBaseService appBaseService;

  /**
   * Create a partner from a lead
   *
   * @param lead
   * @return
   * @throws AxelorException
   */
  public Partner createPartner(Map<String, Object> context, Address primaryAddress)
      throws AxelorException {

    Mapper mapper = Mapper.of(Partner.class);
    Partner partner = Mapper.toBean(Partner.class, null);

    partner = (Partner) convertWizardService.createObject(context, partner, mapper);

    this.setEmailAddress(partner);

    if (appBaseService.getAppBase().getGeneratePartnerSequence()) {
      partner.setPartnerSeq(leadService.getSequence());
    }

    partnerService.setPartnerFullName(partner);

    this.setAddress(partner, primaryAddress);

    Company activeCompany = AuthUtils.getUser().getActiveCompany();
    if (activeCompany != null) {
      partner.addCompanySetItem(activeCompany);
      if (partner.getCurrency() == null) {
        partner.setCurrency(activeCompany.getCurrency());
      }
    }

    return partner;
  }

  public void setEmailAddress(Partner partner) {

    EmailAddress emailAddress = partner.getEmailAddress();

    if (emailAddress != null) {
      partner.setEmailAddress(this.createEmailAddress(emailAddress.getAddress(), null, partner));
    }
  }

  public void setAddress(Partner partner, Address primaryAddress) {

    if (primaryAddress != null) {
      primaryAddress.setFullName(addressService.computeFullName(primaryAddress));
      if (!partner.getIsContact()) {
        partnerService.addPartnerAddress(partner, primaryAddress, true, true, true);
      }
      partner.setMainAddress(primaryAddress);
    }
  }

  @SuppressWarnings("unchecked")
  public Address createPrimaryAddress(Map<String, Object> context) {

    Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");

    Lead lead = Beans.get(LeadRepository.class).find(((Integer) leadContext.get("id")).longValue());

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

  public EmailAddress createEmailAddress(String address, Lead lead, Partner partner) {
    EmailAddress emailAddress = new EmailAddress();
    emailAddress.setAddress(address);
    emailAddress.setLead(lead);
    emailAddress.setPartner(partner);

    return emailAddress;
  }
}
