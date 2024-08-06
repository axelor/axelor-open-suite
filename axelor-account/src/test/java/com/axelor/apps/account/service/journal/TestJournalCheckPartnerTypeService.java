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
package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.base.db.Partner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestJournalCheckPartnerTypeService {

  private static JournalCheckPartnerTypeService journalCheckPartnerTypeService;

  @BeforeAll
  static void prepare() {
    journalCheckPartnerTypeService = new JournalCheckPartnerTypeServiceImpl();
  }

  @Test
  void testNullJournalIsCompatible() {
    Partner partner = new Partner();
    Assertions.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(null, partner));
  }

  @Test
  void testEmptyJournalIsCompatible() {
    Journal journal = new Journal();
    Partner partner = new Partner();
    Assertions.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  void testCompatibilityOneType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assertions.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  void testNonCompatibilityOneType() {
    Partner partner = new Partner();
    partner.setIsCustomer(false);
    partner.setIsSupplier(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assertions.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  void testNonCompatibilityOneTypeNullableBoolean() {
    Partner partner = new Partner();
    partner.setIsCustomer(null);
    partner.setIsSupplier(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assertions.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  void testCompatibilityMultipleType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    partner.setIsEmployee(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER, JournalRepository.IS_PROSPECT);
    Assertions.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  void testNonCompatibilityMultipleType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    partner.setIsEmployee(true);
    Journal journal = createJournal(JournalRepository.IS_SUPPLIER, JournalRepository.IS_PROSPECT);
    Assertions.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  protected Journal createJournal(String... compatiblePartnerTypes) {
    Journal journal = new Journal();
    journal.setCompatiblePartnerTypeSelect(String.join(",", compatiblePartnerTypes));
    return journal;
  }
}
