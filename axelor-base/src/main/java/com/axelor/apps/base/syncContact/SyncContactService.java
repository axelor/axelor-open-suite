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
package com.axelor.apps.base.syncContact;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Function;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SyncContact;
import com.axelor.apps.base.db.SyncContactHistoric;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.FunctionRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.SyncContactRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.tool.EmailTool;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Response;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Organization;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/synccontact")
public class SyncContactService {

  private PartnerRepository partnerRepo;
  private CompanyRepository companyRepo;
  private CityRepository cityRepo;
  private CountryRepository countryRepo;
  private AddressRepository addressRepo;
  private SyncContactRepository syncContactRepo;
  private EmailAddressRepository emailAddressRepo;
  private UserService userService;
  private FunctionRepository functionRepo;

  private String importOrigin =
      "Import Google Contact " + Beans.get(AppBaseService.class).getTodayDate();

  private static final String SYNC_CONTACT_OLD_EMAIL = /*$$(*/ "Old email" /*)*/;
  private static final String SYNC_CONTACT_GOOGLE_EMAIL = /*$$(*/ "Google email" /*)*/;
  private static final String SYNC_CONTACT_OLD_ADDR = /*$$(*/ "Old address" /*)*/;
  private static final String SYNC_CONTACT_GOOGLE_ADDR = /*$$(*/ "Google address" /*)*/;
  private static final String SYNC_CONTACT_OLD_PHONE_NUMB = /*$$(*/ "Old phone number" /*)*/;
  private static final String SYNC_CONTACT_GOOGLE_PHONE_NUMB = /*$$(*/ "Google phone number" /*)*/;
  private static final String SYNC_CONTACT_COMPANY = /*$$(*/ "Company" /*)*/;
  private static final String SYNC_CONTACT_OLD_JOBTITLE = /*$$(*/ "Old job title" /*)*/;
  private static final String SYNC_CONTACT_NEW_JOBTITLE = /*$$(*/ "New job title" /*)*/;
  private static final String SYNC_CONTACT_AUTH_FAILED = /*$$(*/ "Authentication failed." /*)*/;
  private static final String SYNC_CONTACT_NO_IMPORT = /*$$(*/ "No contact to import." /*)*/;
  private static final String SYNC_CONTACT_IMPORT_SUCCESSFUL = /*$$(*/ "Import successful." /*)*/;

  @Inject
  public SyncContactService(
      PartnerRepository partnerRepo,
      CompanyRepository companyRepo,
      CityRepository cityRepo,
      CountryRepository countryRepo,
      AddressRepository addressRepo,
      SyncContactRepository syncContactRepo,
      EmailAddressRepository emailAddressRepo,
      UserService userService,
      FunctionRepository functionRepo) {
    this.partnerRepo = partnerRepo;
    this.companyRepo = companyRepo;
    this.cityRepo = cityRepo;
    this.countryRepo = countryRepo;
    this.addressRepo = addressRepo;
    this.syncContactRepo = syncContactRepo;
    this.emailAddressRepo = emailAddressRepo;
    this.userService = userService;
    this.functionRepo = functionRepo;
  }

  @POST
  @Path("/key/{id}")
  public SyncContactResponse getKeyAndClientId(@PathParam("id") Long id) {
    SyncContact syncContact = syncContactRepo.find(id);
    if (syncContact == null) {
      return null;
    }
    SyncContactResponse response = new SyncContactResponse();
    response.setClientid(syncContact.getCid());
    response.setKey(syncContact.getGoogleApiKey());
    response.setAuthFailed(I18n.get(SYNC_CONTACT_AUTH_FAILED));
    response.setImportSuccessful(I18n.get(SYNC_CONTACT_IMPORT_SUCCESSFUL));
    response.setNoImport(I18n.get(SYNC_CONTACT_NO_IMPORT));
    return response;
  }

  @POST
  @Path("/sync/{id}")
  public Response importContact(@PathParam("id") Long id, PeopleRequest request) {
    if (request == null || request.getPeople() == null || id == null) {
      return new Response();
    }
    importAllContact(id, request.getPeople());
    return new Response();
  }

