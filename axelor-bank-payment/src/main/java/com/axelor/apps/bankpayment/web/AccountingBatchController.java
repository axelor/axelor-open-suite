package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.bankpayment.service.batch.AccountingBatchBankPaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountingBatchController {

    protected final AccountingBatchBankPaymentService accountingBatchService;
    protected final AccountingBatchRepository accountingBatchRepo;

    @Inject
    public AccountingBatchController(AccountingBatchBankPaymentService accountingBatchService,
            AccountingBatchRepository accountingBatchRepo) {
        this.accountingBatchService = accountingBatchService;
        this.accountingBatchRepo = accountingBatchRepo;
    }

    public void actionBankStatement(ActionRequest request, ActionResponse response) {
        AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
        accountingBatch = accountingBatchRepo.find(accountingBatch.getId());
        Batch batch = accountingBatchService.bankStatement(accountingBatch);
        response.setFlash(batch.getComments());
        response.setReload(true);
    }

}
