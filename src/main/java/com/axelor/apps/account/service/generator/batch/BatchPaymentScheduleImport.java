package com.axelor.apps.account.service.generator.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
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
	
	@Inject
	public BatchPaymentScheduleImport(PaymentScheduleImportService paymentScheduleImportService, RejectImportService rejectImportService) {
		
		super(paymentScheduleImportService, rejectImportService);
		
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
		}
		
	}
	
	
	public void runImportProcess(Company company)  {
		
		List<String[]> data = null;
		
		try {
			company = Company.find(company.getId());
			data = rejectImportService.getCFONBFile(company.getRejectImportPathAndFileName(), company.getTempImportPathAndFileName(), company, 1);
			
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
		
		String moveDate = "";
		boolean isMoveDate = true;
		
		if(!stop)  {
			for(String[] reject : data)  {
				try {
					
					String dateReject = reject[0];
					
					if(isMoveDate)  {  moveDate = dateReject;  }
					
					String refDebitReject = reject[1];
					BigDecimal amountReject = new BigDecimal(reject[2]);
					InterbankCodeLine causeReject = rejectImportService.getInterbankCodeLine(reject[3], 1);
					
					List<PaymentScheduleLine> paymentScheduleLineRejectedList = paymentScheduleImportService.importRejectPaymentScheduleLine(dateReject, refDebitReject, amountReject, InterbankCodeLine.find(causeReject.getId()), Company.find(company.getId()));
					
					List<Invoice> invoiceRejectedList = paymentScheduleImportService.importRejectInvoice(dateReject, refDebitReject, amountReject, InterbankCodeLine.find(causeReject.getId()), Company.find(company.getId()));
					
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
		}
		
		if(!stop)  {
			this.createRejectMove(Company.find(company.getId()), 
					paymentScheduleImportService.getPaymentScheduleLinePaymentList(), 
					paymentScheduleImportService.getPaymentScheduleLineMajorAccountList(), 
					paymentScheduleImportService.getInvoiceList(), 
					paymentScheduleImportService.getStatusUpr(), moveDate);
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
	public Move createRejectMove(Company company, List<PaymentScheduleLine> pslListGC, List<PaymentScheduleLine> pslListPayment, List<Invoice> invoiceList, Status statusUpr, String moveDate)  {
	
		LocalDate rejectDate = rejectImportService.createRejectDate(moveDate);
		
		// Création de l'écriture d'extourne par société
		Move move = this.createRejectMove(company, rejectDate);
		
		if(!stop)  {
			int ref = 1;  // Initialisation du compteur d'échéances
	
			/*** Echéancier de Mensu Grand Compte en PRLVT ***/
			if(pslListGC != null && pslListGC.size()!=0 )  {
				
				LOG.debug("Création des écritures de rejets : Echéancier de mensu grand compte");
				ref = this.createMajorAccountRejectMoveLines(pslListGC, Company.find(company.getId()), 
						Company.find(company.getId()).getCustomerAccount(), Move.find(move.getId()), ref);
				
			}
			
			/*** Facture en PRLVT ***/
			if(invoiceList != null && invoiceList.size()!=0)  {
				
				LOG.debug("Création des écritures de rejets : Facture");
				ref = this.createInvoiceRejectMoveLines(invoiceList, Company.find(company.getId()), 
						Company.find(company.getId()).getCustomerAccount(), Move.find(move.getId()), ref);
			}
			
			/*** Echéancier de paiement en PRLVT  ***/
			if(pslListPayment != null && pslListPayment.size()!=0 )  {
				
				LOG.debug("Création des écritures de rejets : Echéancier de paiement");
				ref = this.createPaymentScheduleRejectMoveLines(pslListPayment, Company.find(company.getId()), Move.find(move.getId()), ref, statusUpr);
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
	
	
	/**
	 * 
	 * @param pslListPayment
	 * 				Une liste de ligne d'échéancier de paiement
	 * @param pslListNewPayment
	 * 				La liste des nouvelles lignes d'échéancier de paiement
	 * @param company
	 * 				Une société
	 * @param move
	 * 				L'écriture de rejet
	 * @param ref
	 * 				Le numéro de ligne d'écriture
	 * @param statusUpr
	 * 				Le status 'en cours'
	 * @return
	 * 				Le numéro de ligne d'écriture incrémenté
	 * @throws AxelorException
	 */
	public int createPaymentScheduleRejectMoveLines(List<PaymentScheduleLine> pslListPayment, Company company, Move move, int ref, Status statusUpr)  {
		int ref2 = ref;
		for(PaymentScheduleLine paymentScheduleLine : pslListPayment)  {
			try  {
				MoveLine moveLine = paymentScheduleImportService.createPaymentScheduleRejectMoveLine(PaymentScheduleLine.find(paymentScheduleLine.getId()), 
						Company.find(company.getId()), Move.find(move.getId()), ref2, Status.find(statusUpr.getId()));
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

		super.stop();
		addComment(comment);
		
	}

}
