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
import com.axelor.apps.account.db.repo.InvoiceNoteRepository;
import com.axelor.apps.account.db.repo.InvoiceNoteTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.SelectHelper;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class InvoiceNoteCreationHelper {
  private InvoiceNoteCreationHelper() {
    throw new IllegalStateException("Utility class");
  }

  protected static boolean noteAlreadyExist(InvoiceNoteType noteType, Invoice invoice) {
    return invoice.getInvoiceNoteList().stream()
        .anyMatch(n -> n.getInvoiceNoteType().equals(noteType));
  }

  @Transactional(rollbackOn = Exception.class)
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

  @Transactional(rollbackOn = Exception.class)
  protected static InvoiceNote createInvoiceNote(InvoiceNoteType noteType, String noteContent) {
    InvoiceNote invoiceNote = new InvoiceNote();
    invoiceNote.setInvoiceNoteType(noteType);
    invoiceNote.setNote(noteContent);
    return Beans.get(InvoiceNoteRepository.class).save(invoiceNote);
  }

  protected static void generateAABNote(Invoice invoice, Company company) throws AxelorException {
    InvoiceNoteType noteTypeAAB = getOrCreateInvoiceNoteType("AAB", I18n.get("Cash discount"));
    if (noteAlreadyExist(noteTypeAAB, invoice)) {
      return;
    }
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

  protected static void generateAAINote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeAAI =
        getOrCreateInvoiceNoteType("AAI", I18n.get("General Information"));
    if (noteAlreadyExist(noteTypeAAI, invoice)) {
      return;
    }
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

  protected static void generateABLNote(Invoice invoice, Company company) {
    InvoiceNoteType noteTypeABL =
        getOrCreateInvoiceNoteType("ABL", I18n.get("Seller legal information"));
    if (noteAlreadyExist(noteTypeABL, invoice)) {
      return;
    }
    if (!StringUtils.isBlank(company.getLegalInformation())) {
      String noteContent = company.getLegalInformation();
      InvoiceNote invoiceNote = createInvoiceNote(noteTypeABL, noteContent);
      invoice.addInvoiceNoteListItem(invoiceNote);
    }
  }

  protected static String getValueOfInvoiceScope(Integer scopeSelect) throws AxelorException {
    Optional<String> scope =
        SelectHelper.getOptionalTitleFromIntegerValue(
            "e.invoicing.invoice.electronic.data.scope.select", scopeSelect);
    if (scope.isPresent()) {
      return scope.get();
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        AccountExceptionMessage.UNKNOWN_SCOPE_VALUE,
        scopeSelect);
  }

  protected static void generatePMTNote(Invoice invoice, Company company) throws AxelorException {
    InvoiceNoteType noteTypePMT =
        getOrCreateInvoiceNoteType("PMT", I18n.get("Lump sum indemnity for recovery costs"));
    if (noteAlreadyExist(noteTypePMT, invoice)) {
      return;
    }
    String noteContent = company.getAccountConfig().getSaleInvoiceLegalNote();
    if (StringUtils.isBlank(noteContent)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, AccountExceptionMessage.MISSING_LEGAL_NOTE);
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMT, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generatePMDNote(Invoice invoice, Company company) throws AxelorException {
    InvoiceNoteType noteTypePMD =
        getOrCreateInvoiceNoteType("PMT", I18n.get("Late interest charges"));
    if (noteAlreadyExist(noteTypePMD, invoice)) {
      return;
    }
    String noteContent = company.getAccountConfig().getPenaltyRateNote();
    if (StringUtils.isBlank(noteContent)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, AccountExceptionMessage.MISSING_PENALTY_RATE_NOTE);
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypePMD, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateREGNote(Invoice invoice, Company company) throws AxelorException {
    InvoiceNoteType noteTypeREG = getOrCreateInvoiceNoteType("REG", I18n.get("Legal informations"));
    if (noteAlreadyExist(noteTypeREG, invoice)) {
      return;
    }
    String noteContent = company.getLegalFormAndCapital();
    if (StringUtils.isBlank(noteContent)) {
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeREG, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateSURNote(Invoice invoice) throws AxelorException {
    InvoiceNoteType noteTypeSUR = getOrCreateInvoiceNoteType("SUR", I18n.get("Supplier note"));
    if (noteAlreadyExist(noteTypeSUR, invoice)) {
      return;
    }
    String noteContent = invoice.getNote();
    if (StringUtils.isBlank(noteContent)) {
      return;
    }
    InvoiceNote invoiceNote = createInvoiceNote(noteTypeSUR, noteContent);
    invoice.addInvoiceNoteListItem(invoiceNote);
  }

  protected static void generateFinancialDiscountNote(Invoice invoice) throws AxelorException {
    generateAABNote(invoice, invoice.getCompany());
  }

  protected static void generateREGNote(Invoice invoice) throws AxelorException {
    InvoiceNoteType noteTypeREG = getOrCreateInvoiceNoteType("REG", I18n.get("Invoice category"));

    if (noteAlreadyExist(noteTypeREG, invoice)) {
      return;
    }

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
              InvoiceNote invoiceNote = new InvoiceNote();
              invoiceNote.setInvoiceNoteType(noteTypeREG);
              invoiceNote.setNote(statement);
              invoice.addInvoiceNoteListItem(invoiceNote);
            });
  }

  protected static void generateInvoiceCategoryNote(Invoice invoice) throws AxelorException {
    generateREGNote(invoice);
  }
}
