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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerContactLink;
import com.axelor.apps.base.db.repo.PartnerContactLinkRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.message.db.EmailAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TestPartnerContactLinkService {

  private static PartnerContactLinkServiceImpl partnerContactLinkService;

  @BeforeAll
  static void prepare() {
    partnerContactLinkService =
        new PartnerContactLinkServiceImpl(
            Mockito.mock(PartnerContactLinkRepository.class),
            Mockito.mock(PartnerRepository.class));
  }

  @Test
  void testMainCompanyWithoutLinkCreatesMainLink() {
    Partner company = createCompany(1L);
    Partner contact = createContact();
    contact.setMainPartner(company);
    contact.setFixedPhone("0102030405");
    contact.setEmailAddress(new EmailAddress("john@example.com"));

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertEquals(1, contact.getPartnerContactLinkList().size());
    PartnerContactLink link = contact.getPartnerContactLinkList().get(0);
    Assertions.assertSame(company, link.getPartner());
    Assertions.assertTrue(link.getIsMainCompany());
    Assertions.assertTrue(link.getIsActive());
    Assertions.assertEquals("0102030405", link.getFixedPhone());
    Assertions.assertEquals("john@example.com", link.getEmailAddress().getAddress());
    Assertions.assertSame(company, contact.getMainPartner());
  }

  @Test
  void testMainCompanyWithExistingLinkStarsIt() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    PartnerContactLink linkB = addLink(contact, companyB, false);
    linkB.setFixedPhone("0600000000");
    contact.setMainPartner(companyB);

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertEquals(2, contact.getPartnerContactLinkList().size());
    Assertions.assertFalse(linkA.getIsMainCompany());
    Assertions.assertTrue(linkB.getIsMainCompany());
    Assertions.assertSame(companyB, contact.getMainPartner());
    Assertions.assertEquals("0600000000", contact.getFixedPhone());
  }

  @Test
  void testStarredLinkWithoutMainCompanyIsProjected() {
    Partner companyA = createCompany(1L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    linkA.setFunctionBusinessCard("CEO");

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertSame(companyA, contact.getMainPartner());
    Assertions.assertEquals("CEO", contact.getFunctionBusinessCard());
    Assertions.assertTrue(linkA.getIsMainCompany());
  }

  @Test
  void testNoMainCompanyAndNoStarLeavesContactWithoutCompany() {
    Partner contact = createContact();
    addLink(contact, createCompany(1L), false);
    contact.setFixedPhone("0102030405");

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertNull(contact.getMainPartner());
    Assertions.assertEquals("0102030405", contact.getFixedPhone());
    Assertions.assertFalse(contact.getPartnerContactLinkList().get(0).getIsMainCompany());
  }

  @Test
  void testContactWithoutAnyCompanyStaysUntouched() {
    Partner contact = createContact();

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertNull(contact.getMainPartner());
    Assertions.assertTrue(contact.getPartnerContactLinkList().isEmpty());
  }

  @Test
  void testToggledStarWinsOverMainCompanyAndClearsOtherStars() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    PartnerContactLink linkB = addLink(contact, companyB, true);
    linkB.setIsMainCompanyToggled(true);
    contact.setMainPartner(companyA);

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertFalse(linkA.getIsMainCompany());
    Assertions.assertTrue(linkB.getIsMainCompany());
    Assertions.assertSame(companyB, contact.getMainPartner());
    Assertions.assertFalse(linkA.getIsMainCompanyToggled());
    Assertions.assertFalse(linkB.getIsMainCompanyToggled());
  }

  @Test
  void testToggledUnstarClearsMainCompany() {
    Partner companyA = createCompany(1L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, false);
    linkA.setIsMainCompanyToggled(true);
    contact.setMainPartner(companyA);

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertFalse(linkA.getIsMainCompany());
    Assertions.assertNull(contact.getMainPartner());
    Assertions.assertEquals(1, contact.getPartnerContactLinkList().size());
    Assertions.assertFalse(linkA.getIsMainCompanyToggled());
  }

  @Test
  void testDeactivatedMainLineLeavesContactWithoutMainCompany() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    addLink(contact, companyB, false);
    linkA.setIsActive(false);
    contact.setMainPartner(companyA);

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertFalse(linkA.getIsMainCompany());
    Assertions.assertFalse(linkA.getIsActive());
    Assertions.assertNull(contact.getMainPartner());
  }

  @Test
  void testInactiveStarredLineIsIgnoredWithoutMainCompany() {
    Partner companyA = createCompany(1L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    linkA.setIsActive(false);

    partnerContactLinkService.updateMainPartnerLink(contact);

    Assertions.assertFalse(linkA.getIsMainCompany());
    Assertions.assertNull(contact.getMainPartner());
  }

  @Test
  void testOnlyActiveLinesKeepTheContactInTheCompanyContacts() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    addLink(contact, companyA, true);
    PartnerContactLink linkB = addLink(contact, companyB, false);
    linkB.setIsActive(false);
    companyB.addContactPartnerSetItem(contact);

    partnerContactLinkService.synchronizeCompanyMembership(
        contact, java.util.List.of(companyB), contact.getPartnerContactLinkList());

    Assertions.assertTrue(companyA.getContactPartnerSet().contains(contact));
    Assertions.assertFalse(companyB.getContactPartnerSet().contains(contact));
  }

  @Test
  void testRemovedLineTakesTheContactOutOfTheCompanyContacts() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    addLink(contact, companyA, true);
    companyA.addContactPartnerSetItem(contact);
    companyB.addContactPartnerSetItem(contact);

    partnerContactLinkService.synchronizeCompanyMembership(
        contact, java.util.List.of(companyA, companyB), contact.getPartnerContactLinkList());

    Assertions.assertTrue(companyA.getContactPartnerSet().contains(contact));
    Assertions.assertFalse(companyB.getContactPartnerSet().contains(contact));
  }

  @Test
  void testCompanySideLinkOfLegacyContactCopiesContactValues() {
    Partner companyA = createCompany(1L);
    Partner contact = createContact();
    contact.setMainPartner(companyA);
    contact.setFixedPhone("0102030405");
    contact.setEmailAddress(new EmailAddress("john@example.com"));

    partnerContactLinkService.createLink(companyA, contact);

    Assertions.assertEquals(1, contact.getPartnerContactLinkList().size());
    PartnerContactLink link = contact.getPartnerContactLinkList().get(0);
    Assertions.assertTrue(link.getIsMainCompany());
    Assertions.assertEquals("0102030405", link.getFixedPhone());
    Assertions.assertEquals("john@example.com", link.getEmailAddress().getAddress());
    Assertions.assertEquals("0102030405", contact.getFixedPhone());
    Assertions.assertSame(companyA, contact.getMainPartner());
  }

  @Test
  void testCompanySideLinkOfContactWithAnotherMainCompanyIsNotMain() {
    Partner companyA = createCompany(1L);
    Partner companyB = createCompany(2L);
    Partner contact = createContact();
    addLink(contact, companyA, true).setFixedPhone("0102030405");
    contact.setMainPartner(companyA);
    contact.setFixedPhone("0102030405");

    partnerContactLinkService.createLink(companyB, contact);

    PartnerContactLink linkB =
        contact.getPartnerContactLinkList().stream()
            .filter(link -> link.getPartner() == companyB)
            .findFirst()
            .orElseThrow();
    Assertions.assertFalse(linkB.getIsMainCompany());
    Assertions.assertNull(linkB.getFixedPhone());
    Assertions.assertSame(companyA, contact.getMainPartner());
    Assertions.assertEquals("0102030405", contact.getFixedPhone());
  }

  @Test
  void testContactValuesArePushedOntoAnEmptyMainLine() {
    Partner companyA = createCompany(1L);
    Partner contact = createContact();
    PartnerContactLink linkA = addLink(contact, companyA, true);
    contact.setMainPartner(companyA);
    contact.setFixedPhone("0102030405");
    contact.setEmailAddress(new EmailAddress("john@example.com"));

    partnerContactLinkService.updateMainPartnerLinkFromContact(contact);

    Assertions.assertEquals("0102030405", linkA.getFixedPhone());
    Assertions.assertEquals("john@example.com", linkA.getEmailAddress().getAddress());
    Assertions.assertEquals("0102030405", contact.getFixedPhone());
    Assertions.assertTrue(linkA.getIsMainCompany());
  }

  private static Partner createCompany(Long id) {
    Partner company = new Partner();
    company.setId(id);
    company.setName("Company " + id);
    return company;
  }

  private static Partner createContact() {
    Partner contact = new Partner();
    contact.setIsContact(true);
    contact.setName("Doe");
    contact.setFirstName("John");
    return contact;
  }

  private static PartnerContactLink addLink(Partner contact, Partner company, boolean isMain) {
    PartnerContactLink link = new PartnerContactLink();
    link.setPartner(company);
    link.setIsActive(true);
    link.setIsMainCompany(isMain);
    contact.addPartnerContactLinkListItem(link);
    return link;
  }
}
