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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;

public class InvoiceNoteCreationHelper {
  protected InvoiceNoteCreationHelper() {
    throw new IllegalStateException("Utility class");
  }

  protected static boolean noteAlreadyExist(InvoiceNoteType noteType, Invoice invoice) {
    return invoice.getInvoiceNoteList().stream()
        .anyMatch(n -> n.getInvoiceNoteType().equals(noteType));
  }

  protected static InvoiceNoteType getOrCreateInvoiceNoteType(
      String noteType, String noteTypeName) {
    InvoiceNoteType invoiceNoteType =
        Beans.get(InvoiceNoteTypeRepository.class).findByCode(noteType);
    if (invoiceNoteType == null) {
      invoiceNoteType = new InvoiceNoteType(noteType, noteTypeName);
      Beans.get(InvoiceNoteTypeRepository.class).save(invoiceNoteType);
    }
    return invoiceNoteType;
  }

  protected static InvoiceNote createInvoiceNote(InvoiceNoteType noteType, String noteContent) {
    InvoiceNote invoiceNote = new InvoiceNote();
    invoiceNote.setInvoiceNoteType(noteType);
    invoiceNote.setNote(noteContent);
    return invoiceNote;
  }

  protected static void generateFinancialDiscountNote(Invoice invoice, Company company)
      throws AxelorException {
    InvoiceNoteType noteTypeAAB = getOrCreateInvoiceNoteType("AAB", I18n.get("Cash discount"));
    if (invoice.getFinancialDiscount() != null
        && !StringUtils.isBlank(invoice.getFinancialDiscount().getLegalNotice())) {
      String noteContent = invoice.getFinancialDiscount().getLegalNotice();
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAB, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
      return;
    }
    if (invoice.getPartner().getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_COMPANY
        && company.getAccountConfig().getDisplayNoFinancialDiscountAppliedOnPrinting()) {
      String noteContent = I18n.get("No discount will be granted for early payment.");
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAB, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
      return;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_NO_VALUE,
        AccountExceptionMessage.MISSING_CASH_DISCOUNT_MENTION,
        invoice.getInvoiceId());
  }

  protected static void generateGeneralInformationNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeAAI =
        getOrCreateInvoiceNoteType("AAI", I18n.get("General Information"));
    String noteContent = "";
    if (!StringUtils.isBlank(company.getAccountConfig().getTermsAndConditions())) {
      noteContent = company.getAccountConfig().getTermsAndConditions();
      if (!StringUtils.isBlank(company.getAccountConfig().getInvoiceClientBox())) {
        noteContent += "\n" + company.getAccountConfig().getInvoiceClientBox();
      }
    }
    if (!StringUtils.isBlank(noteContent)) {
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeAAI, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
    }
  }

  protected static void generateSellerLegalInformationNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeABL =
        getOrCreateInvoiceNoteType("ABL", I18n.get("Seller legal information"));
    if (!StringUtils.isBlank(company.getLegalInformation())) {
      String noteContent = company.getLegalInformation();
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeABL, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
    }
  }

  protected static void generateLumpSumIndemnityNote(Invoice invoice, Company company)
      throws AxelorException {
    InvoiceNoteType noteTypePMT =
        getOrCreateInvoiceNoteType("PMT", I18n.get("Lump sum indemnity for recovery costs"));
    String noteContent = company.getAccountConfig().getSaleInvoiceLegalNote();
    if (StringUtils.isBlank(noteContent)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, AccountExceptionMessage.MISSING_LEGAL_NOTE);
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMT, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateLateInterestChargesNote(Invoice invoice, Company company)
      throws AxelorException {
    InvoiceNoteType noteTypePMD =
        getOrCreateInvoiceNoteType("PMD", I18n.get("Late interest charges"));
    String noteContent = company.getAccountConfig().getPenaltyRateNote();
    if (StringUtils.isBlank(noteContent)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, AccountExceptionMessage.MISSING_PENALTY_RATE_NOTE);
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMD, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateLegalFormAndCapitalNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeREG = getOrCreateInvoiceNoteType("REG", I18n.get("Legal informations"));

    String noteContent = company.getLegalFormAndCapital();
    if (StringUtils.isBlank(noteContent)) {
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeREG, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateBankDetailsNote(Invoice invoice) throws AxelorException {
    BankDetails bankDetails = invoice.getCompanyBankDetails();
    if (bankDetails == null || StringUtils.isBlank(bankDetails.getSpecificNoteOnInvoice())) {
      return;
    }
    if (bankDetails.getInvoiceNoteType() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          AccountExceptionMessage.MISSING_BANK_DETAILS_NOTE_TYPE,
          bankDetails,
          bankDetails.getSpecificNoteOnInvoice());
    }
    InvoiceNote invoiceNote =
        createInvoiceNote(bankDetails.getInvoiceNoteType(), bankDetails.getSpecificNoteOnInvoice());
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateSupplierNote(Invoice invoice) {
    InvoiceNoteType noteTypeSUR = getOrCreateInvoiceNoteType("SUR", I18n.get("Supplier note"));

    String noteContent = invoice.getNote();
    if (StringUtils.isBlank(noteContent)) {
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeSUR, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateInvoiceCategoryNote(Invoice invoice) throws AxelorException {
    InvoiceNoteType noteTypeREG = getOrCreateInvoiceNoteType("REG", I18n.get("Invoice category"));

    AccountConfig accountConfig =
        Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());
    if (!accountConfig.getDisplayItemsCategoriesOnPrinting()) {
      return;
    }

    String invoiceCategory = invoice.getInvoiceCategorySelect();
    if (Strings.isNullOrEmpty(invoiceCategory)) {
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