  public void importAllContact(Long id, List<Person> people) {
    int i = 0;
    SyncContact syncContact = syncContactRepo.find(id);
    if (syncContact == null) {
      return;
    }
    SyncContactHistoric syncContactHistoric = new SyncContactHistoric();
    for (Person googlePerson : people) {
      Partner partner = importContact(googlePerson, syncContact.getUpdateContactField());
      if (partner != null) {
        syncContactHistoric.addPartnerSetItem(partner);
      }
      if (i % 10 == 0) {
        JPA.clear();
      }
      i++;
    }
    updateSyncContact(id, syncContactHistoric);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateSyncContact(Long id, SyncContactHistoric syncContactHistoric) {
    SyncContact syncContact;
    syncContact = syncContactRepo.find(id);
    syncContactHistoric.setUser(userService.getUser());
    Set<Partner> partnerSet = new HashSet<>();
    for (Partner partner : syncContactHistoric.getPartnerSet()) {
      Partner find = partnerRepo.find(partner.getId());
      if (find != null) {
        partnerSet.add(find);
      }
    }
    syncContactHistoric.clearPartnerSet();
    syncContactHistoric.setPartnerSet(partnerSet);
    syncContact.addSyncContactHistoricListItem(syncContactHistoric);
    syncContactRepo.save(syncContact);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Partner importContact(Person googlePerson, Boolean updateContactField) {
    if (googlePerson.getNames() == null) {
      return null;
    }
    Name nameGoogle = Mapper.toBean(Name.class, googlePerson.getNames().get(0));
    if (Strings.isNullOrEmpty(nameGoogle.getFamilyName())) {
      return null;
    }
    nameGoogle.setFamilyName(nameGoogle.getFamilyName().trim());
    String query = "self.name = '" + nameGoogle.getFamilyName() + "' ";
    if (!Strings.isNullOrEmpty(nameGoogle.getGivenName())) {
      nameGoogle.setGivenName(nameGoogle.getGivenName().trim());
      query += " AND self.firstName = '" + nameGoogle.getGivenName() + "' ";
    }
    Partner partner = partnerRepo.all().filter(query).fetchOne();
    if (partner == null) {
      partner = createPartner(googlePerson, nameGoogle);
    } else {
      partner = updatePartner(googlePerson, partner, updateContactField);
    }
    importCompany(googlePerson, partner, updateContactField);
    return partner;
  }

  public Partner createPartner(Person googlePerson, Name googleName) {
    Partner partner = new Partner();
    setDefaultPartnerValue(partner);
    importName(googleName, partner);
    importEmailAddress(googlePerson, partner, true);
    importAddress(googlePerson, partner, true);
    importPhoneNumber(googlePerson, partner, true);
    return partnerRepo.save(partner);
  }

  protected void setDefaultPartnerValue(Partner partner) {
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
    Sequence partnerSeq =
        Beans.get(SequenceRepository.class).findByCode(SequenceRepository.PARTNER);
    String seq = Beans.get(SequenceService.class).getSequenceNumber(partnerSeq);
    partner.setUser(userService.getUser());
    partner.setPartnerSeq(seq);
    partner.setIsContact(true);
    partner.setImportOrigin(importOrigin);
  }

  protected void importName(Name googleName, Partner partner) {
    String fullName = googleName.getFamilyName() + " " + googleName.getGivenName();
    partner.setName(googleName.getFamilyName());
    partner.setFirstName(googleName.getGivenName());
    partner.setFullName(fullName);
    partner.setSimpleFullName(fullName);
  }

  protected void importEmailAddress(
      Person googlePerson, Partner partner, Boolean updateContactField) {
    if (googlePerson.getEmailAddresses() == null) {
      return;
    }
    com.google.api.services.people.v1.model.EmailAddress googleEmail =
        Mapper.toBean(
            com.google.api.services.people.v1.model.EmailAddress.class,
            googlePerson.getEmailAddresses().get(0));
    if (Strings.isNullOrEmpty(googleEmail.getValue())
        || !EmailTool.isValidEmailAddress(googleEmail.getValue())) {
      return;
    }
    if (partner.getEmailAddress() == null
        || Strings.isNullOrEmpty(partner.getEmailAddress().getAddress())) {
      EmailAddress partnerEmail = createEmailAddress(googleEmail.getValue());
      partnerEmail.setPartner(partner);
      partner.setEmailAddress(partnerEmail);
    } else {
      if (!partner.getEmailAddress().getAddress().equalsIgnoreCase(googleEmail.getValue())) {
        if (updateContactField) {
          EmailAddress partnerEmail = createEmailAddress(googleEmail.getValue());
          updateDescription(
              partner, I18n.get(SYNC_CONTACT_OLD_EMAIL), partner.getEmailAddress().getAddress());
          partnerEmail.setPartner(partner);
          partner.setEmailAddress(partnerEmail);
        } else {
          updateDescription(partner, I18n.get(SYNC_CONTACT_GOOGLE_EMAIL), googleEmail.getValue());
        }
      }
    }
  }

  protected EmailAddress createEmailAddress(String googleEmail) {
    EmailAddress email = new EmailAddress();
    email.setAddress(googleEmail);
    email.setName(googleEmail);
    email.setImportOrigin(importOrigin);
    return emailAddressRepo.save(email);
  }

  protected void importAddress(Person googlePerson, Partner partner, Boolean updateContactField) {
    if (googlePerson.getAddresses() == null) {
      return;
    }
    com.google.api.services.people.v1.model.Address googleAddr =
        Mapper.toBean(
            com.google.api.services.people.v1.model.Address.class,
            googlePerson.getAddresses().get(0));
    // Google contact has empty address or not enough fields to create an address
    if (Strings.isNullOrEmpty(googleAddr.getCountryCode())
        || Strings.isNullOrEmpty(googleAddr.getCountry())
        || Strings.isNullOrEmpty(googleAddr.getStreetAddress())
        || Strings.isNullOrEmpty(googleAddr.getPostalCode())) {
      return;
    }
    String query =
        "self.zip = '"
            + googleAddr.getPostalCode()
            + "' AND self.addressL7Country.alpha2Code = '"
            + googleAddr.getCountryCode()
            + "' AND self.addressL4 = '"
            + googleAddr.getStreetAddress()
            + "'";
    if (!Strings.isNullOrEmpty(googleAddr.getCity())) {
      query += " AND self.city.name = '" + googleAddr.getCity() + "'";
    }
    Address partnerAddr = addressRepo.all().filter(query).fetchOne();
    if (partnerAddr == null) {
      partnerAddr = createAddress(googleAddr);
    }
    if (partner.getMainAddress() == null) {
      partner.setMainAddress(partnerAddr);
      createPartnerAddress(partner, partnerAddr);
    } else {
      if (!partner.getMainAddress().equals(partnerAddr)) {
        if (updateContactField) {
          updateDescription(
              partner, I18n.get(SYNC_CONTACT_OLD_ADDR), partner.getMainAddress().getFullName());
          partner.setMainAddress(partnerAddr);
          createPartnerAddress(partner, partnerAddr);
        } else if (Strings.isNullOrEmpty(partner.getDescription())
            || (partner.getDescription() != null
                && !partner.getDescription().contains(googleAddr.getStreetAddress()))) {
          updateDescription(
              partner, I18n.get(SYNC_CONTACT_GOOGLE_ADDR), googleAddr.getFormattedValue());
        }
      }
    }
  }

  protected void createPartnerAddress(Partner partner, Address partnerAddr) {
    PartnerAddress partnerAddress = new PartnerAddress();
    partnerAddress.setAddress(partnerAddr);
    partnerAddress.setImportOrigin(importOrigin);
    partnerAddress.setPartner(partner);
    partnerAddress.setIsDefaultAddr(true);
    partnerAddress.setIsDeliveryAddr(true);
    partnerAddress.setIsInvoicingAddr(true);
  }

  protected Address createAddress(com.google.api.services.people.v1.model.Address googleAddr) {
    Address partnerAddr = new Address();
    Country partnerCountry = countryRepo.findByName(googleAddr.getCountry());
    if (partnerCountry == null) {
      partnerCountry = createCountry(googleAddr.getCountry(), googleAddr.getCountryCode());
    }
    partnerAddr.setAddressL7Country(partnerCountry);
    if (!Strings.isNullOrEmpty(googleAddr.getCity())) {
      City partnerCity = cityRepo.findByName(googleAddr.getCity());
      if (partnerCity == null) {
        partnerCity = createCity(googleAddr.getCity(), partnerCountry);
      }
      partnerAddr.setCity(partnerCity);
    }
    StringBuilder addrL4 = new StringBuilder();
    if (!Strings.isNullOrEmpty(googleAddr.getPoBox())) {
      addrL4.append(googleAddr.getPoBox()).append(" - ");
    }
    partnerAddr.setZip(googleAddr.getPostalCode());
    addrL4.append(googleAddr.getStreetAddress());
    partnerAddr.setAddressL4(addrL4.toString());
    partnerAddr.setAddressL5(googleAddr.getCity());
    partnerAddr.setAddressL2(googleAddr.getExtendedAddress());
    partnerAddr.setAddressL6(googleAddr.getPostalCode() + " " + googleAddr.getCity());
    partnerAddr.setFullName(Beans.get(AddressService.class).computeFullName(partnerAddr));
    partnerAddr.setImportOrigin(importOrigin);
    return partnerAddr;
  }

  protected Country createCountry(String googleCountry, String googleCountryCode) {
    Country country = new Country();
    country.setAlpha2Code(googleCountryCode);
    country.setName(googleCountry);
    country.setImportOrigin(importOrigin);
    return countryRepo.save(country);
  }

  protected City createCity(String googleCity, Country country) {
    City city = new City();
    city.setName(googleCity);
    city.setCountry(country);
    city.setImportOrigin(importOrigin);
    return cityRepo.save(city);
  }

  protected void importPhoneNumber(
      Person googlePerson, Partner partner, Boolean updateContactField) {
    if (googlePerson.getPhoneNumbers() == null) {
      return;
    }
    PhoneNumber googleNumb =
        Mapper.toBean(PhoneNumber.class, googlePerson.getPhoneNumbers().get(0));
    if (!Strings.isNullOrEmpty(googleNumb.getCanonicalForm())) {
      if (partner.getMobilePhone() == null) {
        partner.setMobilePhone(googleNumb.getCanonicalForm().trim());
      } else {
        if (!partner.getMobilePhone().equals(googleNumb.getCanonicalForm().trim())) {
          if (updateContactField) {
            updateDescription(
                partner, I18n.get(SYNC_CONTACT_OLD_PHONE_NUMB), partner.getMobilePhone());
            partner.setMobilePhone(googleNumb.getCanonicalForm().trim());
          } else {
            updateDescription(
                partner,
                I18n.get(SYNC_CONTACT_GOOGLE_PHONE_NUMB),
                googleNumb.getCanonicalForm().trim());
          }
        }
      }
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Partner updatePartner(Person googlePerson, Partner partner, Boolean updateContactField) {
    Boolean toUpdate =
        updateContactField
            && partner.getIsContact()
            && !partner.getIsCustomer()
            && !partner.getIsSupplier();
    updatePartnerFields(googlePerson, partner, toUpdate);
    return partnerRepo.save(partner);
  }

  protected void updatePartnerFields(
      Person googlePerson, Partner partner, Boolean updateContactField) {
    importEmailAddress(googlePerson, partner, updateContactField);
    importAddress(googlePerson, partner, updateContactField);
    importPhoneNumber(googlePerson, partner, updateContactField);
  }

  protected void importCompany(Person googlePerson, Partner partner, Boolean updateContactField) {
    if (googlePerson.getOrganizations() == null) {
      return;
    }
    Organization googleCompany =
        Mapper.toBean(Organization.class, googlePerson.getOrganizations().get(0));
    if (!Strings.isNullOrEmpty(googleCompany.getName())) {
      Company company = companyRepo.findByName(googleCompany.getName().trim());
      if (company == null) {
        updateDescription(partner, I18n.get(SYNC_CONTACT_COMPANY), googleCompany.getName());
      } else if (partner.getCompanySet() == null
          || (partner.getCompanySet() != null && !partner.getCompanySet().contains(company))) {
        if (company.getPartner() != null) {
          if (partner.getMainPartner() == null) {
            partner.setMainPartner(company.getPartner());
          }
          company.getPartner().addContactPartnerSetItem(partner);
        }
        partner.addCompanySetItem(company);
      }
    }
    importJobTitle(partner, updateContactField, googleCompany);
  }

  protected void importJobTitle(
      Partner partner, Boolean updateContactField, Organization googleCompany) {
    String jobTitle = googleCompany.getTitle();
    if (!Strings.isNullOrEmpty(jobTitle)) {
      Function jobTitleFunction = functionRepo.findByName(jobTitle);
      if (partner.getJobTitleFunction() == null) {
        if (jobTitleFunction == null) {
          jobTitleFunction = createJobTitleFunction(jobTitle);
        }
        partner.setJobTitleFunction(jobTitleFunction);
      } else {
        if (partner.getJobTitleFunction() != null
            && !partner.getJobTitleFunction().equals(jobTitleFunction)) {
          if (updateContactField) {
            updateDescription(
                partner,
                I18n.get(SYNC_CONTACT_OLD_JOBTITLE),
                partner.getJobTitleFunction().getName());
            if (jobTitleFunction == null) {
              jobTitleFunction = createJobTitleFunction(jobTitle);
            }
            partner.setJobTitleFunction(jobTitleFunction);
          } else {
            updateDescription(partner, I18n.get(SYNC_CONTACT_NEW_JOBTITLE), jobTitle);
          }
        }
      }
    }
  }

  protected Function createJobTitleFunction(String name) {
    Function function = new Function();
    function.setName(name);
    return functionRepo.save(function);
  }

  protected void updateDescription(Partner partner, String field, String data) {
    String description = getPartnerDescription(partner);
    if (!description.contains(data)) {
      description += field + ": " + data;
      partner.setDescription(description);
    }
  }

  protected String getPartnerDescription(Partner partner) {
    if (Strings.isNullOrEmpty(partner.getDescription())) {
      return "";
    }
    return partner.getDescription().equals("<br>") ? "" : partner.getDescription() + "<br>";
  }
}
