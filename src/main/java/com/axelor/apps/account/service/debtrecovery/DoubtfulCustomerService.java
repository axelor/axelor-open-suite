package com.axelor.apps.account.service.debtrecovery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DoubtfulCustomerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DoubtfulCustomerService.class); 
		
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private ReconcileService rs;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private UserInfoService uis;
	
	private LocalDate today;

	@Inject
	public DoubtfulCustomerService() {

		this.today = GeneralService.getTodayDate();
	}
	
	
	/**
	 * Procédure permettant de vérifier le remplissage des champs dans la société, nécessaire au traitement du passage en client douteux
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		if(company.getDoubtfulCustomerAccount() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un compte client douteux pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getSixMonthDebtPassReason() == null || company.getSixMonthDebtPassReason().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Motif de passage (créance de plus de six mois) pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getThreeMonthDebtPassReason() == null || company.getThreeMonthDebtPassReason().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Motif de passage (créance de plus de trois mois) pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getLitigationDebtPassReason() == null || company.getLitigationDebtPassReason().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Motif de passage (contentieux) pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getSixMonthDebtMonthNumber() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un nombre de mois pris en compte pour les créances de plus de six mois pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getSixMonthDebtMonthNumber() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un nombre de mois pris en compte pour les créances de plus de trois mois pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getMiscOperationJournal() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal des O.D. pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * 
	 * Procédure permettant de créer les écritures de passage en client douteux pour chaque écriture de facture
	 * @param moveLineList
	 * 			Une liste d'écritures de facture
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 * @throws AxelorException
	 */
	public void createDoubtFulCustomerMove(List<Move> moveList, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException  {
		
		for(Move move : moveList)  {
		
			this.createDoubtFulCustomerMove(move, doubtfulCustomerAccount, debtPassReason);
			
		}
	}
	
	
	/**
	 * 
	 * Procédure permettant de créer les écritures de passage en client douteux pour chaque écriture de facture
	 * @param moveLineList
	 * 			Une liste d'écritures de facture
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class}) 
	public void createDoubtFulCustomerMove(Move move, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException  {
		
		LOG.debug("Ecriture concernée : {} ",move.getReference());
		
		BigDecimal totalAmountRemaining = BigDecimal.ZERO;
		Company company = move.getCompany();
		Partner partner = move.getPartner();
		Move newMove = ms.createMove(company.getMiscOperationJournal(), company, move.getInvoice(), partner, move.getPaymentMode(), false);

		int ref = 1;
		List<Reconcile> reconcileList = new ArrayList<Reconcile>();
		List<MoveLine> moveLineList = move.getMoveLineList();
		for(MoveLine moveLine : moveLineList)  {
			if(moveLine.getAccount().getReconcileOk() 
					&& moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
					&& moveLine.getAccount() != doubtfulCustomerAccount
					&& moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
					
					BigDecimal amountRemaining = moveLine.getAmountRemaining();
					// Ecriture au crédit sur le 411
					MoveLine creditMoveLine = mls.createMoveLine(newMove , moveLine.getPartner(), moveLine.getAccount(), amountRemaining, false, false, 
							today, ref, false, false, false, null);
					newMove.getMoveLineList().add(creditMoveLine);
					
					Reconcile reconcile = rs.createReconcile(moveLine, creditMoveLine, amountRemaining);
					reconcileList.add(reconcile);
					
					totalAmountRemaining = totalAmountRemaining.add(amountRemaining);
					
					ref++;
			}
		}
		
		// Ecriture au débit sur le 416 (client douteux)
		MoveLine debitMoveLine = mls.createMoveLine(newMove , newMove.getPartner(), doubtfulCustomerAccount, totalAmountRemaining, true, false, 
				today, ref, false, false, false, null);
		newMove.getMoveLineList().add(debitMoveLine);
		
		debitMoveLine.setPassageReason(debtPassReason);
		
		ms.validateMove(newMove);
		newMove.save();
		
		for(Reconcile reconcile : reconcileList)  {
			rs.confirmReconcile(reconcile, false);
		}

		Invoice invoice = this.invoiceProcess(newMove, doubtfulCustomerAccount, debtPassReason);
		
		// Création d'un évènement
//		ActionEvent actionEvent = aes.createActionEvent("Passage en client douteux : "+debtPassReason, this.today, partner, invoice);
//		actionEvent.save();
	}
	
	
	
	public void createDoubtFulCustomerRejectMove(List<MoveLine> moveLineList, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException  {
		
		for(MoveLine moveLine : moveLineList)  {
			
			this.createDoubtFulCustomerRejectMove(moveLine, doubtfulCustomerAccount, debtPassReason);
			
		}
	}
	
	
	/**
	 * Procédure permettant de créer les écritures de passage en client douteux pour chaque ligne d'écriture de rejet de facture
	 * @param moveLineList
	 * 			Une liste de lignes d'écritures de rejet de facture
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class}) 
	public void createDoubtFulCustomerRejectMove(MoveLine moveLine, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException  {
		
		LOG.debug("Ecriture concernée : {} ",moveLine.getName());
		Company company = moveLine.getMove().getCompany();
		Partner partner = moveLine.getPartner();
		
		Move newMove = ms.createMove(company.getMiscOperationJournal(), company, null, partner, moveLine.getMove().getPaymentMode(), false);

		List<Reconcile> reconcileList = new ArrayList<Reconcile>();

		BigDecimal amountRemaining = moveLine.getAmountRemaining();
		
		// Ecriture au crédit sur le 411
		MoveLine creditMoveLine = mls.createMoveLine(newMove , partner, moveLine.getAccount(), amountRemaining, false, false, 
				today, 1, false, false, false, null);
		newMove.getMoveLineList().add(creditMoveLine);
		
		Reconcile reconcile = rs.createReconcile(moveLine, creditMoveLine, amountRemaining);
		reconcileList.add(reconcile);
		rs.confirmReconcile(reconcile, false);
		
		// Ecriture au débit sur le 416 (client douteux)
		MoveLine debitMoveLine = mls.createMoveLine(newMove , newMove.getPartner(), doubtfulCustomerAccount, amountRemaining, true, false, 
				today, 2, false, false, false, null);
		newMove.getMoveLineList().add(debitMoveLine);
		
		debitMoveLine.setInvoiceReject(moveLine.getInvoiceReject());
		debitMoveLine.setPassageReason(debtPassReason);
		
		ms.validateMove(newMove);
		newMove.save();

		Invoice invoice = this.invoiceRejectProcess(debitMoveLine, doubtfulCustomerAccount, debtPassReason);
		
		// Création d'un évènement
//		ActionEvent actionEvent = aes.createActionEvent("Passage en client douteux : "+debtPassReason, this.today, partner, invoice);
//		actionEvent.save();
	}
	
	
	
	/**
	 * Procédure permettant de mettre à jour le motif de passage en client douteux, et créer l'évènement lié.
	 * @param moveList
	 * 			Une liste d'éciture de facture
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 */
	public void updateDoubtfulCustomerMove(List<Move> moveList, Account doubtfulCustomerAccount, String debtPassReason)  {
		
		for(Move move : moveList)  {
			
			for(MoveLine moveLine : move.getMoveLineList())  {
				
				if(moveLine.getAccount().equals(doubtfulCustomerAccount) && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
					
					moveLine.setPassageReason(debtPassReason);
					moveLine.save();
					
					// Création d'un évènement
//					ActionEvent actionEvent = aes.createActionEvent("Passage en client douteux : "+debtPassReason, this.today, moveLine.getPartner(), move.getInvoice());
//					actionEvent.save();
					
					break;
				}
			}
		}
	}
	
	
	/**
	 * Procédure permettant de mettre à jour les champs de la facture avec la nouvelle écriture de débit sur le compte 416
	 * @param move
	 * 			La nouvelle écriture de débit sur le compte 416
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 * @throws AxelorException 
	 */
	public Invoice invoiceProcess(Move move, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException  {
		
		Invoice invoice = move.getInvoice();
		
		if(invoice != null)  {
			
			invoice.setOldMove(invoice.getMove());
			invoice.setMove(move);
			invoice.setPartnerAccount(doubtfulCustomerAccount);
			invoice.setDoubtfulCustomerOk(true);
			// Recalcule du restant à payer de la facture
			invoice.setInTaxTotalRemaining(ms.getInTaxTotalRemaining(invoice, doubtfulCustomerAccount));
		}
		return invoice;
	}
	
	
	/**
	 * Procédure permettant de mettre à jour les champs d'une facture rejetée avec la nouvelle écriture de débit sur le compte 416
	 * @param move
	 * 			La nouvelle ligne d'écriture de débit sur le compte 416
	 * @param doubtfulCustomerAccount
	 * 			Un compte client douteux
	 * @param debtPassReason
	 * 			Un motif de passage en client douteux
	 */
	public Invoice invoiceRejectProcess(MoveLine moveLine, Account doubtfulCustomerAccount, String debtPassReason)  {
		
		Invoice invoice = moveLine.getInvoiceReject();
		
		invoice.setRejectMoveLine(moveLine);
//			invoice.setPartnerAccount(doubtfulCustomerAccount);
		invoice.setDoubtfulCustomerOk(true);
		
		return invoice;
	}
	
	
	/**
	 * Fonction permettant de récupérer les écritures de facture à transférer sur le compte client douteux
	 * @param rule
	 * 		Le règle à appliquer :
	 * 		<ul>
     *      <li>0 = Créance de + 6 mois</li>
     *      <li>1 = Créance de + 3 mois sur contrat résilié</li>
     *  	</ul>
	 * @param doubtfulCustomerAccount
	 * 		Le compte client douteux
	 * @param company
	 * 		La société
	 * @return
	 * 		Les écritures de facture à transférer sur le compte client douteux
	 */
	public List<Move> getMove(int rule, Account doubtfulCustomerAccount, Company company)  {
		
		LocalDate date = null;
		String complementaryRequest = " ";
		
		switch (rule) {
		
			//Créance de + 6 mois			
			case 0 :
				date = this.today.minusMonths(company.getSixMonthDebtMonthNumber());
				break;
	
			//Créance de + 3 mois sur contrat résilié	
			case 1 : 
				date = this.today.minusMonths(company.getThreeMonthDebtMontsNumber());
				complementaryRequest = " AND ml.contractLine.status.code ='res' ";
				break;
				
			default:
				break;
		}
		
		LOG.debug("Date de créance prise en compte : {} ",date);

		String request = "SELECT DISTINCT m FROM MoveLine ml, Move m WHERE ml.move = m AND ml.company.id = "+ company.getId() +" AND ml.account.reconcileOk = 'true' " +
				"AND ml.invoice IS NOT NULL AND ml.amountRemaining > 0.00 AND ml.debit > 0.00 AND ml.dueDate < '"+ date.toString() + 
				"'" + complementaryRequest +" AND ml.account.id != "+doubtfulCustomerAccount.getId();
		
		LOG.debug("Requete : {} ",request);
		
		Query query = JPA.em().createQuery(request);
		
		@SuppressWarnings("unchecked")
		List<Move> moveList = query.getResultList();
		
		return moveList;
	}
	
	/**
	 * Fonction permettant de récupérer les lignes d'écriture de rejet de facture à transférer sur le compte client douteux 
	 * @param rule
	 * 		Le règle à appliquer :
	 * 		<ul>
     *      <li>0 = Créance de + 6 mois</li>
     *      <li>1 = Créance de + 3 mois sur contrat résilié</li>
     *  	</ul>
	 * @param doubtfulCustomerAccount
	 * 		Le compte client douteux
	 * @param company
	 * 		La société
	 * @return
	 * 		Les lignes d'écriture de rejet de facture à transférer sur le comtpe client douteux
	 */
	public List<MoveLine> getRejectMoveLine(int rule, Account doubtfulCustomerAccount, Company company)  {
		
		LocalDate date = null;
		List<MoveLine> moveLineList = null;
		
		switch (rule) {
		
			//Créance de + 6 mois			
			case 0 :
				date = this.today.minusMonths(company.getSixMonthDebtMonthNumber());
				moveLineList = MoveLine.all().filter("self.company = ?1 AND self.account.reconcileOk = 'true' " +
						"AND self.invoiceReject IS NOT NULL AND self.amountRemaining > 0.00 AND self.debit > 0.00 AND self.dueDate < ?2" +
						" AND self.account != ?3",company, date, doubtfulCustomerAccount).fetch();
				break;
	
			//Créance de + 3 mois sur contrat résilié	
			case 1 : 
				date = this.today.minusMonths(company.getThreeMonthDebtMontsNumber());
				moveLineList = MoveLine.all().filter("self.company = ?1 AND self.account.reconcileOk = 'true' " +
						"AND self.invoiceReject IS NOT NULL AND self.amountRemaining > 0.00 AND self.debit > 0.00 AND self.dueDate < ?2" +
						" AND self.contractLine.status.code ='res' AND self.account != ?3",company, date, doubtfulCustomerAccount).fetch();
				break;
				
			default:
				break;
		}
		
		LOG.debug("Date de créance prise en compte : {} ",date);
		
		return moveLineList;
	}
	
	
	/**
	 * Procédure permettant de traiter les échéanciers de surrendettement, liquidation judiciaire, redressement judiciare, et passage à l'huissier
	 * Les écritures de factures selectionner sur l'échéancier seront passées sur le compte client douteux
	 * 
	 * @param paymentSchedule
	 * 			Un échéancier de surrendettement, liquidation judiciaire, redressement judiciare, ou passage à l'huissier
	 * @throws AxelorException
	 */
	public void doubtfulCustomerProcess(PaymentSchedule paymentSchedule) throws AxelorException  {
		
		ArrayList<Move> moveList = new ArrayList<Move>();
		ArrayList<Move> moveListToUpdate = new ArrayList<Move>();
		
		for(Invoice invoice : paymentSchedule.getInvoiceSet())  {
			if(invoice.getOldMove() != null)  {
				moveListToUpdate.add(invoice.getMove());
			}
			else  {
				moveList.add(invoice.getMove());
			}
		}
		
		Company company = paymentSchedule.getCompany();
		this.testCompanyField(company);
		
		Account doubtfulCustomerAccount = company.getDoubtfulCustomerAccount();
		String litigationDebtPassReason = company.getLitigationDebtPassReason();

		this.createDoubtFulCustomerMove(moveList, doubtfulCustomerAccount, litigationDebtPassReason);
		this.updateDoubtfulCustomerMove(moveListToUpdate, doubtfulCustomerAccount, litigationDebtPassReason);
	}
	
	
}
