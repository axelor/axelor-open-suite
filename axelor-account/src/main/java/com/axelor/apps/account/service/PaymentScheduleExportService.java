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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.DirectDebitManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.DirectDebitManagementRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.cfonb.CfonbExportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleExportService{

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveService moveService;
	protected MoveRepository moveRepo;
	protected MoveLineService moveLineServices;
	protected MoveLineRepository moveLineRepo;
	protected ReconcileService reconcileService;
	protected SequenceService sequenceService;
	protected PaymentModeService paymentModeService;
	protected CfonbExportService cfonbExportService;
	protected PaymentService paymentService;
	protected BlockingService blockingService;
	protected AccountConfigService accountConfigService;
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;
	protected DirectDebitManagementRepository directDebitManagementRepo;
	protected InvoiceService invoiceService;
	protected InvoiceRepository invoiceRepo;
	protected PartnerService  partnerService;
	protected LocalDate today;
	protected boolean sepa;
	

	@Inject
	public PaymentScheduleExportService(MoveService moveService, MoveRepository moveRepo, MoveLineService moveLineServices, MoveLineRepository moveLineRepo, ReconcileService reconcileService,
			SequenceService sequenceService, PaymentModeService paymentModeService, CfonbExportService cfonbExportService, PaymentService paymentService,
			BlockingService blockingService, AccountConfigService accountConfigService, PaymentScheduleLineRepository paymentScheduleLineRepo,
			DirectDebitManagementRepository directDebitManagementRepo, InvoiceService invoiceService, InvoiceRepository invoiceRepo, GeneralService generalService, PartnerService partnerService) {
		
		this.moveService = moveService;
		this.moveRepo = moveRepo;
		this.moveLineServices = moveLineServices;
		this.moveLineRepo = moveLineRepo;
		this.reconcileService = reconcileService;
		this.sequenceService = sequenceService;
		this.paymentModeService = paymentModeService;
		this.cfonbExportService = cfonbExportService;
		this.paymentService = paymentService;
		this.blockingService = blockingService;
		this.accountConfigService = accountConfigService;
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		this.directDebitManagementRepo = directDebitManagementRepo;
		this.invoiceService = invoiceService;
		this.invoiceRepo = invoiceRepo;
		this.partnerService = partnerService;

		this.today = generalService.getTodayDate();

	}

	public void setSepa(boolean sepa)  {

		this.sepa = sepa;

	}



	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteMove(Move move) throws AxelorException  {
		moveRepo.remove(move);
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createOppositeExportMensuMoveLine(Move move, Account bankAccount, int ref) throws AxelorException  {
		log.debug("Montant de la contrepartie : {}", totalAmount(move));

		MoveLine moveLine = moveLineServices.createMoveLine(move, null, bankAccount, this.totalAmount(move), true, today, ref, null);

		move.getMoveLineList().add(moveLine);
		moveLineRepo.save(moveLine);
		return move;
	}


	public void testBankDetails(PaymentSchedule paymentSchedule) throws AxelorException  {
		Partner partner = paymentSchedule.getPartner();
		BankDetails bankDetails = paymentSchedule.getBankDetails();
		if(bankDetails == null)  {
			bankDetails = partnerService.getDefaultBankDetails(partner);
		}
		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_1),
					GeneralServiceImpl.EXCEPTION,paymentSchedule.getScheduleId()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
		}
	}


	public void testBankDetails(Invoice invoice) throws AxelorException  {
		BankDetails bankDetails = partnerService.getDefaultBankDetails(invoice.getPartner());

		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_2),
					GeneralServiceImpl.EXCEPTION, invoice.getPartner().getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
		}
	}


	public void testBankDetails(Partner partner) throws AxelorException  {
		BankDetails bankDetails = partnerService.getDefaultBankDetails(partner);

		if(bankDetails == null) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_2),
					GeneralServiceImpl.EXCEPTION, partner.getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			cfonbExportService.testBankDetailsField(bankDetails);
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
					I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_3)
					,GeneralServiceImpl.EXCEPTION,accountingBatch.getCode()), IException.CONFIGURATION_ERROR);
		}
	}


	public void checkInvoiceExportCompany(Company company) throws AxelorException  {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		accountConfigService.getCustomerAccount(accountConfig);
		accountConfigService.getDirectDebitPaymentMode(accountConfig);
	}


	public void checkMonthlyExportCompany(Company company) throws AxelorException  {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		accountConfigService.getCustomerAccount(accountConfig);
		accountConfigService.getDirectDebitPaymentMode(accountConfig);
	}


	/**
	 * Méthode permettant de retrouver l'échéance rejetée qui à impliquer la création de la nouvelle échéance
	 * @param paymentScheduleLine
	 * 			La nouvelle échéance
	 */
	public PaymentScheduleLine getPaymentScheduleLineRejectOrigin(PaymentScheduleLine paymentScheduleLine)  {

		return paymentScheduleLineRepo.all()
				.filter("self.paymentSchedule = ?1 AND self.scheduleLineSeq = ?2 AND self.statusSelect = ?3 ORDER BY self.rejectDate DESC"
						, paymentScheduleLine.getPaymentSchedule(), paymentScheduleLine.getScheduleLineSeq(), PaymentScheduleLineRepository.STATUS_CLOSED).fetchOne();

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
		log.debug("Montant total : {}", total);

		return total;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentScheduleLine generateExportMensu (PaymentScheduleLine paymentScheduleLine, List<PaymentScheduleLine> paymentScheduleLineList, Company company) throws AxelorException  {

		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();

		this.testBankDetails(paymentSchedule);

		AccountConfig accountConfig = company.getAccountConfig();

		Account account = accountConfig.getCustomerAccount();
		PaymentMode paymentMode = accountConfig.getDirectDebitPaymentMode();

		BigDecimal amount =  paymentScheduleLine.getInTaxAmount();
		Partner partner = paymentSchedule.getPartner();

		Move move = moveService.getMoveCreateService().createMove(paymentModeService.getPaymentModeJournal(paymentMode, company), company, null, partner, paymentMode);

		this.setDebitNumber(paymentScheduleLineList, paymentScheduleLine, company);

		MoveLine moveLine = moveLineRepo.save(moveLineServices.createMoveLine(move , partner, account, amount, false, today, 1, paymentScheduleLine.getName()));

		move.addMoveLineListItem(moveLine);

		if(paymentScheduleLine.getFromReject()) {
			// lettrage avec le rejet
			PaymentScheduleLine rejectedPaymentScheduleLine = this.getPaymentScheduleLineRejectOrigin(paymentScheduleLine);
			if(rejectedPaymentScheduleLine.getRejectMoveLine() != null
					&& rejectedPaymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) == 1)  {
				reconcileService.reconcile(rejectedPaymentScheduleLine.getRejectMoveLine(), moveLine, false);
			}
		}
		else  {
			// Lettrage du paiement avec les factures d'échéances
			this.reconcileDirectDebit(moveLine, paymentSchedule);
		}

		move.addMoveLineListItem(
				moveLineServices.createMoveLine(move, partner,	paymentModeService.getPaymentModeAccount(paymentMode, company), amount, true, today, 2, null));

		this.validateMove(move);

		paymentScheduleLine.setDirectDebitAmount(amount);
		paymentScheduleLine.setInTaxAmountPaid(amount);
		paymentScheduleLine.setAdvanceOrPaymentMove(moveRepo.find(move.getId()));
		paymentScheduleLine.setAdvanceMoveLine(moveLine);
		paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_VALIDATED);
		return paymentScheduleLineRepo.save(paymentScheduleLine);
	}



	/**
	 * Procédure permettant de lettrer l'écriture de paiement avec les écritures des factures d'échéance de lissage de paiement du tiers
	 * @param creditMoveLine
	 * 			Une écriture de paiement par prélèvement d'une échéance
	 * @param paymentSchedule
	 * 			Un échéancier
	 *
	 * @throws AxelorException
	 */
	public void reconcileDirectDebit(MoveLine creditMoveLine, PaymentSchedule paymentSchedule) throws AxelorException  {
		List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
		creditMoveLineList.add(creditMoveLine);
		paymentService.useExcessPaymentOnMoveLines(this.getInvoiceMoveLineListToReconcile(paymentSchedule), creditMoveLineList);
	}


	/**
	 * Méthode permettant de récupérer les factures d'échéance mensu grand compte d'un échéancier
	 * @param paymentSchedule
	 * 			Un échéancier
	 * @return
	 */
	public List<MoveLine> getInvoiceMoveLineListToReconcile(PaymentSchedule paymentSchedule)  {
		return moveLineRepo.all()
				.filter("self.move.statusSelect = ?1 AND self.exportedDirectDebitOk = 'false' " +
						"AND self.account.reconcileOk = ?2 AND self.amountRemaining > 0 " +
						"AND self.move.invoice.operationTypeSelect = ?3 " +
						"AND self.move.invoice.schedulePaymentOk = 'true' " +
						"AND self.move.invoice.paymentSchedule = ?4 "+
						"ORDER BY self.date", MoveRepository.STATUS_VALIDATED, true, InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, paymentSchedule).fetch();
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
	public void setDebitNumber(List<PaymentScheduleLine> paymentScheduleLineList, PaymentScheduleLine paymentScheduleLine, Company company) throws AxelorException  {
		if(hasOtherPaymentScheduleLine(paymentScheduleLineList, paymentScheduleLine))  {
			DirectDebitManagement directDebitManagement = this.getDirectDebitManagement(paymentScheduleLineList, paymentScheduleLine);
			if(directDebitManagement == null)  {
				directDebitManagement = this.createDirectDebitManagement(this.getDirectDebitSequence(company), company);
			}
			paymentScheduleLine.setDirectDebitManagement(directDebitManagement);
			directDebitManagement.getPaymentScheduleLineList().add(paymentScheduleLine);
		}
		else  {
			paymentScheduleLine.setDebitNumber(this.getDirectDebitSequence(company));
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
	public List<PaymentScheduleLine> getPaymentScheduleLineToDebit(AccountingBatch accountingBatch)  {

		Company company = accountingBatch.getCompany();
		LocalDate debitDate = accountingBatch.getDebitDate();
		Currency currency = accountingBatch.getCurrency();

		PaymentMode paymentMode = company.getAccountConfig().getDirectDebitPaymentMode();

		List<PaymentScheduleLine> paymentScheduleLineList = paymentScheduleLineRepo.all()
				.filter("self.statusSelect = ?1 AND self.paymentSchedule.statusSelect = ?2 AND self.paymentSchedule.company = ?3 " +
						"AND self.scheduleDate <= ?4 " +
						"AND self.debitBlockingOk IN ('false',null) " +
						"AND self.paymentSchedule.currency = ?5 " +
						"AND self.paymentSchedule.paymentMode = ?6 ORDER BY self.scheduleDate",
						PaymentScheduleLineRepository.STATUS_IN_PROGRESS, PaymentScheduleRepository.STATUS_CONFIRMED, company, debitDate, currency, paymentMode).fetch();

		if(paymentScheduleLineList.size() < 50)  {
			log.debug("\n Liste des échéances retenues : {} \n", this.toStringPaymentScheduleLineList(paymentScheduleLineList));
		}
		else  {
			log.debug("\n Nombres échéances retenues : {} \n", paymentScheduleLineList.size());
		}

		return paymentScheduleLineList;
	}



	public boolean isDebitBlocking(PaymentScheduleLine paymentScheduleLine)  {

		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();

		return blockingService.isDebitBlockingBlocking(paymentSchedule.getPartner(), paymentSchedule.getCompany());

	}


	public boolean isDebitBlocking(Invoice invoice)  {

		return blockingService.isDebitBlockingBlocking(invoice.getPartner(), invoice.getCompany());

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
	public void checkDirectDebitSequence(Company company) throws AxelorException  {

		PaymentMode directDebitPaymentMode = company.getAccountConfig().getDirectDebitPaymentMode();

		paymentModeService.getPaymentModeSequence(directDebitPaymentMode, company);

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
			paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
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
	public boolean hasOtherInvoice(List<MoveLine> moveLineList, MoveLine moveLine)  {

		Partner partner = moveLine.getPartner();

		Query q = JPA.em().createQuery("select count(*) FROM MoveLine as self WHERE self IN ?1 AND self.partner = ?2 ");
		q.setParameter(1, moveLineList);
		q.setParameter(2, partner);

		if((long) q.getSingleResult() > 1)  {
			log.debug("Recherche d'une autre facture à prélever (autre que l'écriture {}) pour le tiers {} : OUI", moveLine.getName(), partner.getFullName());
			return true;
		}

		log.debug("Recherche d'une autre facture à prélever (autre que l'écriture {}) pour le tiers {} : NON", moveLine.getName(), partner.getFullName());

		return false;
	}


	/**
	 * Méthode permettant de récupérer le dernier 'id' utilisé pour l'objet de gestion des prélèvements afin de pouvoir
	 * exclure les numéros de prélèvements consolidés, rejetés, lors du prochain prélèvement.
	 * @return
	 */
	public long getDirectDevitManagementMaxId()  {

		Query q = JPA.em().createQuery("select MAX(id) FROM DirectDebitManagement");

		Object result = q.getSingleResult();

		if(result != null)  {
			return (long) result;
		}
		return 0;

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
			paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
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
	public DirectDebitManagement getDirectDebitManagement(List<MoveLine> moveLineList, MoveLine ml, long directDebitManagementMaxId)  {

		Partner partner = ml.getPartner();

		log.debug("Récupération de l'objet de prélèvement du tiers {}", partner.getFullName());

		List<MoveLine> moveLineListResult = moveLineRepo.all().filter("self IN (?1) and self.partner = ?2", moveLineList, partner).fetch();

		for(MoveLine moveLine : moveLineListResult)  {
			Invoice invoice = cfonbExportService.getInvoice(moveLine);

			DirectDebitManagement directDebitManagement = invoice.getDirectDebitManagement();
			if(directDebitManagement != null && directDebitManagement.getId() > directDebitManagementMaxId)  {

				log.debug("Objet de prélèvement trouvé : {} pour le tiers {}", partner.getFullName());
				return invoice.getDirectDebitManagement();
			}
		}

		log.debug("Aucun objet de prélèvement trouvé pour le tiers {}", partner.getFullName());

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
	public Invoice exportInvoice(MoveLine moveLine, List<MoveLine> moveLineList, Company company, long directDebitManagementMaxId) throws AxelorException  {

		/** Important : Doit être executé avant la méthode 'createPaymentMove()' afin de récupérer le montant restant à prélever **/
		BigDecimal amountExported = moveLine.getAmountRemaining();

		this.testBankDetails(moveLine.getPartner());

		// creation d'une ecriture de paiement
		Invoice invoice = this.updateInvoice(moveLine,
				this.createPaymentMove(company, moveLine, company.getAccountConfig().getDirectDebitPaymentMode()),
				moveLineList,
				amountExported,
				directDebitManagementMaxId);

		invoiceRepo.save(invoice);

		return invoice;
	}



	public List<MoveLine> getInvoiceToExport(Company company, LocalDate scheduleDate, Currency currency)  {

		List<MoveLine>  moveLineInvoiceList = new ArrayList<MoveLine>();

		PaymentMode paymentMode = company.getAccountConfig().getDirectDebitPaymentMode();

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
		 */
		List<MoveLine> moveLineList = moveLineRepo.all()
				.filter("self.move.statusSelect = ?1 AND self.exportedDirectDebitOk = 'false' " +
						"AND self.move.company = ?2 " +
						"AND self.account.reconcileOk = ?3 AND self.amountRemaining > 0 " +
						"AND self.debit > 0 " +
						"AND self.dueDate <= ?5 AND self.move.invoice IS NOT NULL " +
						"AND self.move.invoice.paymentMode = ?4 " +
						"AND self.move.invoice.schedulePaymentOk = 'false' " +
						"AND self.move.invoice.currency = ?5"
						, MoveRepository.STATUS_VALIDATED, company, true, paymentMode, currency).fetch();


		// Ajout des factures
		for(MoveLine moveLine : moveLineList)  {
			if(!this.isDebitBlocking(moveLine.getMove().getInvoice()))  {
				moveLineInvoiceList.add(moveLine);
			}
		}


		// Récupération des factures rejetées
		List<Invoice> invoiceRejectList = invoiceRepo.all()
				.filter("self.rejectMoveLine IS NOT NULL AND self.rejectMoveLine.amountRemaining > 0 AND self.rejectMoveLine.debit > 0" +
						" AND self.paymentMode = ?1 AND self.company = ?2 AND self.rejectMoveLine.exportedDirectDebitOk = 'false' AND self.move.statusSelect = ?3" +
						" AND self.rejectMoveLine.account.reconcileOk = 'true' " +
						" AND self.rejectMoveLine.invoiceReject IS NOT NULL" +
						" AND self.currency = ?4"
						, paymentMode, company, MoveRepository.STATUS_VALIDATED, currency).fetch();

		// Ajout des factures rejetées
		for(Invoice invoice : invoiceRejectList)  {

			if(!this.isDebitBlocking(invoice))  {
				moveLineInvoiceList.add(invoice.getRejectMoveLine());
			}
		}

		return moveLineInvoiceList;
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
	public Move createPaymentMove(Company company, MoveLine moveLine, PaymentMode paymentMode) throws AxelorException  {

		log.debug("Create payment move");

		Move paymentMove = moveService.getMoveCreateService().createMove(
				paymentModeService.getPaymentModeJournal(paymentMode, company), company, null, null, paymentMode);

		BigDecimal amountExported = moveLine.getAmountRemaining();

		this.createPaymentMoveLine(paymentMove, moveLine, 1);

		log.debug("Create payment move line");

		Account paymentModeAccount = paymentModeService.getPaymentModeAccount(paymentMode, company);

		String invoiceName = "";
		if(moveLine.getMove().getInvoice()!=null)  {
			invoiceName = moveLine.getMove().getInvoice().getInvoiceId();
		}
		MoveLine moveLineGenerated2 = moveLineServices.createMoveLine(paymentMove, null, paymentModeAccount, amountExported,
				true, today, 2, invoiceName);

		paymentMove.getMoveLineList().add(moveLineGenerated2);
		moveLineRepo.save(moveLineGenerated2);

		moveService.getMoveValidateService().validateMove(paymentMove);
		moveRepo.save(paymentMove);

		return paymentMove;
	}


	public void createPaymentMoveLine(Move paymentMove, MoveLine moveLine, int ref) throws AxelorException  {
		BigDecimal amountExported = moveLine.getAmountRemaining();

		// On assigne le montant exporté pour pouvoir l'utiliser lors de la création du fichier d'export CFONB
		moveLine.setAmountExportedInDirectDebit(amountExported);

		// creation d'une ecriture de paiement

		log.debug("generateAllExportInvoice - Création de la première ligne d'écriture");
		String invoiceName = "";
		if(moveLine.getMove().getInvoice()!=null)  {
			invoiceName = moveLine.getMove().getInvoice().getInvoiceId();
		}
		MoveLine moveLineGenerated = moveLineServices.createMoveLine(paymentMove, moveLine.getPartner(), moveLine.getAccount(),
				amountExported, false, today, ref, invoiceName);

		paymentMove.getMoveLineList().add(moveLineGenerated);

		moveLineRepo.save(moveLineGenerated);

		// Lettrage de la ligne 411 avec la ligne 411 de la facture
		log.debug("Creation du lettrage de la ligne 411 avec la ligne 411 de la facture");

		reconcileService.reconcile(moveLine, moveLineGenerated, false);

		log.debug("generateAllExportInvoice - Sauvegarde de l'écriture");

		moveRepo.save(paymentMove);

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
	public Invoice updateInvoice(MoveLine moveLine, Move paymentMove, List<MoveLine> mlList, BigDecimal amountExported, long directDebitManagementMaxId) throws AxelorException  {

		Invoice invoice = cfonbExportService.getInvoice(moveLine);

		this.testBankDetails(invoice);

		Company company = invoice.getCompany();

		// Mise à jour du champ 'Ecriture de paiement' sur la facture
		log.debug("generateAllExportInvoice - Mise à jour du champ 'Ecriture de paiement' sur la facture");
		invoice.setPaymentMove(paymentMove);

		// Mise à jour du montant prélever
		invoice.setDirectDebitAmount(amountExported);

		// Mise à jour du Numéro de prélèvement sur la facture
		log.debug("Mise à jour du Numéro de prélèvement sur la facture");

		if(this.hasOtherInvoice(mlList, moveLine))  {
			DirectDebitManagement directDebitManagement = this.getDirectDebitManagement(mlList, moveLine, directDebitManagementMaxId);
			if(directDebitManagement == null)  {
				directDebitManagement = this.createDirectDebitManagement(this.getDirectDebitSequence(company), company);
			}
			invoice.setDirectDebitManagement(directDebitManagement);
			invoice.setDebitNumber(null);
			directDebitManagement.getInvoiceSet().add(invoice);
			directDebitManagementRepo.save(directDebitManagement);
		}
		else  {
			invoice.setDebitNumber(this.getDirectDebitSequence(company));
			invoice.setDirectDebitManagement(null);
		}
		return invoice;
	}


	public String getDirectDebitSequence(Company company) throws AxelorException  {

		PaymentMode directDebitPaymentMode = company.getAccountConfig().getDirectDebitPaymentMode();

		return sequenceService.getSequenceNumber(
				paymentModeService.getPaymentModeSequence(directDebitPaymentMode, company));

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move validateMove(Move move) throws AxelorException  {
		moveService.getMoveValidateService().validateMove(move);
		moveRepo.save(move);
		return move;
	}



}