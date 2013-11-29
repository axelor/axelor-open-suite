/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
import com.axelor.apps.account.service.PaymentScheduleImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.persist.Transactional;

public class BatchPaymentScheduleImport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchPaymentScheduleImport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;
	
	private String updateCustomerAccountLog = "";
	
	@Inject
	public BatchPaymentScheduleImport(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService, BatchAccountCustomer batchAccountCustomer) {
		
		super(paymentScheduleImportService, rejectImportService, batchAccountCustomer);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
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
			company = Company.find(company.getId());
			
			AccountConfig accountConfig = company.getAccountConfig();
			
			data = rejectImportService.getCFONBFileByLot(accountConfig.getRejectImportPathAndFileName(), accountConfig.getTempImportPathAndFileName(), company, 1);
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			stop = true;
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			stop = true;
			
			LOG.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());
			
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
						
						List<PaymentScheduleLine> paymentScheduleLineRejectedList = paymentScheduleImportService.importRejectPaymentScheduleLine(rejectDate, refDebitReject, amountReject, InterbankCodeLine.find(causeReject.getId()), Company.find(company.getId()));
						
						List<Invoice> invoiceRejectedList = paymentScheduleImportService.importRejectInvoice(rejectDate, refDebitReject, amountReject, InterbankCodeLine.find(causeReject.getId()), Company.find(company.getId()));
						
						/***  Aucun échéancier ou facture trouvé(e) pour le numéro de prélèvement  ***/
						if((invoiceRejectedList == null || invoiceRejectedList.isEmpty()) && (paymentScheduleLineRejectedList == null || paymentScheduleLineRejectedList.isEmpty()))  {
							throw new AxelorException(String.format("%s :\n Aucun échéancier ou facture trouvé(e) pour le numéro de prélèvement : %s", 
									GeneralService.getExceptionAccountingMsg(),refDebitReject), IException.NO_VALUE);
						}
						else  {
							i++;
						}
						
					} catch (AxelorException e) {
						
						TraceBackService.trace(new AxelorException(String.format("Rejet %s", reject[1]), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
						
						incrementAnomaly();
						
					} catch (Exception e) {
						
						TraceBackService.trace(new Exception(String.format("Rejet %s", reject[1]), e), IException.DIRECT_DEBIT, batch.getId());
						
						incrementAnomaly();
						
						LOG.error("Bug(Anomalie) généré(e) pour l'import du rejet {}", reject[1]);
						
					} finally {
						
						if (i % 10 == 0) { JPA.clear(); }
			
					}
				}
		
				this.createRejectMove(Company.find(company.getId()), 
						paymentScheduleImportService.getPaymentScheduleLineMajorAccountList(), 
						paymentScheduleImportService.getInvoiceList(), 
						paymentScheduleImportService.getStatusUpr(), rejectDate);
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
	 * @param statusUpr
	 * 				Un status 'en cours'
	 * @return
	 * 				L'écriture des rejets
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createRejectMove(Company company, List<PaymentScheduleLine> pslListGC, 	List<Invoice> invoiceList, Status statusUpr, LocalDate rejectDate)  {
	
		// Création de l'écriture d'extourne par société
		Move move = this.createRejectMove(company, rejectDate);
		
		if(!stop)  {
			int ref = 1;  // Initialisation du compteur d'échéances
	
			/*** Echéancier de Lissage de paiement en PRLVT ***/
			if(pslListGC != null && pslListGC.size()!=0 )  {
				
				LOG.debug("Création des écritures de rejets : Echéancier de lissage de paiement");
				ref = this.createMajorAccountRejectMoveLines(pslListGC, Company.find(company.getId()), 
						Company.find(company.getId()).getAccountConfig().getCustomerAccount(), Move.find(move.getId()), ref);
				
			}
			
			/*** Facture en PRLVT ***/
			if(invoiceList != null && invoiceList.size()!=0)  {
				
				LOG.debug("Création des écritures de rejets : Facture");
				ref = this.createInvoiceRejectMoveLines(invoiceList, Company.find(company.getId()), 
						Company.find(company.getId()).getAccountConfig().getCustomerAccount(), Move.find(move.getId()), ref);
			}
			
	
			this.validateRejectMove(Company.find(company.getId()), Move.find(move.getId()), ref, rejectDate);
			
		}
		
		return move;
	}
	
	
	public Move createRejectMove(Company company, LocalDate date)  {
		Move move = null;
		try {
			move = paymentScheduleImportService.createRejectMove(company, date);
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			stop = true;
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			stop = true;
			
			LOG.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());
			
		}
		return move;
	}
	
	
	public void validateRejectMove(Company company, Move move, int ref, LocalDate rejectDate)  {
		try {
			
			if(ref > 1)  {

				MoveLine oppositeMoveLine = paymentScheduleImportService.createRejectOppositeMoveLine(company, Move.find(move.getId()), ref, rejectDate);
				
				paymentScheduleImportService.validateMove(Move.find(move.getId()));
				
				this.totalAmount = MoveLine.find(oppositeMoveLine.getId()).getCredit();
			}
			else {
				paymentScheduleImportService.deleteMove(Move.find(move.getId()));
			}
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException(String.format("Batch %s", batch.getId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
		} catch (Exception e) {
			
			TraceBackService.trace(new Exception(String.format("Batch %s", batch.getId()), e), IException.DIRECT_DEBIT, batch.getId());
			
			incrementAnomaly();
			
			LOG.error("Bug(Anomalie) généré(e) pour l'import des rejets de prélèvement {}", batch.getId());
			
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
				MoveLine moveLine = paymentScheduleImportService.createMajorAccountRejectMoveLine(PaymentScheduleLine.find(paymentScheduleLine.getId()), Company.find(company.getId()), 
						Account.find(customerAccount.getId()), Move.find(move.getId()), ref);
				
				if(moveLine != null)  {
					ref2++;
					updatePaymentScheduleLine(PaymentScheduleLine.find(paymentScheduleLine.getId()));
				}
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Création de l'écriture de rejet de l'échéance %s", paymentScheduleLine.getName()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Création de l'écriture de rejet de l'échéance %s", paymentScheduleLine.getName()), e), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour la création de l'écriture de rejet de l'échéance {}", paymentScheduleLine.getName());
				
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
				MoveLine moveLine = paymentScheduleImportService.createInvoiceRejectMoveLine(Invoice.find(invoice.getId()), Company.find(company.getId()), 
						Account.find(customerAccount.getId()), Move.find(move.getId()), ref2);
				if(moveLine != null)  {
					ref2++;
					updateInvoice(invoice);
				}
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Création de l'écriture de rejet de la facture %s", invoice.getInvoiceId()), e, e.getcategory()), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Création de l'écriture de rejet de la facture %s", invoice.getInvoiceId()), e), IException.DIRECT_DEBIT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour la création de l'écriture de rejet de la facture {}", invoice.getInvoiceId());
				
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
		comment = "Compte rendu de l'import des rejets de prélèvement :\n";
		comment += String.format("\t* %s prélèvement(s) rejeté(s)\n", batch.getDone());
		comment += String.format("\t* Montant total : %s \n", this.totalAmount);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		comment += String.format("\t* ------------------------------- \n");
		comment += String.format("\t* %s ", updateCustomerAccountLog);

		super.stop();
		addComment(comment);
		
	}

}
