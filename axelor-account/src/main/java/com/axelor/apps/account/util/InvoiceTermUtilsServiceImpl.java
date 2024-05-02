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
package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.util.Optional;

public class InvoiceTermUtilsServiceImpl implements InvoiceTermUtilsService {

  protected PfpService pfpService;
  protected JournalService journalService;

  @Inject
  public InvoiceTermUtilsServiceImpl(PfpService pfpService, JournalService journalService) {
    this.pfpService = pfpService;
    this.journalService = journalService;
  }

  @Override
  public void setPfpStatus(InvoiceTerm invoiceTerm, Move move) throws AxelorException {
    Company company;
    boolean isSupplierPurchase, isSupplierRefund;

    if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();

      company = invoice.getCompany();
      isSupplierPurchase =
          invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
      isSupplierRefund =
          invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
    } else {
      if (move == null
          && invoiceTerm.getMoveLine() != null
          && invoiceTerm.getMoveLine().getMove() != null) {
        move = invoiceTerm.getMoveLine().getMove();
      }

      if (move == null) {
        invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_NO_PFP);
        return;
      }

      company = move.getCompany();
      isSupplierPurchase =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE;
      isSupplierRefund =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE;
    }

    if (pfpService.isManagePassedForPayment(company)
        && (isSupplierPurchase || (isSupplierRefund && pfpService.isManagePFPInRefund(company)))) {
      invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    } else {
      invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_NO_PFP);
    }
  }

  @Override
  public void setParentFields(
      InvoiceTerm invoiceTerm, Move move, MoveLine moveLine, Invoice invoice) {
    if (invoice != null) {
      invoiceTerm.setCompany(invoice.getCompany());
      invoiceTerm.setPartner(invoice.getPartner());
      invoiceTerm.setCurrency(invoice.getCurrency());

      this.setThirdPartyPayerPartner(invoiceTerm);

      if (StringUtils.isEmpty(invoice.getSupplierInvoiceNb())) {
        invoiceTerm.setOrigin(invoice.getInvoiceId());
      } else {
        invoiceTerm.setOrigin(invoice.getSupplierInvoiceNb());
      }

      if (invoice.getOriginDate() != null) {
        invoiceTerm.setOriginDate(invoice.getOriginDate());
      }
    } else if (moveLine != null) {
      invoiceTerm.setOrigin(moveLine.getOrigin());

      if (moveLine.getPartner() != null) {
        invoiceTerm.setPartner(moveLine.getPartner());
      }

      if (move != null) {
        invoiceTerm.setCompany(move.getCompany());
        invoiceTerm.setCurrency(move.getCurrency());

        if (invoiceTerm.getPartner() == null) {
          invoiceTerm.setPartner(move.getPartner());
        }

        if (journalService.isThirdPartyPayerOk(move.getJournal())) {
          this.setThirdPartyPayerPartner(invoiceTerm);
        }
      }
    }

    if (moveLine != null && move != null && invoiceTerm.getOriginDate() == null) {
      invoiceTerm.setOriginDate(move.getOriginDate());
    }
  }

  protected void setThirdPartyPayerPartner(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0) {
      if (invoiceTerm.getInvoice() != null) {
        invoiceTerm.setThirdPartyPayerPartner(invoiceTerm.getInvoice().getThirdPartyPayerPartner());
      } else {
        Partner thirdPartyPayerPartner =
            Optional.of(invoiceTerm)
                .map(InvoiceTerm::getMoveLine)
                .map(MoveLine::getMove)
                .map(Move::getThirdPartyPayerPartner)
                .orElse(null);

        invoiceTerm.setThirdPartyPayerPartner(thirdPartyPayerPartner);
      }
    }
  }
}
