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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return AccountingBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {

    Batch batch;
    AccountingBatch accountingBatch = (AccountingBatch) batchModel;

    switch (accountingBatch.getActionSelect()) {
      case AccountingBatchRepository.ACTION_REIMBURSEMENT:
        if (accountingBatch.getReimbursementTypeSelect()
            == AccountingBatchRepository.REIMBURSEMENT_TYPE_EXPORT) {
          batch = reimbursementExport(accountingBatch);
        } else if (accountingBatch.getReimbursementTypeSelect()
            == AccountingBatchRepository.REIMBURSEMENT_TYPE_IMPORT) {
          batch = reimbursementImport(accountingBatch);
        }
        batch = null;
        break;
      case AccountingBatchRepository.ACTION_DEBT_RECOVERY:
        batch = debtRecovery(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_DOUBTFUL_CUSTOMER:
        batch = doubtfulCustomer(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_ACCOUNT_CUSTOMER:
        batch = accountCustomer(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_MOVE_LINE_EXPORT:
        batch = moveLineExport(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_CREDIT_TRANSFER:
        batch = creditTransfer(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_REALIZE_FIXED_ASSET_LINES:
        batch = realizeFixedAssetLines(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_LATE_PAYMENT_CUSTOMER_BLOCKING:
        batch = blockCustomersWithLatePayments(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_CLOSE_OR_OPEN_THE_ANNUAL_ACCOUNTS:
        batch = closeAnnualAccounts(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_MOVES_CONSISTENCY_CONTROL:
        batch = controlMovesConsistency(accountingBatch);
        break;
      case AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF:
        batch = accountingCutOff(accountingBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            accountingBatch.getActionSelect(),
            accountingBatch.getCode());
    }

    return batch;
  }

  public Batch debtRecovery(AccountingBatch accountingBatch) {

    return Beans.get(BatchDebtRecovery.class).run(accountingBatch);
  }

  public Batch doubtfulCustomer(AccountingBatch accountingBatch) {

    return Beans.get(BatchDoubtfulCustomer.class).run(accountingBatch);
  }

  public Batch reimbursementExport(AccountingBatch accountingBatch) {

    return Beans.get(BatchReimbursementExport.class).run(accountingBatch);
  }

  public Batch reimbursementImport(AccountingBatch accountingBatch) {

    return Beans.get(BatchReimbursementImport.class).run(accountingBatch);
  }

  public Batch accountCustomer(AccountingBatch accountingBatch) {

    return Beans.get(BatchAccountCustomer.class).run(accountingBatch);
  }

  public Batch moveLineExport(AccountingBatch accountingBatch) {

    return Beans.get(BatchMoveLineExport.class).run(accountingBatch);
  }

  public Batch creditTransfer(AccountingBatch accountingBatch) {
    Class<? extends BatchStrategy> batchStrategyClass;

    switch (accountingBatch.getCreditTransferTypeSelect()) {
      case AccountingBatchRepository.CREDIT_TRANSFER_EXPENSE_PAYMENT:
        batchStrategyClass = BatchCreditTransferExpensePayment.class;
        break;
      case AccountingBatchRepository.CREDIT_TRANSFER_SUPPLIER_PAYMENT:
        batchStrategyClass = BatchCreditTransferSupplierPayment.class;
        break;
      case AccountingBatchRepository.CREDIT_TRANSFER_CUSTOMER_REIMBURSEMENT:
        switch (accountingBatch.getCustomerReimbursementTypeSelect()) {
          case AccountingBatchRepository.CUSTOMER_REIMBURSEMENT_CUSTOMER_REFUND:
            batchStrategyClass = BatchCreditTransferCustomerRefund.class;
            break;
          case AccountingBatchRepository.CUSTOMER_REIMBURSEMENT_PARTNER_CREDIT_BALANCE:
            batchStrategyClass = BatchCreditTransferPartnerReimbursement.class;
            break;
          default:
            throw new IllegalArgumentException("Unknown customer reimbursement type");
        }
        break;
      default:
        throw new IllegalArgumentException(
            String.format(
                "Unknown credit transfer type: %d", accountingBatch.getCreditTransferTypeSelect()));
    }

    return Beans.get(batchStrategyClass).run(accountingBatch);
  }

  public Batch directDebit(AccountingBatch accountingBatch) {
    throw new UnsupportedOperationException(
        I18n.get("This batch requires the bank payment module."));
  }

  public Batch billOfExchange(AccountingBatch accountingBatch) {
    throw new UnsupportedOperationException(
        I18n.get("This batch requires the bank payment module."));
  }

  public Batch realizeFixedAssetLines(AccountingBatch accountingBatch) {

    return Beans.get(BatchRealizeFixedAssetLine.class).run(accountingBatch);
  }

  public Batch closeAnnualAccounts(AccountingBatch accountingBatch) {

    return Beans.get(BatchCloseAnnualAccounts.class).run(accountingBatch);
  }

  public Batch blockCustomersWithLatePayments(AccountingBatch accountingBatch) {

    return Beans.get(BatchBlockCustomersWithLatePayments.class).run(accountingBatch);
  }

  public Batch controlMovesConsistency(AccountingBatch accountingBatch) {
    return Beans.get(BatchControlMovesConsistency.class).run(accountingBatch);
  }

  public Batch accountingCutOff(AccountingBatch accountingBatch) {
    return Beans.get(BatchAccountingCutOff.class).run(accountingBatch);
  }

  @Transactional
  public AccountingBatch createNewAccountingBatch(int action, Company company) {
    if (company != null) {
      AccountingBatch accountingBatch = new AccountingBatch();
      accountingBatch.setActionSelect(action);
      accountingBatch.setCompany(company);
      Beans.get(AccountingBatchRepository.class).save(accountingBatch);
      return accountingBatch;
    }
    return null;
  }

  public boolean checkIfAnomalyInBatch(AccountingBatch accountingBatch) {
    if (!CollectionUtils.isEmpty(accountingBatch.getBatchList())) {
      List<Batch> batchList =
          new ArrayList<>(accountingBatch.getBatchList())
              .stream()
                  .sorted(Comparator.comparing(Batch::getStartDate))
                  .collect(Collectors.toList());
      if (batchList.get(batchList.size() - 1).getAnomaly() > 0) {
        return true;
      }
    }
    return false;
  }
}
