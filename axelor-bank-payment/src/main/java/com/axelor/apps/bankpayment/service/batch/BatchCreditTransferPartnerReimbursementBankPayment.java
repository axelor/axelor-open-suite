package com.axelor.apps.bankpayment.service.batch;

import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.ReimbursementRepository;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.account.service.batch.BatchCreditTransferPartnerReimbursement;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchCreditTransferPartnerReimbursementBankPayment extends BatchCreditTransferPartnerReimbursement {
	protected GeneralService generalService;
	protected ReimbursementRepository reimbursementRepo;
	protected BankOrderCreateService bankOrderCreateService;
	protected BankOrderLineService bankOrderLineService;
	protected BankOrderRepository bankOrderRepo;

	@Inject
	public BatchCreditTransferPartnerReimbursementBankPayment(PartnerRepository partnerRepo,
			PartnerService partnerService, ReimbursementExportService reimbursementExportService,
			GeneralService generalService, ReimbursementRepository reimbursementRepo,
			BankOrderCreateService bankOrderCreateService, BankOrderLineService bankOrderLineService,
			BankOrderRepository bankOrderRepo) {
		super(partnerRepo, partnerService, reimbursementExportService);
		this.generalService = generalService;
		this.reimbursementRepo = reimbursementRepo;
		this.bankOrderCreateService = bankOrderCreateService;
		this.bankOrderLineService = bankOrderLineService;
		this.bankOrderRepo = bankOrderRepo;
	}

	@Override
	protected void process() {
		super.process();
		AccountingBatch accountingBatch = batch.getAccountingBatch();

		if (!accountingBatch.getPaymentMode().getGenerateBankOrder()) {
			return;
		}

		Query<Reimbursement> query = reimbursementRepo.all()
				.filter("self.statusSelect = :statusSelect AND self.company = :company");
		query.bind("statusSelect", ReimbursementRepository.STATUS_VALIDATED);
		query.bind("company", accountingBatch.getCompany());
		List<Reimbursement> reimbursementList = query.fetch();

		if (reimbursementList.isEmpty()) {
			return;
		}

		accountingBatch = Beans.get(AccountingBatchRepository.class).find(accountingBatch.getId());

		try {
			createBankOrder(accountingBatch, reimbursementList);
		} catch (Exception ex) {
			TraceBackService.trace(ex);
			ex.printStackTrace();
			log.error("Credit transfer batch for partner credit balance reimbursement: postProcess");
		}

	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	protected BankOrder createBankOrder(AccountingBatch accountingBatch, List<Reimbursement> reimbursementList)
			throws AxelorException {
		LocalDate bankOrderDate = accountingBatch.getDueDate();
		BankOrder bankOrder = bankOrderCreateService.createBankOrder(accountingBatch.getPaymentMode(),
				BankOrderRepository.PARTNER_TYPE_CUSTOMER, bankOrderDate, accountingBatch.getCompany(),
				accountingBatch.getBankDetails(), accountingBatch.getCompany().getCurrency(), null, null);

		for (Reimbursement reimbursement : reimbursementList) {
			BankOrderLine bankOrderLine = bankOrderLineService.createBankOrderLine(
					accountingBatch.getPaymentMode().getBankOrderFileFormat(), null, reimbursement.getPartner(),
					reimbursement.getBankDetails(), reimbursement.getAmountToReimburse(),
					accountingBatch.getCompany().getCurrency(), bankOrderDate, reimbursement.getRef(),
					reimbursement.getDescription());
			bankOrder.addBankOrderLineListItem(bankOrderLine);
		}

		return bankOrderRepo.save(bankOrder);
	}

}
