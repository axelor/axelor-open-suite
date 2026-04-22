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
package com.axelor.apps.account.service.note;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.InvoiceNoteTypeRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceNoteCreationHelper {
  protected static final Logger logger = LoggerFactory.getLogger(InvoiceNoteCreationHelper.class);

  protected InvoiceNoteCreationHelper() {
    throw new IllegalStateException("Utility class");
  }

  protected static boolean noteAlreadyExist(InvoiceNoteType noteType, Invoice invoice) {
    return invoice.getInvoiceNoteList().stream()
        .anyMatch(n -> n.getInvoiceNoteType().equals(noteType));
  }

  protected static InvoiceNote createInvoiceNote(InvoiceNoteType noteType, String noteContent) {
    InvoiceNote invoiceNote = new InvoiceNote();
    invoiceNote.setInvoiceNoteType(noteType);
    invoiceNote.setNote(noteContent);
    return invoiceNote;
  }

  protected static void generateFinancialDiscountNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeAAB = Beans.get(InvoiceNoteTypeRepository.class).findByCode("AAB");
    if (noteTypeAAB == null) {
      logger.warn("Note type AAB doesn't exist");
      return;
    }

    if (invoice.getFinancialDiscount() != null
        && !StringUtils.isBlank(invoice.getFinancialDiscount().getLegalNotice())) {
      String noteContent = invoice.getFinancialDiscount().getLegalNotice();
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAB, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
      return;
    }
    if (company.getAccountConfig().getDisplayNoFinancialDiscountAppliedOnPrinting()) {
      String noteContent = I18n.get("No discount will be granted for early payment.");
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAB, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
      return;
    }
    logger.info(
        "No content to display for note AAB, financial discount. Note creation was skipped");
  }

  protected static void generateGeneralInformationNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeAAI = Beans.get(InvoiceNoteTypeRepository.class).findByCode("AAI");
    if (noteTypeAAI == null) {
      logger.warn("Note type AAI doesn't exist");
      return;
    }

    String noteContent = "";
    if (!StringUtils.isBlank(company.getAccountConfig().getTermsAndConditions())) {
      noteContent = company.getAccountConfig().getTermsAndConditions();
      if (!StringUtils.isBlank(company.getAccountConfig().getInvoiceClientBox())) {
        noteContent += "\n" + company.getAccountConfig().getInvoiceClientBox();
      }
    }
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note AAI, general information. Note creation was skipped");
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAI, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateSellerLegalInformationNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeABL = Beans.get(InvoiceNoteTypeRepository.class).findByCode("ABL");
    if (noteTypeABL == null) {
      logger.warn("Note type ABL doesn't exist");
      return;
    }

    String noteContent = company.getLegalInformation();
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note ABL, seller information. Note creation was skipped");
      return;
    }

    InvoiceNote invoiceNote = createInvoiceNote(noteTypeABL, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateLumpSumIndemnityNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypePMT = Beans.get(InvoiceNoteTypeRepository.class).findByCode("PMT");
    if (noteTypePMT == null) {
      logger.warn("Note type PMT doesn't exist");
      return;
    }

    String noteContent = company.getAccountConfig().getSaleInvoiceLegalNote();
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note PMT, lump sum indemnity. Note creation was skipped");
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMT, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateLateInterestChargesNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypePMD = Beans.get(InvoiceNoteTypeRepository.class).findByCode("PMD");
    if (noteTypePMD == null) {
      logger.warn("Note type PMD doesn't exist");
      return;
    }

    String noteContent = company.getAccountConfig().getPenaltyRateNote();
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note PMD, late interest charges. Note creation was skipped");
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMD, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateLegalFormAndCapitalNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeREG = Beans.get(InvoiceNoteTypeRepository.class).findByCode("REG");
    if (noteTypeREG == null) {
      logger.warn("Note type REG doesn't exist");
      return;
    }

    String noteContent = company.getLegalFormAndCapital();
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note REG, legal form and capital. Note creation was skipped");
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeREG, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateSupplierNote(Invoice invoice) {
    InvoiceNoteType noteTypeSUR = Beans.get(InvoiceNoteTypeRepository.class).findByCode("SUR");
    if (noteTypeSUR == null) {
      logger.warn("Note type SUR doesn't exist");
      return;
    }

    String noteContent = invoice.getNote();
    if (StringUtils.isBlank(noteContent)) {
      logger.info(
          "No content to display for note SUR, supplier information. Note creation was skipped");
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeSUR, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateInvoiceCategoryNote(Invoice invoice) throws AxelorException {
    InvoiceNoteType noteTypeREG = Beans.get(InvoiceNoteTypeRepository.class).findByCode("REG");
    if (noteTypeREG == null) {
      logger.warn("Note type REG doesn't exist");
      return;
    }

    AccountConfig accountConfig =
        Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());
    if (!accountConfig.getDisplayItemsCategoriesOnPrinting()) {
      return;
    }

    String invoiceCategory = invoice.getInvoiceCategorySelect();
    if (Strings.isNullOrEmpty(invoiceCategory)) {
      logger.info("No invoice category defined. Note creation was skipped");
      return;
    }

    accountConfig.getStatementsForItemsCategoriesList().stream()
        .filter(s -> invoiceCategory.equals(s.getTypeList()))
        .findFirst()
        .map(InvoiceProductStatement::getStatement)
        .ifPresent(
            statement -> {
              InvoiceNote invoiceNote = createInvoiceNote(noteTypeREG, statement);
              invoice.addInvoiceNoteListItem(invoiceNote);
            });
  }
}
