/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.partner;

import static com.axelor.apps.base.db.repo.PartnerRepository.PARTNER_TYPE_COMPANY;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerContactLink;
import com.axelor.apps.base.db.PartnerRole;
import com.axelor.apps.base.db.repo.PartnerContactLinkRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PartnerContactLinkServiceImpl implements PartnerContactLinkService {

  protected PartnerContactLinkRepository partnerContactLinkRepository;

  @Inject
  public PartnerContactLinkServiceImpl(PartnerContactLinkRepository partnerContactLinkRepository) {
    this.partnerContactLinkRepository = partnerContactLinkRepository;
  }

  @Override
  public void onContactSave(Partner contact) throws AxelorException {
    if (!contact.getIsContact()) {
      return;
    }

    List<PartnerContactLink> links = getLinks(contact);
    validateLinks(links);
    synchronizeCompanyMembership(contact, findByContact(contact), links);
    projectMainLink(contact, links);
  }

  @Override
  public void afterPartnerSave(Partner partner) throws AxelorException {
    if (partner.getIsContact()
        || !Objects.equals(partner.getPartnerTypeSelect(), PARTNER_TYPE_COMPANY)
        || partner.getContactPartnerSet() == null) {
      return;
    }

    List<PartnerContactLink> persistedLinks = findByPartner(partner);
    Set<Long> contactIds = new HashSet<>();

    for (Partner contact : new ArrayList<>(partner.getContactPartnerSet())) {
      contactIds.add(contact.getId());
      if (findLink(persistedLinks, contact) == null) {
        createLink(partner, contact);
      }
    }

    for (PartnerContactLink link : persistedLinks) {
      if (!contactIds.contains(link.getContact().getId())) {
        removeLink(link);
      }
    }
  }

  @Override
  public void onPartnerRemove(Partner partner) throws AxelorException {
    Set<PartnerContactLink> links = new LinkedHashSet<>(findByContact(partner));
    links.addAll(findByPartner(partner));
    for (PartnerContactLink link : links) {
      removeLink(link);
    }
  }

  @Override
  public Map<String, Object> getMainLinkOnChangeValuesMap(Partner contact) {
    projectMainLink(contact, getLinks(contact));

    Map<String, Object> valuesMap = new HashMap<>();
    valuesMap.put("mainPartner", EntityHelper.getEntity(contact.getMainPartner()));
    valuesMap.put("emailAddress", EntityHelper.getEntity(contact.getEmailAddress()));
    valuesMap.put("fixedPhone", contact.getFixedPhone());
    valuesMap.put("jobTitleFunction", EntityHelper.getEntity(contact.getJobTitleFunction()));
    valuesMap.put("functionBusinessCard", contact.getFunctionBusinessCard());
    valuesMap.put("partnerRoleSet", copyRoles(contact.getPartnerRoleSet()));
    return valuesMap;
  }

  protected void createLink(Partner company, Partner contact) {
    boolean isMainCompany = findByContact(contact).isEmpty();

    PartnerContactLink link = new PartnerContactLink();
    link.setPartner(company);
    link.setIsActive(true);
    link.setIsMainCompany(isMainCompany);
    contact.addPartnerContactLinkListItem(link);

    if (isMainCompany) {
      copyContactValues(contact, link);
      projectMainLink(contact, List.of(link));
    }
    partnerContactLinkRepository.save(link);
  }

  protected void removeLink(PartnerContactLink link) {
    Partner contact = link.getContact();

    link.getPartner().removeContactPartnerSetItem(contact);
    contact.removePartnerContactLinkListItem(link);
    partnerContactLinkRepository.remove(link);

    if (link.getIsMainCompany()) {
      projectMainLink(contact, getLinks(contact));
    }
  }

  protected void validateLinks(List<PartnerContactLink> links) throws AxelorException {
    Set<Long> companyIds = new HashSet<>();
    for (PartnerContactLink link : links) {
      if (!companyIds.add(link.getPartner().getId())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.PARTNER_CONTACT_LINK_DUPLICATE_COMPANY));
      }
    }

    if (links.stream().filter(PartnerContactLink::getIsMainCompany).count() > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PARTNER_CONTACT_LINK_MULTIPLE_MAIN_COMPANIES));
    }
  }

  protected void synchronizeCompanyMembership(
      Partner contact, List<PartnerContactLink> persistedLinks, List<PartnerContactLink> links) {
    Set<Long> companyIds =
        links.stream()
            .map(link -> link.getPartner().getId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    for (PartnerContactLink link : persistedLinks) {
      if (!companyIds.contains(link.getPartner().getId())) {
        link.getPartner().removeContactPartnerSetItem(contact);
      }
    }
    for (PartnerContactLink link : links) {
      link.getPartner().addContactPartnerSetItem(contact);
    }
  }

  protected void projectMainLink(Partner contact, List<PartnerContactLink> links) {
    PartnerContactLink mainLink =
        links.stream().filter(PartnerContactLink::getIsMainCompany).findFirst().orElse(null);
    if (mainLink == null) {
      contact.setMainPartner(null);
      return;
    }

    contact.setMainPartner(mainLink.getPartner());
    contact.setEmailAddress(mainLink.getEmailAddress());
    contact.setFixedPhone(mainLink.getFixedPhone());
    contact.setJobTitleFunction(mainLink.getJobTitleFunction());
    contact.setFunctionBusinessCard(mainLink.getFunctionBusinessCard());
    contact.setPartnerRoleSet(copyRoles(mainLink.getPartnerRoleSet()));
  }

  protected void copyContactValues(Partner contact, PartnerContactLink link) {
    link.setEmailAddress(contact.getEmailAddress());
    link.setFixedPhone(contact.getFixedPhone());
    link.setJobTitleFunction(contact.getJobTitleFunction());
    link.setFunctionBusinessCard(contact.getFunctionBusinessCard());
    link.setPartnerRoleSet(copyRoles(contact.getPartnerRoleSet()));
  }

  protected Set<PartnerRole> copyRoles(Set<PartnerRole> partnerRoleSet) {
    return partnerRoleSet == null ? new HashSet<>() : new HashSet<>(partnerRoleSet);
  }

  protected List<PartnerContactLink> getLinks(Partner contact) {
    if (contact.getPartnerContactLinkList() == null) {
      contact.setPartnerContactLinkList(new ArrayList<>());
    }
    return contact.getPartnerContactLinkList();
  }

  protected PartnerContactLink findLink(List<PartnerContactLink> links, Partner contact) {
    return links.stream()
        .filter(link -> Objects.equals(link.getContact().getId(), contact.getId()))
        .findFirst()
        .orElse(null);
  }

  protected List<PartnerContactLink> findByContact(Partner contact) {
    return partnerContactLinkRepository
        .all()
        .autoFlush(false)
        .filter("self.contact = :contact")
        .bind("contact", contact)
        .fetch();
  }

  protected List<PartnerContactLink> findByPartner(Partner partner) {
    return partnerContactLinkRepository
        .all()
        .autoFlush(false)
        .filter("self.partner = :partner")
        .bind("partner", partner)
        .fetch();
  }
}
