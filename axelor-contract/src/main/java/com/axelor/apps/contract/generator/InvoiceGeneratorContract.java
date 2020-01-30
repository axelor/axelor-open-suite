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
package com.axelor.apps.contract.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

public class InvoiceGeneratorContract extends InvoiceGenerator {

  protected Contract contract;
  private AppBaseService appBaseService;

  public InvoiceGeneratorContract(Contract contract) throws AxelorException {
    super(
        contract.getTargetTypeSelect() == ContractRepository.CUSTOMER_CONTRACT
            ? InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
            : InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
        contract.getCompany(),
        contract.getPartner(),
        null,
        null,
        contract.getContractId(),
        null,
        null,
        null);
    this.contract = contract;
    this.currency = contract.getCurrency();
    this.paymentCondition = contract.getCurrentContractVersion().getPaymentCondition();
    this.paymentMode = contract.getCurrentContractVersion().getPaymentMode();
    this.appBaseService = Beans.get(AppBaseService.class);
  }

  @Override
  protected Invoice createInvoiceHeader() throws AxelorException {
    Invoice invoice = super.createInvoiceHeader();

    ContractVersion version = contract.getCurrentContractVersion();
    if (contract.getIsInvoicingManagement() && version.getIsPeriodicInvoicing()) {
      invoice.setOperationSubTypeSelect(
          InvoiceRepository.OPERATION_SUB_TYPE_CONTRACT_PERIODIC_INVOICE);
      invoice.setSubscriptionFromDate(contract.getInvoicePeriodStartDate());
      invoice.setSubscriptionToDate(contract.getInvoicePeriodEndDate());
    } else if (contract.getEndDate() == null
        || contract.getEndDate().isAfter(appBaseService.getTodayDate())) {
      invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_CONTRACT_INVOICE);
    } else {
      invoice.setOperationSubTypeSelect(
          InvoiceRepository.OPERATION_SUB_TYPE_CONTRACT_CLOSING_INVOICE);
    }

    invoice.setContract(contract);
    if (contract.getInvoicingDate() != null) {
      invoice.setInvoiceDate(contract.getInvoicingDate());
    } else {
      invoice.setInvoiceDate(appBaseService.getTodayDate());
    }

    return invoice;
  }

  @Override
  public Invoice generate() throws AxelorException {
    return createInvoiceHeader();
  }
}
