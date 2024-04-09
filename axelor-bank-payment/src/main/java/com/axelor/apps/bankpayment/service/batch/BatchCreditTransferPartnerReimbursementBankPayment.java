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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.ReimbursementRepository;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.batch.BatchCreditTransferPartnerReimbursement;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCreditTransferPartnerReimbursementBankPayment
    extends BatchCreditTransferPartnerReimbursement {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ReimbursementRepository reimbursementRepository;
  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderLineService bankOrderLineService;
  protected BankOrderRepository bankOrderRepository;
  protected AccountingBatchRepository accountingBatchRepository;
  protected BankOrderValidationService bankOrderValidationService;

  @Inject
  public BatchCreditTransferPartnerReimbursementBankPayment(
      PartnerRepository partnerRepository,
      PartnerService partnerService,
      ReimbursementExportService reimbursementExportService,
      ReimbursementRepository reimbursementRepository,
      BankOrderCreateService bankOrderCreateService,
      BankOrderValidationService bankOrderValidationService,
      BankOrderLineService bankOrderLineService,
      BankOrderRepository bankOrderRepository,
      AccountingBatchRepository accountingBatchRepository) {
    super(partnerRepository, partnerService, reimbursementExportService);
    this.reimbursementRepository = reimbursementRepository;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderValidationService = bankOrderValidationService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderRepository = bankOrderRepository;
    this.accountingBatchRepository = accountingBatchRepository;
  }

  @Override
  protected void process() {
    super.process();
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (!accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    // Fetch all reimbursements that are validated for the specified company.
    Query<Reimbursement> query =
        reimbursementRepository
            .all()
            .filter("self.statusSelect = :statusSelect AND self.company = :company");
    query.bind("statusSelect", ReimbursementRepository.STATUS_VALIDATED);
    query.bind("company", accountingBatch.getCompany());
    List<Reimbursement> reimbursementList = query.fetch();

    if (reimbursementList.isEmpty()) {
      return;
    }

    accountingBatch = accountingBatchRepository.find(accountingBatch.getId());

    try {
      createBankOrder(accountingBatch, reimbursementList);
    } catch (Exception ex) {
      TraceBackService.trace(ex);
      logger.error(ex.getLocalizedMessage());
    }
  }

  /**
   * Create a bank order for the specified list of reimbursements.
   *
   * @param accountingBatch
   * @param reimbursementList
   * @return
   * @throws AxelorException
   * @throws DatatypeConfigurationException
   * @throws IOException
   * @throws JAXBException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected BankOrder createBankOrder(
      AccountingBatch accountingBatch, List<Reimbursement> reimbursementList)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    LocalDate bankOrderDate =
        accountingBatch.getDueDate() != null
            ? accountingBatch.getDueDate()
            : appBaseService.getTodayDate(accountingBatch.getCompany());
    BankOrder bankOrder =
        bankOrderCreateService.createBankOrder(
            accountingBatch.getPaymentMode(),
            BankOrderRepository.PARTNER_TYPE_CUSTOMER,
            bankOrderDate,
            accountingBatch.getCompany(),
            accountingBatch.getBankDetails(),
            accountingBatch.getCompany().getCurrency(),
            null,
            null,
            BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            BankOrderRepository.FUNCTIONAL_ORIGIN_BATCH_PAYBACK,
            PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);

    for (Reimbursement reimbursement : reimbursementList) {
      BankOrderLine bankOrderLine =
          bankOrderLineService.createBankOrderLine(
              accountingBatch.getPaymentMode().getBankOrderFileFormat(),
              null,
              reimbursement.getPartner(),
              reimbursement.getBankDetails(),
              reimbursement.getAmountToReimburse(),
              accountingBatch.getCompany().getCurrency(),
              bankOrderDate,
              reimbursement.getRef(),
              reimbursement.getDescription(),
              reimbursement);
      bankOrder.addBankOrderLineListItem(bankOrderLine);
      reimbursementExportService.reimburse(reimbursement, accountingBatch.getCompany());
    }

    bankOrder = bankOrderRepository.save(bankOrder);
    bankOrderValidationService.confirm(bankOrder);
    return bankOrder;
  }
}
