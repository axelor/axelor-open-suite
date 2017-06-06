package com.axelor.apps.account.service.batch;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.ReimbursementExportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchCreditTransferPartnerCreditBalanceReimbursement extends BatchStrategy {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected PartnerRepository partnerRepo;
	protected PartnerService partnerService;
	protected ReimbursementExportService reimbursementExportService;

	@Inject
	public BatchCreditTransferPartnerCreditBalanceReimbursement(PartnerRepository partnerRepo,
			PartnerService partnerService, ReimbursementExportService reimbursementExportService) {
		this.partnerRepo = partnerRepo;
		this.partnerService = partnerService;
		this.reimbursementExportService = reimbursementExportService;
	}

	@Override
	protected void process() {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		TypedQuery<Partner> partnerQuery = JPA.em().createQuery(
				"SELECT self FROM Partner self JOIN self.accountingSituationList accountingSituation "
						+ "WHERE accountingSituation.company = :company AND accountingSituation.balanceCustAccount < 0",
				Partner.class);
		partnerQuery.setParameter("company", accountingBatch.getCompany());
		List<Partner> partnerList = partnerQuery.getResultList();

		for (Partner partner : partnerList) {
			try {
				partner = partnerRepo.find(partner.getId());
				createReimbursement(partner, accountingBatch.getCompany());
				incrementDone();
			} catch (Exception ex) {
				incrementAnomaly();
				TraceBackService.trace(ex);
				ex.printStackTrace();
				log.error(String.format(
						"Credit transfer batch for partner credit balance reimbursement: anomaly for partner %s",
						partner.getName()));
			}
			JPA.clear();
		}
	}

	@Override
	protected void stop() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_REPORT_TITLE));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_PARTNER_DONE_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_PARTNER_DONE_PLURAL, batch.getDone()),
				batch.getDone()));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_ANOMALY_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_ANOMALY_PLURAL, batch.getAnomaly()),
				batch.getAnomaly()));
		addComment(sb.toString());
		super.stop();
	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	protected Reimbursement createReimbursement(Partner partner, Company company) throws AxelorException {
		List<MoveLine> moveLineList = (List<MoveLine>) moveLineRepo.all()
				.filter("self.account.reconcileOk = true AND self.fromSchedulePaymentOk = false "
						+ "AND self.move.statusSelect = ?1 AND self.amountRemaining > 0 AND self.credit > 0 AND self.partner = ?2 AND self.company = ?3 AND "
						+ "self.reimbursementStatusSelect = ?4 ", MoveRepository.STATUS_VALIDATED, partner, company,
						MoveLineRepository.REIMBURSEMENT_STATUS_NULL)
				.fetch();

		Reimbursement reimbursement = reimbursementExportService.runCreateReimbursement(moveLineList, company, partner);
		return reimbursement;
	}

}
