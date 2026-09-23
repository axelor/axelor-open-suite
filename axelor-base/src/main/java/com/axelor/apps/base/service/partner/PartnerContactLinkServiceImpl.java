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
import com.axelor.apps.base.db.repo.PartnerRepository;
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
  protected PartnerRepository partnerRepository;

  @Inject
  public PartnerContactLinkServiceImpl(
      PartnerContactLinkRepository partnerContactLinkRepository,
      PartnerRepository partnerRepository) {
    this.partnerContactLinkRepository = partnerContactLinkRepository;
    this.partnerRepository = partnerRepository;
  }

  @Override
  public void onContactSave(Partner contact) throws AxelorException {
    if (!contact.getIsContact()) {
      return;
    }

    validateLinks(getLinks(contact));
    updateMainPartnerLink(contact);
    synchronizeCompanyMembership(contact, findCompaniesListing(contact), getLinks(contact));
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
      PartnerContactLink link = findLink(persistedLinks, contact);
      if (link == null) {
        createLink(partner, contact);
      } else if (!link.getIsActive()) {
        link.setIsActive(true);
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
  public Map<String, Object> getMainPartnerLinkValuesMap(Partner contact) {
    updateMainPartnerLink(contact);

    Map<String, Object> valuesMap = new HashMap<>();
    valuesMap.put("partnerContactLinkList", getLinks(contact));
    valuesMap.put("mainPartner", EntityHelper.getEntity(contact.getMainPartner()));
    valuesMap.put("emailAddress", EntityHelper.getEntity(contact.getEmailAddress()));
    valuesMap.put("fixedPhone", contact.getFixedPhone());
    valuesMap.put("jobTitleFunction", EntityHelper.getEntity(contact.getJobTitleFunction()));
    valuesMap.put("functionBusinessCard", contact.getFunctionBusinessCard());
    valuesMap.put("partnerRoleSet", copyRoles(contact.getPartnerRoleSet()));
    return valuesMap;
  }

  @Override
  public void updateMainPartnerLink(Partner contact) {
    List<PartnerContactLink> links = getLinks(contact);
    PartnerContactLink mainLink = findMainLink(contact, links);

    for (PartnerContactLink link : links) {
      link.setIsMainCompany(link == mainLink);
      link.setIsMainCompanyToggled(false);
    }
    projectMainLink(contact, mainLink);
  }

  @Override
  public void updateMainPartnerLinkFromContact(Partner contact) {
    if (!contact.getIsContact() || contact.getMainPartner() == null) {
      return;
    }

    copyContactValues(contact, getOrCreateMainPartnerLink(contact));
    updateMainPartnerLink(contact);
  }

  @Override
  public PartnerContactLink createMainPartnerLink(Partner company) {
    return newLink(company, true);
  }

  /**
   * Finds the link to flag as main, or {@code null} when the contact has no main company.
   *
   * <p>Priority: a star the user just toggled, then the main company field (whose link is created
   * when missing), then the starred link. An inactive link is never main.
   */
  protected PartnerContactLink findMainLink(Partner contact, List<PartnerContactLink> links) {
    PartnerContactLink toggledLink =
        links.stream().filter(PartnerContactLink::getIsMainCompanyToggled).findFirst().orElse(null);
    if (toggledLink != null) {
      return toggledLink.getIsMainCompany() && toggledLink.getIsActive() ? toggledLink : null;
    }

    if (contact.getMainPartner() != null) {
      PartnerContactLink mainLink = getOrCreateMainPartnerLink(contact);
      return mainLink.getIsActive() ? mainLink : null;
    }

    return links.stream()
        .filter(link -> link.getIsMainCompany() && link.getIsActive())
        .findFirst()
        .orElse(null);
  }

  protected PartnerContactLink getOrCreateMainPartnerLink(Partner contact) {
    Partner mainPartner = contact.getMainPartner();
    PartnerContactLink mainLink = findLinkByPartner(getLinks(contact), mainPartner);
    if (mainLink == null) {
      mainLink = createMainPartnerLink(mainPartner);
      copyContactValues(contact, mainLink);
      contact.addPartnerContactLinkListItem(mainLink);
    }
    return mainLink;
  }

  protected void createLink(Partner company, Partner contact) {
    boolean isMainCompany =
        contact.getMainPartner() == null
            ? getLinks(contact).isEmpty()
            : Objects.equals(contact.getMainPartner(), company);

    PartnerContactLink link = newLink(company, isMainCompany);
    if (isMainCompany) {
      copyContactValues(contact, link);
    }
    contact.addPartnerContactLinkListItem(link);
    updateMainPartnerLink(contact);
  }

  protected PartnerContactLink newLink(Partner company, boolean isMainCompany) {
    PartnerContactLink link = new PartnerContactLink();
    link.setPartner(company);
    link.setIsActive(true);
    link.setIsMainCompany(isMainCompany);
    return link;
  }

  protected void removeLink(PartnerContactLink link) {
    Partner contact = link.getContact();

    link.getPartner().removeContactPartnerSetItem(contact);
    contact.removePartnerContactLinkListItem(link);
    partnerContactLinkRepository.remove(link);

    if (link.getIsMainCompany()) {
      contact.setMainPartner(null);
      updateMainPartnerLink(contact);
    }
  }

  protected void validateLinks(List<PartnerContactLink> links) throws AxelorException {
    Set<Long> companyIds = new HashSet<>();
    for (PartnerContactLink link : links) {
      if (link.getPartner() != null && !companyIds.add(link.getPartner().getId())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.PARTNER_CONTACT_LINK_DUPLICATE_COMPANY));
      }
    }
  }

  /**
   * Makes the contact a member of the companies of its active links only.
   *
   * <p>The companies currently listing the contact come from their contact set rather than from the
   * links: a link removed in the same save is already gone from the persistence context.
   */
  protected void synchronizeCompanyMembership(
      Partner contact, List<Partner> listingCompanies, List<PartnerContactLink> links) {
    Set<Long> activeCompanyIds =
        links.stream()
            .filter(PartnerContactLink::getIsActive)
            .map(link -> link.getPartner().getId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    for (Partner company : listingCompanies) {
      if (!activeCompanyIds.contains(company.getId())) {
        company.removeContactPartnerSetItem(contact);
      }
    }
    for (PartnerContactLink link : links) {
      if (link.getIsActive()) {
        link.getPartner().addContactPartnerSetItem(contact);
      }
    }
  }

  protected void projectMainLink(Partner contact, PartnerContactLink mainLink) {
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

  protected PartnerContactLink findLinkByPartner(List<PartnerContactLink> links, Partner partner) {
    return links.stream()
        .filter(link -> Objects.equals(link.getPartner(), partner))
        .findFirst()
        .orElse(null);
  }

  protected List<Partner> findCompaniesListing(Partner contact) {
    if (contact.getId() == null) {
      return List.of();
    }
    return partnerRepository
        .all()
        .filter(":contact MEMBER OF self.contactPartnerSet")
        .bind("contact", contact)
        .fetch();
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
