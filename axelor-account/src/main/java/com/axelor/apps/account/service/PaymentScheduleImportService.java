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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.DirectDebitManagement;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
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
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentScheduleImportService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveLineService moveLineService;
	protected MoveLineRepository moveLineRepo;
	protected MoveService moveService;
	protected MoveRepository moveRepo;
	protected PaymentScheduleService paymentScheduleService;
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;
	protected PaymentModeService paymentModeService;
	protected CfonbImportService cfonbImportService;
	protected ReminderService reminderService;
	protected AccountConfigService accountConfigService;
	protected DirectDebitManagementRepository directDebitManagementRepo;
	protected InvoiceRepository invoiceRepo;

	protected LocalDate today;

	private List<PaymentScheduleLine> pslListGC = new ArrayList<PaymentScheduleLine>();   				// liste des échéances de lissage de paiement rejetées
	private List<Invoice> invoiceList = new ArrayList<Invoice>();										// liste des factures rejetées

	@Inject
	public PaymentScheduleImportService(GeneralService generalService, MoveLineService moveLineService, MoveService moveService, MoveRepository moveRepo,
			PaymentScheduleService paymentScheduleService, PaymentScheduleLineRepository paymentScheduleLineRepo, PaymentModeService paymentModeService,
			CfonbImportService cfonbImportService, ReminderService reminderService, AccountConfigService accountConfigService, DirectDebitManagementRepository directDebitManagementRepo,
			InvoiceRepository invoiceRepo) {

		this.moveLineService = moveLineService;
		this.moveService = moveService;
		this.moveRepo = moveRepo;
		this.paymentScheduleService = paymentScheduleService;
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		this.paymentModeService = paymentModeService;
		this.cfonbImportService = cfonbImportService;
		this.reminderService = reminderService;
		this.accountConfigService = accountConfigService;
		this.directDebitManagementRepo = directDebitManagementRepo;
		this.invoiceRepo = invoiceRepo;
		this.today = generalService.getTodayDate();

	}

	public List<PaymentScheduleLine> getPaymentScheduleLineMajorAccountList()  {
		return this.pslListGC;
	}

	public List<Invoice> getInvoiceList()  {
		return this.invoiceList;
	}


	public void initialiseCollection()  {

		pslListGC = new ArrayList<PaymentScheduleLine>();
		invoiceList = new ArrayList<Invoice>();

	}


	public void checkCompanyFields(Company company) throws AxelorException  {
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		// Test si les champs d'import sont configurés pour la société
		accountConfigService.getRejectImportPathAndFileName(accountConfig);
		accountConfigService.getTempImportPathAndFileName(accountConfig);
		accountConfigService.getRejectPaymentScheduleTemplate(accountConfig);
		accountConfigService.getCustomerAccount(accountConfig);
		accountConfigService.getRejectionPaymentMode(accountConfig);

		Journal rejectJournal = accountConfigService.getRejectJournal(accountConfig);

		if(rejectJournal.getSequence() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PAYMENT_SCHEDULE_4),
					GeneralServiceImpl.EXCEPTION, company.getName(), rejectJournal.getName()), IException.CONFIGURATION_ERROR);
		}
	}


	public String getReferenceRejected(PaymentScheduleLine paymentScheduleLine)  {
		if(paymentScheduleLine.getDirectDebitManagement() != null)  {
			return paymentScheduleLine.getDirectDebitManagement().getDebitNumber();
		}
		else  {
			return paymentScheduleLine.getDebitNumber();
		}
	}


	public List<PaymentScheduleLine> getPaymentScheduleLinesToReject(String refDebitReject, Company company)  {

		// Identification de la ligne d'échéance correspondant au rejet
		PaymentScheduleLine pslRequested = paymentScheduleLineRepo.all().filter("UPPER(self.debitNumber) = ?1 AND self.paymentSchedule.company = ?2", refDebitReject, company).fetchOne();

		// Identification de l'objet de gestion de prélèvement (cas des export bancaire dont plusieurs échéances ont été consolidées)
		DirectDebitManagement directDebitManagementRequested = directDebitManagementRepo.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();

		List<PaymentScheduleLine> paymentScheduleLinesToRejectList = new ArrayList<PaymentScheduleLine>();

		if(pslRequested != null)  {
			paymentScheduleLinesToRejectList.add(pslRequested);
		}

		if(directDebitManagementRequested != null)  {
			if(directDebitManagementRequested.getPaymentScheduleLineList() != null && directDebitManagementRequested.getPaymentScheduleLineList().size() != 0)  {
				paymentScheduleLinesToRejectList.addAll(this.sortForAssignAmountRejectedForPaymentScheduleLine(directDebitManagementRequested));
			}
		}
		return paymentScheduleLinesToRejectList;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public List<PaymentScheduleLine> importRejectPaymentScheduleLine(LocalDate dateReject, String refDebitReject, BigDecimal amountReject, InterbankCodeLine causeReject, Company company) throws AxelorException  {

		List<PaymentScheduleLine> paymentScheduleLineRejectedList = new ArrayList<PaymentScheduleLine>();

		List<PaymentScheduleLine> paymentScheduleLinesToRejectList = this.getPaymentScheduleLinesToReject(refDebitReject, company);


		/*** Récupération et traitements des échéances rejetées  ***/
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLinesToRejectList)  {
			if(!paymentScheduleLine.getRejectedOk())  {

				log.debug("un échéancier trouvé");

				//Lissage de paiement

				// Afin de pouvoir associer le montant rejeté à l'échéance
				amountReject = this.setAmountRejected(paymentScheduleLine, amountReject, paymentScheduleLine.getInTaxAmount());

				if(paymentScheduleLine.getAmountRejected().compareTo(BigDecimal.ZERO) == 1)  {
					// Si dérnière échéance, créer juste l'écriture de rejet (extourne)
					if(paymentScheduleService.isLastSchedule(paymentScheduleLine))  {
						this.setRejectOnPaymentScheduleLine(paymentScheduleLine, dateReject, causeReject);
					}
					else  {
						// Mise à jour des échéances
						this.paymentScheduleRejectProcessing(paymentScheduleLine);
						this.setRejectOnPaymentScheduleLine(paymentScheduleLine, dateReject, causeReject);
					}

					pslListGC.add(paymentScheduleLine);
					paymentScheduleLineRejectedList.add(paymentScheduleLine);
				}

			}
		}
		return paymentScheduleLineRejectedList;
	}


	public List<Invoice> getInvoicesToReject(String refDebitReject, Company company)  {

		// Identification de la facture correspondant au rejet
		Invoice invoiceRequested = invoiceRepo.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();

		// Identification de l'objet de gestion de prélèvement (cas des export bancaire dont plusieurs échéances où plusieurs factures ont été consolidés)
		DirectDebitManagement directDebitManagementRequested = directDebitManagementRepo.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();

		List<Invoice> invoicesToRejectList = new ArrayList<Invoice>();

		if(invoiceRequested != null)  {
			invoicesToRejectList.add(invoiceRequested);
		}

		if(directDebitManagementRequested != null)  {
			if(directDebitManagementRequested.getInvoiceSet() != null && directDebitManagementRequested.getInvoiceSet().size() != 0)  {
				invoicesToRejectList.addAll(this.sortForAssignAmountRejectedForInvoice(directDebitManagementRequested));
			}
		}
		return invoicesToRejectList;
	}


	public List<Invoice> importRejectInvoice(LocalDate dateReject, String refDebitReject, BigDecimal amountReject, InterbankCodeLine causeReject, Company company)  {
		List<Invoice> invoiceRejectedList = new ArrayList<Invoice>();

		List<Invoice> invoicesToRejectList = this.getInvoicesToReject(refDebitReject, company);

		/*** Récupération des factures rejetées  ***/
		for(Invoice invoice : invoicesToRejectList)  {
			log.debug("une facture trouvée");

			// Afin de pouvoir associer le montant rejeté à la facture
			amountReject = this.setAmountRejected(invoice, amountReject, cfonbImportService.getAmountRemainingFromPaymentMove(invoice));

			if(invoice.getAmountRejected().compareTo(BigDecimal.ZERO) == 1)  {
				invoice.setRejectDate(dateReject);
				invoice.setInterbankCodeLine(causeReject);

				invoiceRepo.save(invoice);

				invoiceList.add(invoice);
				invoiceRejectedList.add(invoice);
			}
		}

		return invoiceRejectedList;
	}


	public BigDecimal setAmountRejected(PaymentScheduleLine paymentScheduleLine, BigDecimal amountReject, BigDecimal amountPaid)  {
		BigDecimal amountReject2 = amountReject;
		if(paymentScheduleLine.getDirectDebitManagement() != null)  {
			if(amountPaid.compareTo(amountReject2) > 0)  {
				paymentScheduleLine.setAmountRejected(amountReject2);
			}
			else  {
				paymentScheduleLine.setAmountRejected(amountPaid);
			}
			amountReject2 = amountReject2.subtract(amountPaid);
		}
		else  {
			paymentScheduleLine.setAmountRejected(amountReject2);
		}
		return amountReject2;
	}


	public BigDecimal setAmountRejected(Invoice invoice, BigDecimal amountReject, BigDecimal amountPaid)  {
		BigDecimal amountReject2 = amountReject;
		if(invoice.getDirectDebitManagement() != null)  {
			if(amountPaid.compareTo(amountReject2) > 0)  {
				invoice.setAmountRejected(amountReject2);
			}
			else  {
				invoice.setAmountRejected(amountPaid);
			}
			amountReject2 = amountReject2.subtract(amountPaid);
		}
		else  {
			invoice.setAmountRejected(amountReject2);
		}
		return amountReject2;
	}


	public List<PaymentScheduleLine> sortForAssignAmountRejectedForPaymentScheduleLine(DirectDebitManagement directDebitManagement)  {
		List<PaymentScheduleLine> pslList = new ArrayList<PaymentScheduleLine>();
		List<PaymentScheduleLine> rejectedPslList = new ArrayList<PaymentScheduleLine>();
		List<PaymentScheduleLine> sortedPslList = new ArrayList<PaymentScheduleLine>();
		for(PaymentScheduleLine paymentScheduleLine : directDebitManagement.getPaymentScheduleLineList())  {
			if(paymentScheduleLine.getFromReject())  {
				rejectedPslList.add(paymentScheduleLine);
			}
			else  {
				pslList.add(paymentScheduleLine);
			}
		}

		sortedPslList.addAll(sortPaymentScheduleLineList(rejectedPslList));
		sortedPslList.addAll(sortPaymentScheduleLineList(pslList));

		return sortedPslList;

	}


	public List<Invoice> sortForAssignAmountRejectedForInvoice(DirectDebitManagement directDebitManagement)  {
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		List<Invoice> rejectedInvoiceList = new ArrayList<Invoice>();
		List<Invoice> sortedInvoiceList = new ArrayList<Invoice>();
		for(Invoice invoice : directDebitManagement.getInvoiceSet())  {
			if(invoice.getRejectMoveLine() != null)  {
				rejectedInvoiceList.add(invoice);
			}
			else  {
				invoiceList.add(invoice);
			}
		}

		sortedInvoiceList.addAll(sortInvoiceList(rejectedInvoiceList));
		sortedInvoiceList.addAll(sortInvoiceList(invoiceList));

		return sortedInvoiceList;

	}


	public List<PaymentScheduleLine> sortPaymentScheduleLineList(List<PaymentScheduleLine> paymentScheduleLineList)  {
		List<PaymentScheduleLine> paymentScheduleLineList2 = paymentScheduleLineList;
		int size = paymentScheduleLineList2.size();
		int min = 0;

		for(int i = 0; i < size-1 ;i++)  {
			min = i;
			for(int j = i + 1; j < size; j++)  {
				if(paymentScheduleLineList2.get(j).getScheduleDate().isBefore(paymentScheduleLineList2.get(min).getScheduleDate()))  {
					min = j;
				}
			}
			if(min != i)  {
				paymentScheduleLineList2 = this.permuteElement(paymentScheduleLineList2, paymentScheduleLineList2.get(i), paymentScheduleLineList2.get(min));
			}
		}
		return paymentScheduleLineList2;

	}



	public List<Invoice> sortInvoiceList(List<Invoice> invoiceList)  {
		List<Invoice> invoiceList2 = invoiceList;
		int size = invoiceList2.size();
		int min = 0;

		for(int i = 0; i < size-1 ;i++)  {
			min = i;
			for(int j = i + 1; j < size; j++)  {
				if(invoiceList2.get(j).getDueDate().isBefore(invoiceList2.get(min).getDueDate()))  {
					min = j;
				}
			}
			if(min != i)  {
				invoiceList2 = this.permuteElement(invoiceList2, invoiceList2.get(i), invoiceList2.get(min));
			}
		}
		return invoiceList2;

	}


	public List<PaymentScheduleLine> permuteElement(List<PaymentScheduleLine> pslList, PaymentScheduleLine psl1, PaymentScheduleLine psl2)  {
		int position = pslList.indexOf(psl1);
		pslList.set(pslList.indexOf(psl2), psl1);
		pslList.set(position, psl2);
		return pslList;
	}


	public List<Invoice> permuteElement(List<Invoice> pslList, Invoice invoice1, Invoice invoice2)  {
		int position = pslList.indexOf(invoice1);
		pslList.set(pslList.indexOf(invoice2), invoice1);
		pslList.set(position, invoice2);
		return pslList;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createRejectMove(Company company, LocalDate date) throws AxelorException  {
		Journal rejectJournal = company.getAccountConfig().getRejectJournal();

		Move move = moveService.getMoveCreateService().createMove(rejectJournal, company, null, null, date, null);
		move.setRejectOk(true);
		moveRepo.save(move);
		return move;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move validateMove(Move move) throws AxelorException  {
		moveService.getMoveValidateService().validateMove(move);
		moveRepo.save(move);
		return move;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteMove(Move move) throws AxelorException  {
		moveRepo.remove(move);
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public MoveLine createRejectOppositeMoveLine(Company company, Move move, int ref, LocalDate rejectDate) throws AxelorException  {

		//On récupère l'objet mode de paiement pour pouvoir retrouver le numéro de compte associé
		PaymentMode paymentMode = company.getAccountConfig().getRejectionPaymentMode();
		Account paymentModeAccount = paymentModeService.getPaymentModeAccount(paymentMode, company);

		// Création d'une seule contrepartie
		log.debug("Création d'une seule contrepartie");
		MoveLine moveLine = moveLineService.createMoveLine(move, null, paymentModeAccount, this.getTotalDebit(move), false, rejectDate, ref, null);
		move.getMoveLineList().add(moveLine);

		moveLineRepo.save(moveLine);

		return moveLine;
	}


	public BigDecimal getTotalDebit(Move move)  {
		log.debug("move.getMoveLineList() {}", move.getMoveLineList());

		BigDecimal totalDebit = BigDecimal.ZERO;
		for(MoveLine moveLine : move.getMoveLineList())  {
			totalDebit = totalDebit.add(moveLine.getDebit());
		}
		return totalDebit;
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
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public MoveLine createMajorAccountRejectMoveLine(PaymentScheduleLine paymentScheduleLine, Company company, Account customerAccount, Move move, int ref) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		if(paymentSchedule.getCompany().equals(company))  {

			// Création d'une ligne d'écriture par rejet
			log.debug("Création d'une ligne d'écriture par rejet");
			MoveLine moveLine = moveLineService.createMoveLine(move, paymentSchedule.getPartner(), customerAccount, paymentScheduleLine.getAmountRejected(),
					true, paymentScheduleLine.getRejectDate(), paymentScheduleLine.getRejectDate(), ref, paymentScheduleLine.getName());
			moveLine.setPaymentScheduleLine(paymentScheduleLine);
			move.getMoveLineList().add(moveLine);
			moveLineRepo.save(moveLine);

			InterbankCodeLine interbankCodeLine = paymentScheduleLine.getInterbankCodeLine();

			// Mise à jour du motif du rejet dans la ligne d'écriture
			moveLine.setInterbankCodeLine(interbankCodeLine);

			// Mise à jour du nombre de rejet sur le tiers si ce n'est pas un rejet technique
			if(!interbankCodeLine.getTechnicalRejectOk()) {
				paymentSchedule.getPartner().setRejectCounter(paymentSchedule.getPartner().getRejectCounter()+1);
			}

			// Mise à jour de la ligne de rejet dans la ligne d'échéance
			paymentScheduleLine.setRejectMoveLine(moveLine);

			ref++;

			// Si le nombre de rejet limite est atteint :
			this.rejectLimitExceeded(paymentScheduleLine);

			return moveLine;
		}
		return null;
	}


	/**
	 *
	 * @param invoiceList
	 * @param company
	 * @param move
	 * @param ref
	 * @return
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public MoveLine createInvoiceRejectMoveLine(Invoice invoice, Company company, Account customerAccount, Move move, int ref) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {
		if(invoice.getCompany().equals(company))  {

			MoveLine moveLine = this.createRejectMoveLine(invoice, invoice.getCompany(), customerAccount, move, ref);

			InterbankCodeLine interbankCodeLine = invoice.getInterbankCodeLine();

			// Mise à jour du motif du rejet dans la ligne d'écriture
			moveLine.setInterbankCodeLine(interbankCodeLine);

			// Mise à jour du nombre de rejet sur le tiers si ce n'est pas un rejet technique
			if(!interbankCodeLine.getTechnicalRejectOk()) {
				invoice.getPartner().setRejectCounter(invoice.getPartner().getRejectCounter()+1);
			}

			// Si le nombre de rejet limite est atteint :
			this.rejectLimitExceeded(invoice);

			return moveLine;
		}
		return null;
	}


	/**
	 * Méthode générant une ligne d'écriture de rejet pour une facture
	 * @param invoice
	 * 			Une facture
	 * @param company
	 * 			Une sociéte
	 * @param moveGenerated
	 * 			L'écriture de rejet
	 * @param totalAmountInvoice
	 * 			Le montant courant de l'ensemble des rejets sur facture, sera incrémenté à chaque appel de la fonction
	 * @param ref
	 * 			La référence de la facture
	 * @param rejectReason
	 * 			Le motif du prélèvement
	 * @param fromBatch
	 * @return
	 * 			Le montant courant de l'ensemble des rejets d'une facture
	 */
	public MoveLine createRejectMoveLine(Invoice invoice, Company company, Account customerAccount, Move moveGenerated, int ref)  {

		MoveLine rejectMoveLine = moveLineService.createMoveLine(moveGenerated, invoice.getPartner(), customerAccount, invoice.getAmountRejected(), true,
				invoice.getRejectDate(), invoice.getRejectDate(), ref, invoice.getInvoiceId());

		moveGenerated.getMoveLineList().add(rejectMoveLine);

		log.debug("PaymentScheduleRejectProcessing - ajout de la ligne de rejet à l'écriture de rejet");

		rejectMoveLine.setInvoiceReject(invoice);

		moveLineRepo.save(rejectMoveLine);

		invoice.setRejectMoveLine(rejectMoveLine);

		return rejectMoveLine;
	}


	/**
	 * Procédure permettant de déclencher des actions si le nombre limite de rejet autorisé est dépassé
	 * @param psl
	 * 			Une ligne d'échéancier
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void rejectLimitExceeded(PaymentScheduleLine paymentScheduleLine) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		log.debug("Action suite à un rejet sur une échéancier");
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		Company company = paymentSchedule.getCompany();
		Partner partner = paymentSchedule.getPartner();
		AccountConfig accountConfig = company.getAccountConfig();

		if(partner.getRejectCounter() >= accountConfig.getPaymentScheduleRejectNumLimit())  {

			// Génération du message
			this.createImportRejectMessage(partner, company, accountConfig.getRejectPaymentScheduleTemplate(), paymentScheduleLine.getRejectMoveLine());
			// Changement du mode de paiement de l'échéancier, du tiers
			this.setPaymentMode(paymentSchedule);
			// Alarme générée dans l'historique du client ?
			log.debug("Alarme générée dans l'historique du client");
		}

		// Mise à jour de la date de la dernière relance sur le tiers
		reminderService.getReminder(partner, company).setReminderDate(today);
	}



	/**
	 * Procédure permettant de déclencher des actions si le nombre limite de rejet autorisé est dépassé
	 * @param psl
	 * 			Une facture
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void rejectLimitExceeded(Invoice invoice) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {
		log.debug("Action suite à un rejet sur une facture");
		Partner partner = invoice.getPartner();
		Company company = invoice.getCompany();
		AccountConfig accountConfig = company.getAccountConfig();

		if(partner.getRejectCounter() >= accountConfig.getInvoiceRejectNumLimit())  {

			// Génération du message
			this.createImportRejectMessage(invoice.getPartner(), company, accountConfig.getRejectPaymentScheduleTemplate(), invoice.getRejectMoveLine());
			// Mise à jour de la date de la dernière relance sur le tiers
			reminderService.getReminder(partner, company).setReminderDate(today);
			// Changement du mode de paiement de la facture, du tiers
			this.setPaymentMode(invoice);
			// Alarme générée dans l'historique du client ?
			log.debug("Alarme générée dans l'historique du client");
			invoiceRepo.save(invoice);
		}
	}


	/**
	 * Procédure permettant de créer un courrier spécifique aux imports des rejets
	 * @param contact
	 * 			Un contact
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Message createImportRejectMessage(Partner partner, Company company, Template template, MoveLine rejectMoveLine) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		TemplateMessageService templateMessageService = Beans.get(TemplateMessageService.class);

		Message message = templateMessageService.generateMessage(rejectMoveLine, template);

		Beans.get(MessageRepository.class).save(message);

		return message;

	}






	/**
	 * Méthode permettant de changer le mode de paiement de la facture et du tiers
	 * @param invoice
	 * 			Une facture
	 */
	public void setPaymentMode(Invoice invoice)  {
		Partner partner = invoice.getPartner();
		PaymentMode paymentMode = invoice.getCompany().getAccountConfig().getRejectionPaymentMode();
		invoice.setPaymentMode(paymentMode);
		partner.setPaymentMode(paymentMode);
	}


	/**
	 * Méthode permettant de changer le mode de paiement de l'échéancier de paiement et du tiers
	 * @param invoice
	 * 			Une échéance de paiement
	 */
	public void setPaymentMode(PaymentSchedule paymentSchedule)  {
		Partner partner = paymentSchedule.getPartner();
		PaymentMode paymentMode = paymentSchedule.getCompany().getAccountConfig().getRejectionPaymentMode();
		paymentSchedule.setPaymentMode(paymentMode);
		partner.setPaymentMode(paymentMode);
	}


	/**
	 * Procédure pemrettant de mettre à jour et de créer les lignes d'échéances correspondant à un rejet
	 * @param psl
	 * 				Une ligne d'échéancier
	 * @param amountReject
	 * 				le montant rejeté
	 * @param moveLine
	 * 				La ligne d'écriture de rejet utilisée si l'on est en présence d'un échéancier de paiment
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentScheduleLine paymentScheduleRejectProcessing(PaymentScheduleLine paymentScheduleLine)  {

		log.debug("PaymentScheduleRejectProcessing - Création d'une nouvelle ligne identique à l'originale");

		// Création d'une nouvelle ligne identique à l'originale
		PaymentScheduleLine paymentScheduleLineCopy = new PaymentScheduleLine();
		paymentScheduleLineCopy.setDebitBlockingOk(paymentScheduleLine.getDebitBlockingOk());
		paymentScheduleLineCopy.setInTaxAmount(paymentScheduleLine.getAmountRejected());
		paymentScheduleLineCopy.setInTaxAmountPaid(BigDecimal.ZERO);
		paymentScheduleLineCopy.setName(paymentScheduleLine.getName());
		paymentScheduleLineCopy.setPaymentSchedule(paymentScheduleLine.getPaymentSchedule());
		paymentScheduleLine.getPaymentSchedule().getPaymentScheduleLineList().add(paymentScheduleLineCopy);
		paymentScheduleLineCopy.setScheduleDate(paymentScheduleLine.getScheduleDate());
		paymentScheduleLineCopy.setScheduleLineSeq(paymentScheduleLine.getScheduleLineSeq());
		paymentScheduleLineCopy.setFromReject(true);

		paymentScheduleLineCopy.setStatusSelect(PaymentScheduleLineRepository.STATUS_IN_PROGRESS);

		return paymentScheduleLineRepo.save(paymentScheduleLineCopy);
	}


	/**
	 * Procédure permettant de passer une ligne d'échéancier validé en ligne d'échéancier rejetée
	 * @param paymentScheduleLine
	 * 				Une ligne d'échéancier
	 * @param dateReject
	 * 				Une date de rejet
	 * @param causeReject
	 * 				Un motif de rejet
	 */
	public void setRejectOnPaymentScheduleLine(PaymentScheduleLine paymentScheduleLine, LocalDate dateReject, InterbankCodeLine causeReject)  {
		// Maj de la ligne originale en rejet
		paymentScheduleLine.setRejectedOk(true);

		paymentScheduleLine.setRejectDate(dateReject);
		paymentScheduleLine.setInterbankCodeLine(causeReject);
		paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_CLOSED);
	}



}
