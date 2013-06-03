package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.DirectDebitManagement;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.payment.PaymentVoucherService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleExportService {

	private static final Logger LOG = LoggerFactory.getLogger(PaymentScheduleExportService.class);

	@Inject
	private MoveService ms;

	@Inject
	private MoveLineService mls;
	
	@Inject
	private ReconcileService rcs;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private PaymentVoucherService pvs;
	
	@Inject
	private PaymentModeService pms;

	@Inject
	private CfonbService cs;
	
	@Inject
	private PaymentService pas;

	private LocalDate today;
	
	private DateTime dateTime;

	@Inject
	public PaymentScheduleExportService() {
		
		this.today = GeneralService.getTodayDate();
		this.dateTime = GeneralService.getTodayDateTime();
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteMove(Move move) throws AxelorException  {
		move.remove();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createOppositeExportMensuMoveLine(Move move, Account bankAccount, int ref) throws AxelorException  {
		LOG.debug("Montant de la contrepartie : {}", totalAmount(move));
		
		MoveLine moveLine = mls.createMoveLine(move, null, bankAccount, this.totalAmount(move), true, false, today, ref, false, false, false, null);
		
		move.getMoveLineList().add(moveLine);
		moveLine.save();
		return move;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createExportMensuMove(Journal journal, Company company, PaymentMode paymentMode) throws AxelorException  {
		return ms.createMove(journal, company, null, null, paymentMode, false).save();
	}
	
	
	public void testBankDetails(PaymentSchedule paymentSchedule) throws AxelorException  {
		Partner partner = paymentSchedule.getPartner();
		BankDetails bankDetails = paymentSchedule.getBankDetails();
		if(bankDetails == null)  {
			bankDetails = partner.getBankDetails();
		}
		if(bankDetails == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour l'échéancier de paiement %s",
					GeneralService.getExceptionAccountingMsg(),paymentSchedule.getScheduleId()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cs.testBankDetailsField(bankDetails);
		}
	}
	
	
	public void testBankDetails(Invoice invoice) throws AxelorException  {
		BankDetails bankDetails = invoice.getPartner().getBankDetails();
		
		if(bankDetails == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
					GeneralService.getExceptionAccountingMsg(), invoice.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cs.testBankDetailsField(bankDetails);
		}
	}
	
	
	public void testBankDetails(Partner partner) throws AxelorException  {
		BankDetails bankDetails = partner.getBankDetails();
		
		if(bankDetails == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un RIB pour le tiers %s",
					GeneralService.getExceptionAccountingMsg(), partner.getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cs.testBankDetailsField(bankDetails);
		}
	}
	
	
	/**
	 * Méthode permettant de retrouver l'échéance rejetée qui à impliquer la création de la nouvelle échéance
	 * @param paymentScheduleLine
	 * 			La nouvelle échéance
	 */
	public PaymentScheduleLine getPaymentScheduleLineRejectOrigin(PaymentScheduleLine paymentScheduleLine)  {
		
		return PaymentScheduleLine
				.all().filter("self.paymentSchedule = ?1 AND self.scheduleLineSeq = ?2 AND self.status.code = 'clo' ORDER BY self.rejectDate DSC"
						, paymentScheduleLine.getPaymentSchedule(), paymentScheduleLine.getScheduleLineSeq()).fetchOne();
		
	}
	
	
	
	/**
	 * Methode permettant de récupérer le montant à prélever suivant que l'échéance soit une échéance rejetée représentée ou non.
	 * @param paymentScheduleLine
	 * 			Un échéance à prélever
	 * @return
	 * 			Le montant à prélever
	 */
	public BigDecimal getMonthlyPaymentAmountToPay(PaymentScheduleLine paymentScheduleLine, boolean isMajorAccount)  {
		if(paymentScheduleLine.getFromReject() && isMajorAccount)  {
			return paymentScheduleLine.getAdvanceMoveLine().getAmountRemaining();
		}
		else  {
			return paymentScheduleLine.getInTaxAmount();
		}
	}
	
	
	/**
	 * Fonction calculant le montant des échéances de la contrepartie
	 * @param pslList
	 * 			Une Liste d'échéances
	 * @return
	 * 			Le montant total
	 */
	public BigDecimal totalAmount(List<PaymentScheduleLine> pslList)  {
		BigDecimal total = BigDecimal.ZERO;
		for(PaymentScheduleLine psl : pslList)  {
			total=total.add(psl.getInTaxAmount());
		}
		return total;
	}
	
	/**
	 * Fonction calculant le montant de la contrepartie d'une écriture de prélèvement
	 * @param pslList
	 * 			Une écriture de prélèvement
	 * @return
	 * 			Le montant total
	 */
	public BigDecimal totalAmount(Move move)  {
		BigDecimal total = BigDecimal.ZERO;
		for(MoveLine moveLine : move.getMoveLineList())  {
			total=total.add(moveLine.getCredit());
		}
		LOG.debug("Montant total : {}", total);

		return total;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentScheduleLine generateExportMensu (PaymentScheduleLine paymentScheduleLine, List<PaymentScheduleLine> pslList, Status statusVal, Company company, 
			boolean isMajorAccount, int ref, Move move) throws AxelorException  {
		
		this.testBankDetails(paymentScheduleLine.getPaymentSchedule());
		
		paymentScheduleLine.setStatus(statusVal);
		
		Account account;
		
		this.setDebitNumber(pslList, paymentScheduleLine, company, company.getMajorAccountJournal());
		
		account = company.getCustomerAccount();
			
		LOG.debug("generateAllDeposit - psl.getInTaxAmount() : {}", paymentScheduleLine.getInTaxAmount());
		
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		
		BigDecimal amount =  paymentScheduleLine.getInTaxAmount();
		
		paymentScheduleLine.setInTaxAmountPaid(amount);
		
		if(amount.compareTo(BigDecimal.ZERO) == 1)  {
			MoveLine moveLine = mls.createMoveLine(move , paymentSchedule.getPartner(), account, amount, false, false, 
					today, ref, false, false, false, paymentScheduleLine.getName());
			
			move.getMoveLineList().add(moveLine);
			moveLine.save();  
			
			if(paymentScheduleLine.getFromReject()) {
				// lettrage avec le rejet
				PaymentScheduleLine rejectedPaymentScheduleLine = this.getPaymentScheduleLineRejectOrigin(paymentScheduleLine);
				if(rejectedPaymentScheduleLine.getRejectMoveLine() != null 
						&& rejectedPaymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) == 1)  {
					rcs.reconcile(rejectedPaymentScheduleLine.getRejectMoveLine(), moveLine);
				}
			}
			else  {
				// Lettrage du paiement avec les factures d'échéances
				this.reconcileDirectDebit(moveLine, paymentSchedule);
			}
			
			ref+=1;	
			
			// Maj du champ ligne d'écriture générée sur la ligne d'échéancier ??
			paymentScheduleLine.setAdvanceOrPaymentMove(Move.find(move.getId()));
			paymentScheduleLine.setAdvanceMoveLine(moveLine);
		}
		paymentScheduleLine.save();
		return paymentScheduleLine;
	}
	
	
	
	/**
	 * Procédure permettant de lettrer l'écriture de paiement avec les écritures des factures d'échéance mensu grand compte du contrat
	 * @param contractLine
	 * 			Un contrat
	 * @param creditMoveLine
	 * 			Une écriture de paiement par prélèvement d'une échéance
	 * @throws AxelorException
	 */
	public void reconcileDirectDebit(MoveLine creditMoveLine, PaymentSchedule paymentSchedule) throws AxelorException  {
		List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
		creditMoveLineList.add(creditMoveLine);
		pas.useExcessPaymentOnMoveLines(this.getInvoiceMoveLineListToReconcile(paymentSchedule), creditMoveLineList);
	}
	
	
	/**
	 * Méthode permettant de récupérer les factures d'échéance mensu grand compte d'un contrat
	 * @param contractLine
	 * 			Un contrat
	 * @return
	 */
	public List<MoveLine> getInvoiceMoveLineListToReconcile(PaymentSchedule paymentSchedule)  {
		return MoveLine.all()
				.filter("self.move.state = ?1 AND self.exportedDirectDebitOk = 'false' " +
						"AND self.account.reconcileOk = ?2 AND self.amountRemaining > 0 " +
						"AND self.move.invoice.operationTypeSelect = ?3 " +
						"AND self.move.invoice.schedulePaymentOk = 'false' " +
						"AND self.move.invoice.invoiceSubTypeSelect = 4 " +
						"AND self.contractLine.debitBlockingOk IN ('false',null) " +
						"AND self.move.invoice.debitBlockingOk IN ('false',null) " +
						"AND self.move.invoice.paymentSchedule = ?4 "+
						"ORDER BY self.date", IAccount.VALIDATED_MOVE, true, IInvoice.CLIENT_SALE, paymentSchedule).fetch();
	}
	
	
	/**
	 * Procédure permettant d'assigner un numéro de prélèvement à l'échéance à prélever
	 * Si plusieurs échéance d'un même échéancier sont à prélever, alors on utilise un objet de gestion de prélèvement encadrant l'ensemble des échéances en question
	 * Sinon on assigne simplement un numéro de prélèvement à l'échéance
	 * @param paymentScheduleLineList
	 * 			Une liste d'échéance à prélever
	 * @param paymentScheduleLine
	 * 			L'échéance traité
	 * @param company
	 * 			Une société
	 * @param journal
	 * 			Un journal (prélèvement mensu masse ou grand compte)
	 * @throws AxelorException
	 */
	public void setDebitNumber(List<PaymentScheduleLine> paymentScheduleLineList, PaymentScheduleLine paymentScheduleLine, Company company, Journal journal) throws AxelorException  {
		if(hasOtherPaymentScheduleLine(paymentScheduleLineList, paymentScheduleLine))  {
			DirectDebitManagement directDebitManagement = this.getDirectDebitManagement(paymentScheduleLineList, paymentScheduleLine);
			if(directDebitManagement == null)  {
				directDebitManagement = this.createDirectDebitManagement(this.getDirectDebitSequence(company, journal), company);
			}
			paymentScheduleLine.setDirectDebitManagement(directDebitManagement);
			directDebitManagement.getPaymentScheduleLineList().add(paymentScheduleLine);
		}
		else  {
			paymentScheduleLine.setDebitNumber(this.getDirectDebitSequence(company, journal));
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier que les journaux sont bien configuré dans la société
	 * @param company
	 * @throws AxelorException
	 */
	public void checkCompanyJournal(Company company) throws AxelorException  {
		if(company.getInvoiceDirectDebitJournal() == null)  {
			throw new AxelorException(String.format(
					"%s :\n Erreur : Veuillez configurer un Journal prélèvement facture et échéancier hors mensu pour la société %s"
					,GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	/**
	 * Procédure permettant de vérifier que les journaux sont bien configuré dans la société
	 * @param company
	 * @throws AxelorException
	 */
	public void checkCompanyJournalMonthlyPayment(Company company) throws AxelorException  {
		if(company.getMajorAccountJournal() == null)  {
			throw new AxelorException(String.format(
					"%s :\n Erreur : Veuillez configurer un Journal mensu grand compte pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier que la date d'échéance est bien remplie
	 * @param company
	 * @throws AxelorException
	 */
	public void checkDebitDate(AccountingBatch accountingBatch) throws AxelorException  {
		if(accountingBatch.getDebitDate() == null)  {
			throw new AxelorException(String.format(
					"%s :\n Erreur : Veuillez configurer une date de prélèvement pour la configuration de batch %s"
					,GeneralService.getExceptionAccountingMsg(),accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
	}
	
	/**
	 * Methode permettant de récupérer la liste des échéances à prélever en fonction de la société et de la date de prélèvement
	 * @param company
	 * 			Une société
	 * @param debitDate
	 * 			Une date de prélèvement
	 * @return
	 */
	public List<PaymentScheduleLine> getPaymentScheduleLineToDebit(Company company, LocalDate debitDate, PaymentMode paymentMode, boolean isMajorAccount)  {
		if(isMajorAccount)  {
			return PaymentScheduleLine.all()
					.filter("self.status.code = 'upr' AND self.debitBlockingOk IN ('f',null) AND self.paymentSchedule.state = '2' AND self.paymentSchedule.company = ?1 " +
							"AND EXTRACT (day from self.scheduleDate) = ?2 AND self.scheduleDate <= ?3 " +
							"AND (self.paymentSchedule.contractLine.debitBlockingOk IN ('false',null) OR (self.paymentSchedule.contractLine.debitBlockingOk = 'true' AND self.paymentSchedule.contractLine.debitBlockingToDate < ?4)) " +
							"AND self.debitBlockingOk IN ('false',null) AND self.paymentSchedule.contractLine.monthlyPaymentMajorAccount.earlyEndCycleOk IN ('false', null) " +
							"AND self.paymentSchedule.paymentMode = ?5 AND self.paymentSchedule.natureSelect = ?6 ORDER BY self.scheduleDate"
							, company, debitDate.getDayOfMonth(), debitDate, debitDate, paymentMode, IAccount.MAJOR_ACCOUNT_SCHEDULE).fetch(); 
		}
		else  {
			return PaymentScheduleLine.all()
					.filter("self.status.code = 'upr' AND self.debitBlockingOk IN ('f',null) AND self.paymentSchedule.state = '2' AND self.paymentSchedule.company = ?1 " +
							"AND EXTRACT (day from self.scheduleDate) = ?2 AND self.scheduleDate <= ?3 " +
							"AND self.debitBlockingOk IN ('false',null) AND self.paymentSchedule.contractLine.monthlyPayment.earlyEndCycleOk IN ('false', null) " +
							"AND (self.paymentSchedule.contractLine.debitBlockingOk IN ('false',null) OR (self.paymentSchedule.contractLine.debitBlockingOk = 'true' AND self.paymentSchedule.contractLine.debitBlockingToDate < ?4)) " +
							"AND self.paymentSchedule.paymentMode = ?5 AND self.paymentSchedule.natureSelect = ?6 AND self.invoice IS NOT NULL ORDER BY self.scheduleDate"
							, company, debitDate.getDayOfMonth(), debitDate, debitDate, paymentMode, IAccount.MONTHLY_PAYMENT_SCHEDULE).fetch(); 
		}
	}
	
	
	
	/**
	 * Fonction permettant de tester et de récupérer une séquence de prélèvement
	 * @param company
	 * 			Une société
	 * @param journal
	 * 			Un journal
	 * @return
	 * @throws AxelorException
	 */
	public String getDirectDebitSequence(Company company, Journal journal) throws AxelorException  {
		String seq = sgs.getSequence(IAdministration.DEBIT,company,journal, false);
		if(seq == null  || seq.isEmpty())  {
			throw new AxelorException(String.format(
							"%s :\n Veuillez configurer une séquence Numéro de prélèvement pour la société %s pour le journal %s ",
							GeneralService.getExceptionAccountingMsg(),company.getName(),journal.getName()), IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	
	
	/**
	 * Y a t'il d'autres échéance a exporter pour le même payeur ?
	 * @param pslList : une liste d'échéance
	 * @param psl
	 * @return
	 */
	public boolean hasOtherPaymentScheduleLine(List<PaymentScheduleLine> pslList, PaymentScheduleLine psl)  {
		int i = 0;
		for(PaymentScheduleLine paymentScheduleLine : pslList)  {
			paymentScheduleLine = PaymentScheduleLine.find(paymentScheduleLine.getId());
			if(psl.getPaymentSchedule().equals(paymentScheduleLine.getPaymentSchedule()))  {
				i++;
			}
		}
		return i > 1;
	}
	
	/**
	 * Y a t'il d'autres facture a exporter pour le même payeur ?
	 * @param moveLineList : une liste de ligne d'écriture de facture
	 * @param psl
	 * @return
	 */
	public boolean hasOtherInvoice(List<MoveLine> moveLineList, MoveLine ml)  {
		int i = 0;
		for(MoveLine moveLine : moveLineList)  {
			moveLine=MoveLine.find(moveLine.getId());
			if(ml.getPartner().equals(moveLine.getPartner()))  {
				i++;
			}
		}
		return i > 1;
	}
	
	
	/**
	 * Procédure permettant de récupérer l'objet de gestion déjà créé lors du prélèvement d'une autre échéance
	 * @param pslList
	 * 			La liste d'échéance à prélever
	 * @param psl
	 * 			L'échéance à prélever
	 * @return
	 * 			L'objet de gestion trouvé
	 */
	public DirectDebitManagement getDirectDebitManagement(List<PaymentScheduleLine> pslList, PaymentScheduleLine psl)  {
		for(PaymentScheduleLine paymentScheduleLine : pslList)  {
			paymentScheduleLine = PaymentScheduleLine.find(paymentScheduleLine.getId());
			if(psl.getPaymentSchedule().equals(paymentScheduleLine.getPaymentSchedule()))  {
				if(paymentScheduleLine.getDirectDebitManagement() != null)  {
					return paymentScheduleLine.getDirectDebitManagement();
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Procédure permettant de récupérer l'objet de gestion déjà créé lors du prélèvement d'une autre facture
	 * @param mlList
	 * 			La liste des lignes d'écriture de facture à prélever
	 * @param ml
	 * 			Une ligne d'écriture de facture ) prélever
	 * @return
	 * 			L'objet de gestion trouvé
	 */
	public DirectDebitManagement getDirectDebitManagement(List<MoveLine> mlList, MoveLine ml)  {
		for(MoveLine moveLine : mlList)  {
			moveLine = MoveLine.find(moveLine.getId());
			if(ml.getPartner().equals(moveLine.getPartner()))  {
				Invoice invoice = cs.getInvoice(moveLine);
				if(invoice.getDirectDebitManagement() != null)  {
					return invoice.getDirectDebitManagement();
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Procédure permettant de créer un objet de gestion de prélèvement
	 * @param sequence
	 * 			La séquence de prélèvement à utiliser
	 * @param company
	 * 			Une société
	 * 			
	 * @return
	 */
	public DirectDebitManagement createDirectDebitManagement(String sequence, Company company)  {
		DirectDebitManagement directDebitManagement = new DirectDebitManagement();
		directDebitManagement.setDebitNumber(sequence);
		directDebitManagement.setInvoiceSet(new HashSet<Invoice>());
		directDebitManagement.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());
		directDebitManagement.setCompany(company);
		return directDebitManagement;
	}
	
	
	/**
	 * Méthode permettant de construire un log affichant une liste d'échéance
	 * @param paymentScheduleLineList
	 * @return
	 */
	public String toStringPaymentScheduleLineList(List<PaymentScheduleLine> paymentScheduleLineList)  {
		String list = " (nb = ";
		list += paymentScheduleLineList.size();
		list += " ) : ";
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			list += paymentScheduleLine.getName();
			list += ", ";
		}
		return list;
	}
	
	/**
	 * Méthode permettant de construire un log affichant une liste de lignes d'écriture
	 * @param paymentScheduleLineList
	 * @return
	 */
	public String toStringMoveLineList(List<MoveLine> moveLineList)  {
		String list = " (nb = ";
		list += moveLineList.size();
		list += " ) : ";
		for(MoveLine moveLine : moveLineList)  {
			list += moveLine.getName();
			list += ", ";
		}
		return list;
	}
	
	
	public PaymentScheduleLine exportPaymentScheduleLine(PaymentScheduleLine paymentScheduleLine, List<PaymentScheduleLine> paymentScheduleLineList, PaymentMode paymentMode, Company company, Status status) throws AxelorException   {
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		
		this.testBankDetails(paymentSchedule);
		
		if(paymentScheduleLine.getMoveLineGenerated()!=null)  {
			
			paymentScheduleLine.setInTaxAmountPaid(paymentScheduleLine.getAmountRemaining());
			
			this.createPaymentScheduleMove(paymentScheduleLine, paymentMode, paymentSchedule.getPartner(), 
					paymentScheduleLine.getMoveLineGenerated(), paymentScheduleLine.getAmountRemaining(), company, company.getInvoiceDirectDebitJournal());
			
			paymentScheduleLine.setStatus(status);
			
			this.setDebitNumber(paymentScheduleLineList, paymentScheduleLine, company, company.getInvoiceDirectDebitJournal());
			
			return paymentScheduleLine;

		}
		return null;
	}
	
	
	public List<PaymentScheduleLine> getPaymentScheduleLineToDebit(Company company, LocalDate scheduleDate, PaymentMode paymentMode)  {
		List<PaymentScheduleLine> paymentScheduleLineList = PaymentScheduleLine
				.all().filter("status.code = 'upr' AND debitBlockingOk IN ('f',null)" +
						" AND paymentSchedule.company = ?1 AND paymentSchedule.state = '2' " +
						"AND self.scheduleDate <= ?2 AND paymentSchedule.natureSelect = ?3 " +
						"AND (paymentSchedule.contractLine.debitBlockingOk IN ('false',null) OR (paymentSchedule.contractLine.debitBlockingOk = 'true' AND paymentSchedule.contractLine.debitBlockingToDate < ?4)) " +
						"AND self.debitBlockingOk IN ('false',null) " +
						"AND paymentSchedule.paymentMode = ?5 ORDER BY self.scheduleDate"   
						, company
						, scheduleDate
						, IAccount.PAYMENT_SCHEDULE
						, scheduleDate
						, paymentMode).fetch(); 	
		
		LOG.debug("\n Liste des échéances de paiement retenues {} \n", this.toStringPaymentScheduleLineList(paymentScheduleLineList));
		
		return paymentScheduleLineList;
	}
	
	
	/**
	 * Procédure permettant d'exporter des factures
	 * @param company
	 * 			Une société
	 * @param pse
	 * 			Un Export des prélèvement
	 * @param pm
	 * 			Un mode de paiement
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice exportInvoice(MoveLine moveLine, List<MoveLine> moveLineList, Company company, PaymentMode paymentMode) throws AxelorException  {
			
		/** Important : Doit être executé avant la méthode 'createPaymentMove()' afin de récupérer le montant restant à prélever **/
		BigDecimal amountExported = moveLine.getAmountRemaining();
		
		this.testBankDetails(moveLine.getPartner());
		
		// creation d'une ecriture de paiement
		Invoice invoice = this.updateInvoice(moveLine, 
				this.createPaymentMove(company, moveLine, paymentMode),
				moveLineList,
				amountExported);
		invoice.save();
		
		return invoice;
	}
	
	
	
	public List<MoveLine> getInvoiceToExport(Company company, PaymentMode paymentMode, LocalDate scheduleDate)  {
		/**
		 * Selection des lignes d'écritures dont :
		 * - l'état est validé
		 * - la société est celle selectionnée sur l'objet export
		 * - le compte est lettrable
		 * - le montant restant à payer est supérieur à 0 et débit supérieur à 0 (équivaut à une facture et non un avoir)
		 * - le mode de règlement de la facture est en prélèvement
		 * - la date d'échéance est passée
		 * - la facture est remplie sur l'écriture
		 * - la facture n'est pas selectionnée sur un échéancier
		 * - la facture n'est pas du type : 
		 * 		- virtuel
		 * 		- échéancier
		 * 		- RI
		 * 		- Bordereau de regroupement
		 */
		List<MoveLine> mlList = MoveLine
				.all()
				.filter("self.move.state = ?1 AND self.exportedDirectDebitOk = 'false' " +
						"AND self.move.company = ?2 " +
						"AND self.account.reconcileOk = ?3 AND self.amountRemaining > 0 " +
						"AND self.debit > 0 " +
						"AND self.dueDate <= ?5 AND self.move.invoice IS NOT NULL " +
						"AND self.move.invoice.paymentMode = ?4 " +
//						"AND self.move.invoice.memoryInvoice IS NULL " +
//						"AND self.move.invoice.inMemoryOk = 'false' " +
//						"AND self.move.invoice.toMemoryOk = 'false' " +
						"AND self.move.invoice.invoicePaymentCondition.invoicesBill IS NULL " +
						"AND self.move.invoice.invoicePaymentCondition.payerPartner.treasuryOk = 'false' "+
						"AND self.move.invoice.schedulePaymentOk = 'false' " +
						"AND ((self.contractLine.debitBlockingOk IN ('false',null)) OR (self.contractLine.debitBlockingOk = 'true' AND self.contractLine.debitBlockingToDate < ?10)) " +
						"AND ((self.move.invoice.debitBlockingOk IN ('false',null)) OR (self.move.invoice.debitBlockingOk = 'true' AND self.move.invoice.debitBlockingToDate < ?11)) "
						, IAccount.VALIDATED_MOVE, company, true, paymentMode, scheduleDate
						, scheduleDate, scheduleDate
						).fetch(); 
		
		
		// Récupération des factures rejetées
		List<Invoice> invoiceRejectList = Invoice.all()
				.filter("self.rejectMoveLine IS NOT NULL AND self.rejectMoveLine.amountRemaining > 0 AND self.rejectMoveLine.debit > 0" +
						" AND self.paymentMode = ?1 AND self.company = ?2 AND self.rejectMoveLine.exportedDirectDebitOk = 'false' AND self.move.state = ?3" +
						" AND self.rejectMoveLine.account.reconcileOk = 'true' " +
						" AND (self.contractLine.debitBlockingOk IN ('false',null) OR (self.contractLine.debitBlockingOk = 'true' AND self.contractLine.debitBlockingToDate < ?4))" +
						" AND (self.debitBlockingOk IN ('false',null) OR (self.debitBlockingOk = 'true' AND self.debitBlockingToDate < ?5))" +
						" AND self.rejectMoveLine.invoiceReject IS NOT NULL"
						, paymentMode, company, IAccount.VALIDATED_MOVE, scheduleDate, scheduleDate).fetch();
		
		// Ajout des factures rejetées
		for(Invoice invoice : invoiceRejectList)  {
			mlList.add(invoice.getRejectMoveLine());
		}
		
		LOG.debug("\n Liste des lignes d'écritures retenues {} \n", this.toStringMoveLineList(mlList));
		
		return mlList;
	}
	
	
	
	/**
	 * Procédure permettant de créer une écriture de paiement d'une facture
	 * @param company
	 * 			Une société
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @param pm
	 * 			Un mode de paiement
	 * @param pse
	 * 			Un Export des prélèvement
	 * @throws AxelorException
	 */
	public Move createPaymentMove(Company company, MoveLine moveLine, PaymentMode pm) throws AxelorException  {

		LOG.debug("generateAllExportInvoice - Création de l'écriture");
		
		Move paymentMove = ms.createMove(company.getInvoiceDirectDebitJournal(), company, null, null, pm, false);
			
		BigDecimal amountExported = moveLine.getAmountRemaining();
		
		this.createPaymentMoveLine(paymentMove, moveLine, pm);
		
		LOG.debug("generateAllExportInvoice - Création de la seconde ligne d'écriture");
		
		Account paymentModeAccount = pms.getCompanyAccount(pm, company);
		
		String invoiceName = "";
		if(moveLine.getMove().getInvoice()!=null)  {
			invoiceName = moveLine.getMove().getInvoice().getInvoiceId();
		}
		MoveLine moveLineGenerated2 = mls.createMoveLine(paymentMove, null, paymentModeAccount, amountExported,
				true, false, today, 2, false, false, false, invoiceName);
				
		
		
		paymentMove.getMoveLineList().add(moveLineGenerated2);
		moveLineGenerated2.save();
			
		ms.validateMove(paymentMove);
		paymentMove.save();
		
		return paymentMove;
	}
	
	
	public void createPaymentMoveLine(Move paymentMove, MoveLine moveLine, PaymentMode pm) throws AxelorException  {
		BigDecimal amountExported = moveLine.getAmountRemaining();
		
		// On assigne le montant exporté pour pouvoir l'utiliser lors de la création du fichier d'export CFONB
		moveLine.setAmountExportedInDirectDebit(amountExported);
		
		// creation d'une ecriture de paiement
			
		LOG.debug("generateAllExportInvoice - Création de la première ligne d'écriture");
		String invoiceName = "";
		if(moveLine.getMove().getInvoice()!=null)  {
			invoiceName = moveLine.getMove().getInvoice().getInvoiceId();
		}
		MoveLine moveLineGenerated = mls.createMoveLine(paymentMove, moveLine.getPartner(), moveLine.getAccount(),
				amountExported, false, false, today, 1, false, false, false, invoiceName);
	
		paymentMove.getMoveLineList().add(moveLineGenerated);

		moveLineGenerated.save();
		
		// Lettrage de la ligne 411 avec la ligne 411 de la facture
		LOG.debug("Creation du lettrage de la ligne 411 avec la ligne 411 de la facture");
		
		rcs.reconcile(moveLine, moveLineGenerated);
		
		LOG.debug("generateAllExportInvoice - Sauvegarde de l'écriture");
		
		paymentMove.save();
		
		
	}
	
	
	/**
	 * Methode permettant de mettre à jour les informations de la facture
	 * 
	 * @param moveLine
	 * @param paymentMove
	 * @param pse
	 * @param mlList
	 * @return
	 * @throws AxelorException
	 */
	public Invoice updateInvoice(MoveLine moveLine, Move paymentMove, List<MoveLine> mlList, BigDecimal amountExported) throws AxelorException  {
		
		Invoice invoice = cs.getInvoice(moveLine);
		
		Company company = invoice.getCompany();
		
		// Mise à jour du champ 'Ecriture de paiement' sur la facture
		LOG.debug("generateAllExportInvoice - Mise à jour du champ 'Ecriture de paiement' sur la facture");
		invoice.setPaymentMove(paymentMove);
		
		// Mise à jour du montant prélever
		invoice.setDirectDebitAmount(amountExported);
		
		// Mise à jour du Numéro de prélèvement sur la facture
		LOG.debug("Mise à jour du Numéro de prélèvement sur la facture");
		String seqInvoice = sgs.getSequence(IAdministration.DEBIT,company,company.getInvoiceDirectDebitJournal(), false);
		if(seqInvoice == null  || seqInvoice.isEmpty())  {
			throw new AxelorException(String.format(
							"%s :\n Erreur : Veuillez configurer une séquence Numéro de prélèvement pour la société %s et le journal %s",
							GeneralService.getExceptionAccountingMsg(),company.getName(),company.getInvoiceDirectDebitJournal().getName()), IException.CONFIGURATION_ERROR);
		}
		
		if(this.hasOtherInvoice(mlList, moveLine))  {
			DirectDebitManagement directDebitManagement = this.getDirectDebitManagement(mlList, moveLine);
			if(directDebitManagement == null)  {
				directDebitManagement = this.createDirectDebitManagement(seqInvoice, company);
			}
			invoice.setDirectDebitManagement(directDebitManagement);
			directDebitManagement.getInvoiceSet().add(invoice);
			directDebitManagement.save();
		}
		else  {
			invoice.setDebitNumber(seqInvoice);
		}
		return invoice;
	}
	
	
	/**
	 * Procédure permettant de créer une écriture de paiement d'une échéance
	 * Même méthode qu'utiliser lors d'une saisie paiement
	 * @param psl
	 * @param paymentMode
	 * @param payerPartner
	 * @param moveLineToPay
	 * @param amount
	 * @param company
	 * @param journal
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createPaymentScheduleMove(PaymentScheduleLine psl, PaymentMode paymentMode, Partner payerPartner, MoveLine moveLineToPay, BigDecimal amount, Company company, Journal journal) throws AxelorException {
		PaymentSchedule paymentSchedule = psl.getPaymentSchedule();
		
		Move move = ms.createMove(journal,
				company,
				null,
				payerPartner,
				paymentMode,
				false);
			
		int moveLineNo = pvs.toPayPaymentScheduleLine(null, paymentSchedule, payerPartner, 1, 
				amount, company, psl, moveLineToPay, paymentMode, move);
		
		MoveLine debitMoveLine = mls.createMoveLine(move,payerPartner,pms.getCompanyAccount(paymentMode, company),amount,true,false,this.today,moveLineNo,false,false,false, psl.getName());
		move.getMoveLineList().add(debitMoveLine);
		debitMoveLine.save(); 
		
		// Lettrage de l'écriture de paiement avec l'écriture de rejet, si paiement d'un rejet
		if(moveLineToPay.getMove().getRejectOk())  {
			for(MoveLine moveLine : move.getMoveLineList())  {
				if(moveLine.getAccount().getReconcileOk())  {
					LOG.debug("Creation du lettrage de la ligne 411 de rejet avec la ligne 411 de paiement");
					rcs.reconcile(moveLineToPay, moveLine);
					break;
				}
			}
		}
		
		ms.validateMove(move);
		psl.setAdvanceOrPaymentMove(move);
	}
	
	
	public PaymentMode getPaymentMode(String code)  {
		return PaymentMode.all().filter("self.code = ?1", code).fetchOne();
	}
	
	
	/**
	 * Méthode permettant de récupérer le mode de paiement par Prélèvement
	 * @return
	 * 			Le mode de paiement par prélèvement
	 */
	public PaymentMode getDebitPaymentMode()  {
		return this.getPaymentMode("DD");
	}
		
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move validateMove(Move move) throws AxelorException  {
		ms.validateMove(move);
		move.save();
		return move;
	}
	
	
	
}