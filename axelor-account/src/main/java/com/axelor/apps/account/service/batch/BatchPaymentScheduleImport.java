/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingService;
import com.axelor.apps.account.service.PaymentScheduleImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class BatchPaymentScheduleImport extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected boolean stop = false;

	protected BigDecimal totalAmount = BigDecimal.ZERO;

	protected String updateCustomerAccountLog = "";

	protected InterbankCodeLineRepository interbankCodeLineRepo;
	protected AccountRepository accountRepo;
	protected InvoiceRepository invoiceRepo;
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;

	@Inject
	public BatchPaymentScheduleImport(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer,
			InterbankCodeLineRepository interbankCodeLineRepo, AccountRepository accountRepo, InvoiceRepository invoiceRepo, PaymentScheduleLineRepository paymentScheduleLineRepo) {

		super(paymentScheduleImportService, rejectImportService, batchAccountCustomer);
		
		this.interbankCodeLineRepo = interbankCodeLineRepo;
		this.accountRepo = accountRepo;
		this.invoiceRepo = invoiceRepo;
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		
		AccountingService.setUpdateCustomerAccount(false);

	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {

		super.start();

		Company company = batch.getAccountingBatch().getCompany();

		try{
			paymentScheduleImportService.checkCompanyFields(company);
		} catch (AxelorException e) {
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			incrementAnomaly();
			stop = true;
		}

		checkPoint();

	}

	@Override
	protected void process() {

		if(!stop)  {

			this.runImportProcess(batch.getAccountingBatch().getCompany());

			updateCustomerAccountLog += batchAccountCustomer.updateAccountingSituationMarked(null);
		}

	}


	public void runImportProcess(Company company)  {

		Map<List<String[]>,String> data = null;

		try {
			company = companyRepo.find(company.getId());

			AccountConfig accountConfig = company.getAccountConfig();

			data = rejectImportService.getCFONBFileByLot(accountConfig.getRejectImportPathAndFileName(), accountConfig.getTempImportPathAndFileName(), company, 1);

		} catch (AxelorException e) {

			TraceBackService.trace(new AxelorException(String.format(I18n.get("Batch")+" %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			stop = true;

		} catch (Exception e) {

			TraceBackService.trace(new Exception(String.format(I18n.get("Batch")+" %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			stop = true;

			log.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());

		}

		int i=0;

		if(!stop)  {

			for(List<String[]> rejectList : data.keySet())  {

				LocalDate rejectDate = rejectImportService.createRejectDate(data.get(rejectList));

				paymentScheduleImportService.initialiseCollection();

				for(String[] reject : rejectList)  {
					try {

						String refDebitReject = reject[1].replaceAll(" ", "_");
						BigDecimal amountReject = new BigDecimal(reject[2]);
						InterbankCodeLine causeReject = rejectImportService.getInterbankCodeLine(reject[3], 1);

						List<PaymentScheduleLine> paymentScheduleLineRejectedList = paymentScheduleImportService.importRejectPaymentScheduleLine(rejectDate, refDebitReject, amountReject, interbankCodeLineRepo.find(causeReject.getId()), companyRepo.find(company.getId()));

						List<Invoice> invoiceRejectedList = paymentScheduleImportService.importRejectInvoice(rejectDate, refDebitReject, amountReject, interbankCodeLineRepo.find(causeReject.getId()), companyRepo.find(company.getId()));

						/***  Aucun échéancier ou facture trouvé(e) pour le numéro de prélèvement  ***/
						if((invoiceRejectedList == null || invoiceRejectedList.isEmpty()) && (paymentScheduleLineRejectedList == null || paymentScheduleLineRejectedList.isEmpty()))  {
							throw new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_8),
									GeneralServiceImpl.EXCEPTION,refDebitReject), IException.NO_VALUE);
						}
						else  {
							i++;
						}

					} catch (AxelorException e) {

						TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_9), reject[1]), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

						incrementAnomaly();

					} catch (Exception e) {

						TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_9), reject[1]), e), IException.DIRECT_DEBIT, batch.getId());

						incrementAnomaly();

						log.error("Bug(Anomalie) généré(e) pour l'import du rejet {}", reject[1]);

					} finally {

						if (i % 10 == 0) { JPA.clear(); }

					}
				}

				this.createRejectMove(companyRepo.find(company.getId()),
						paymentScheduleImportService.getPaymentScheduleLineMajorAccountList(),
						paymentScheduleImportService.getInvoiceList(), rejectDate);
			}
		}
	}


	/**
	 * Fonction permettant de créer l'écriture des rejets pour l'ensembles des rejets (echéance de paiement,
	 * mensu masse, mensu grand compte et facture) d'une société
	 *
	 * @param company
	 * 				Une société
	 * @param pslList
	 * @param pslListGC
	 * @param pslListPayment
	 * @param pslListNewPayment
	 * @param pslListMonthlyPaymentAfterVentilateInvoice
	 * @param invoiceList
	 * @param invoiceRejectReasonList
	 * @return
	 * 				L'écriture des rejets
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createRejectMove(Company company, List<PaymentScheduleLine> pslListGC, 	List<Invoice> invoiceList, LocalDate rejectDate)  {

		// Création de l'écriture d'extourne par société
		Move move = this.createRejectMove(company, rejectDate);

		if(!stop)  {
			int ref = 1;  // Initialisation du compteur d'échéances

			/*** Echéancier de Lissage de paiement en PRLVT ***/
			if(pslListGC != null && pslListGC.size()!=0 )  {

				log.debug("Création des écritures de rejets : Echéancier de lissage de paiement");
				ref = this.createMajorAccountRejectMoveLines(pslListGC, companyRepo.find(company.getId()),
						companyRepo.find(company.getId()).getAccountConfig().getCustomerAccount(), moveRepo.find(move.getId()), ref);

			}

			/*** Facture en PRLVT ***/
			if(invoiceList != null && invoiceList.size()!=0)  {

				log.debug("Création des écritures de rejets : Facture");
				ref = this.createInvoiceRejectMoveLines(invoiceList, companyRepo.find(company.getId()),
						companyRepo.find(company.getId()).getAccountConfig().getCustomerAccount(), moveRepo.find(move.getId()), ref);
			}


			this.validateRejectMove(companyRepo.find(company.getId()), moveRepo.find(move.getId()), ref, rejectDate);

		}

		return move;
	}


	public Move createRejectMove(Company company, LocalDate date)  {
		Move move = null;
		try {
			move = paymentScheduleImportService.createRejectMove(company, date);
		} catch (AxelorException e) {

			TraceBackService.trace(new AxelorException(String.format(I18n.get("Batch")+" %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			stop = true;

		} catch (Exception e) {

			TraceBackService.trace(new Exception(String.format(I18n.get("Batch")+" %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			stop = true;

			log.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());

		}
		return move;
	}


	public void validateRejectMove(Company company, Move move, int ref, LocalDate rejectDate)  {
		try {

			if(ref > 1)  {

				MoveLine oppositeMoveLine = paymentScheduleImportService.createRejectOppositeMoveLine(company, moveRepo.find(move.getId()), ref, rejectDate);

				paymentScheduleImportService.validateMove(moveRepo.find(move.getId()));

				this.totalAmount = moveLineRepo.find(oppositeMoveLine.getId()).getCredit();
			}
			else {
				paymentScheduleImportService.deleteMove(moveRepo.find(move.getId()));
			}
		} catch (AxelorException e) {

			TraceBackService.trace(new AxelorException(String.format(I18n.get("Batch")+" %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

		} catch (Exception e) {

			TraceBackService.trace(new Exception(String.format(I18n.get("Batch")+" %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());

			incrementAnomaly();

			log.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());

		}
	}


	/**
	 *
	 * @param pslListGC
	 * 				Une liste de ligne d'échéancier de Mensu grand compte
	 * @param company
	 * 				Une société
	 * @param customerAccount
	 * 				Un compte client
	 * @param move
	 * 				L'écriture de rejet
	 * @param ref
	 * 				Le numéro de ligne d'écriture
	 * @return
	 * 				Le numéro de ligne d'écriture incrémenté
	 * @throws AxelorException
	 */
	public int createMajorAccountRejectMoveLines(List<PaymentScheduleLine> pslListGC, Company company, Account customerAccount, Move move, int ref)  {
		int ref2 = ref;

		for(PaymentScheduleLine paymentScheduleLine : pslListGC)  {
			try  {
				MoveLine moveLine = paymentScheduleImportService.createMajorAccountRejectMoveLine(paymentScheduleLineRepo.find(paymentScheduleLine.getId()), companyRepo.find(company.getId()),
						accountRepo.find(customerAccount.getId()), moveRepo.find(move.getId()), ref);

				if(moveLine != null)  {
					ref2++;
					updatePaymentScheduleLine(paymentScheduleLineRepo.find(paymentScheduleLine.getId()));
				}
			} catch (AxelorException e) {

				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_10), paymentScheduleLine.getName()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

				incrementAnomaly();

			} catch (Exception e) {

				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_10), paymentScheduleLine.getName()), e), IException.DIRECT_DEBIT, batch.getId());

				incrementAnomaly();

				log.error("Bug(Anomalie) généré(e) pour la création de l'écriture de rejet de l'échéance {}", paymentScheduleLine.getName());

			} finally {

				if (ref2 % 10 == 0) { JPA.clear(); }

			}

		}
		return ref2;
	}


	/**
	 *
	 * @param invoiceList
	 * @param company
	 * @param move
	 * @param ref
	 * @return
	 * @throws AxelorException
	 */
	public int createInvoiceRejectMoveLines(List<Invoice> invoiceList, Company company, Account customerAccount, Move move, int ref)  {
		int ref2 = ref;
		for(Invoice invoice : invoiceList)  {
			try  {
				MoveLine moveLine = paymentScheduleImportService.createInvoiceRejectMoveLine(invoiceRepo.find(invoice.getId()), companyRepo.find(company.getId()),
						accountRepo.find(customerAccount.getId()), moveRepo.find(move.getId()), ref2);
				if(moveLine != null)  {
					ref2++;
					updateInvoice(invoice);
				}
			} catch (AxelorException e) {

				TraceBackService.trace(new AxelorException(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_11), invoice.getInvoiceId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());

				incrementAnomaly();

			} catch (Exception e) {

				TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_11), invoice.getInvoiceId()), e), IException.DIRECT_DEBIT, batch.getId());

				incrementAnomaly();

				log.error("Bug(Anomalie) généré(e) pour la création de l'écriture de rejet de la facture {}", invoice.getInvoiceId());

			} finally {

				if (ref2 % 10 == 0) { JPA.clear(); }

			}
		}
		return ref2;
	}


	public void updateAllInvoice(List<Invoice> invoiceList)  {
		for(Invoice invoice : invoiceList)  {
			updateInvoice(invoice);
		}
	}

	public void updateAllPaymentScheduleLine(List<PaymentScheduleLine> paymentScheduleLineList)  {
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			updatePaymentScheduleLine(paymentScheduleLine);
		}
	}


	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = "";
		comment = I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_12);
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_PAYMENT_SCHEDULE_13)+"\n", batch.getDone());
		comment += String.format("\t* "+I18n.get(IExceptionMessage.BATCH_INTERBANK_PO_IMPORT_5)+" : %s \n", this.totalAmount);
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);

		super.stop();
		addComment(comment);

	}

}
