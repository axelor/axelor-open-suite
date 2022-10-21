package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.base.db.Partner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJournalCheckPartnerTypeService {

  protected JournalCheckPartnerTypeService journalCheckPartnerTypeService;

  @Before
  public void prepare() {
    journalCheckPartnerTypeService = new JournalCheckPartnerTypeServiceImpl();
  }

  @Test
  public void testNullJournalIsCompatible() {
    Partner partner = new Partner();
    Assert.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(null, partner));
  }

  @Test
  public void testEmptyJournalIsCompatible() {
    Journal journal = new Journal();
    Partner partner = new Partner();
    Assert.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  public void testCompatibilityOneType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assert.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  public void testNonCompatibilityOneType() {
    Partner partner = new Partner();
    partner.setIsCustomer(false);
    partner.setIsSupplier(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assert.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  public void testNonCompatibilityOneTypeNullableBoolean() {
    Partner partner = new Partner();
    partner.setIsCustomer(null);
    partner.setIsSupplier(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER);
    Assert.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  public void testCompatibilityMultipleType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    partner.setIsEmployee(true);
    Journal journal = createJournal(JournalRepository.IS_CUSTOMER, JournalRepository.IS_PROSPECT);
    Assert.assertTrue(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  @Test
  public void testNonCompatibilityMultipleType() {
    Partner partner = new Partner();
    partner.setIsCustomer(true);
    partner.setIsEmployee(true);
    Journal journal = createJournal(JournalRepository.IS_SUPPLIER, JournalRepository.IS_PROSPECT);
    Assert.assertFalse(journalCheckPartnerTypeService.isPartnerCompatible(journal, partner));
  }

  protected Journal createJournal(String... compatiblePartnerTypes) {
    Journal journal = new Journal();
    journal.setCompatiblePartnerTypeSelect(String.join(",", compatiblePartnerTypes));
    return journal;
  }
}
