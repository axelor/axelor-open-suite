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
package com.axelor.apps.account.service;

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
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class PaymentScheduleImportService {

private static final Logger LOG = LoggerFactory.getLogger(PaymentScheduleImportService.class); 
	
	@Inject
	private MoveLineService mls;

	@Inject
	private MoveService ms;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private ReconcileService rcs;
	
	@Inject
	private MailService mas;
	
	@Inject
	private PaymentScheduleService pss;
	
	@Inject
	private PaymentModeService pms;

	@Inject
	private CfonbService cs;
	
	@Inject
	private RejectImportService ris;
	
	@Inject
	private ReminderService rs;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	@Inject
	private Injector injector;
	
	private LocalDate today;
	
	private List<PaymentScheduleLine> pslListGC = new ArrayList<PaymentScheduleLine>();   				// liste des échéances de lissage de paiement rejetées
	private List<Invoice> invoiceList = new ArrayList<Invoice>();										// liste des factures rejetées

	@Inject
	public PaymentScheduleImportService() {
		
		this.today = GeneralService.getTodayDate();

	}
	
	public List<PaymentScheduleLine> getPaymentScheduleLineMajorAccountList()  {
		return this.pslListGC;
	}
	
	public List<Invoice> getInvoiceList()  {
		return this.invoiceList;
	}
	
	
	public void initialiseCollection()  {
		
		pslListGC = new ArrayList<PaymentScheduleLine>();   				// liste des échéances de lissage de paiement rejetées
		invoiceList = new ArrayList<Invoice>();	
		
	}
	
	
	public void checkCompanyFields(Company company) throws AxelorException  {
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		// Test si les champs d'import sont configurés pour la société
		accountConfigService.getRejectImportPathAndFileName(accountConfig);
		accountConfigService.getTempImportPathAndFileName(accountConfig);
		accountConfigService.getRejectPaymentScheduleMailModel(accountConfig);
		accountConfigService.getCustomerAccount(accountConfig);
		
		Journal rejectJournal = accountConfigService.getRejectJournal(accountConfig);
		
		String seq = sgs.getSequence(IAdministration.DEBIT_REJECT, company, rejectJournal, true);
		if(seq == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de rejet des prélèvements\n pour la société %s pour le journal %s", 
					GeneralService.getExceptionAccountingMsg(), company.getName(), rejectJournal.getName()), IException.CONFIGURATION_ERROR);
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
		PaymentScheduleLine pslRequested = PaymentScheduleLine.all().filter("UPPER(self.debitNumber) = ?1 AND self.paymentSchedule.company = ?2", refDebitReject, company).fetchOne();
		
		// Identification de l'objet de gestion de prélèvement (cas des export bancaire dont plusieurs échéances ont été consolidées)
		DirectDebitManagement directDebitManagementRequested = DirectDebitManagement.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();
		
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
				
				LOG.debug("un échéancier trouvé");

				//Lissage de paiement
					
				LOG.debug("Mensu Grand Compte");	
				
				// Afin de pouvoir associer le montant rejeté à l'échéance
				amountReject = this.setAmountRejected(paymentScheduleLine, amountReject, paymentScheduleLine.getInTaxAmount());
				
				if(paymentScheduleLine.getAmountRejected().compareTo(BigDecimal.ZERO) == 1)  {
					// Si dérnière échéance, créer juste l'écriture de rejet (extourne)
					if(pss.isLastSchedule(paymentScheduleLine))  {
						this.setRejectOnPaymentScheduleLine(paymentScheduleLine, dateReject, causeReject, this.getStatusClo());
					}
					else  {
						// Mise à jour des échéances
						this.paymentScheduleRejectProcessing(paymentScheduleLine, this.getStatusUpr(), false, paymentScheduleLine.getAmountRejected());
						this.setRejectOnPaymentScheduleLine(paymentScheduleLine, dateReject, causeReject, this.getStatusClo());
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
		Invoice invoiceRequested = Invoice.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();
				
		// Identification de l'objet de gestion de prélèvement (cas des export bancaire dont plusieurs échéances où plusieurs factures ont été consolidés)
		DirectDebitManagement directDebitManagementRequested = DirectDebitManagement.all().filter("UPPER(self.debitNumber) = ?1 AND company = ?2", refDebitReject, company).fetchOne();
				
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
			LOG.debug("une facture trouvée");
			
			// Afin de pouvoir associer le montant rejeté à la facture
			amountReject = this.setAmountRejected(invoice, amountReject, cs.getAmountRemainingFromPaymentMove(invoice));
			
			if(invoice.getAmountRejected().compareTo(BigDecimal.ZERO) == 1)  {
				invoice.setRejectDate(dateReject);
				invoice.setInterbankCodeLine(causeReject);
				
				invoice.save();
			
				invoiceList.add(invoice);
				invoiceRejectedList.add(invoice);
			}
		}	
		
		return invoiceRejectedList;
	}
	
	
	public Status getStatusUpr()  {
		return Status.all().filter("code = 'upr'").fetchOne();
	}
	
	public Status getStatusClo()  {
		return Status.all().filter("code = 'clo'").fetchOne();
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
	
	
	/**
	 * Récupération d'un échéancier de mensu masse rejeté après la ventilation de la facture
	 * @param pslListMPAVI
	 * @param pslMonthlyPaymentAfterVentilateInvoice
	 * @param causeReject
	 * @param amountReject
	 * @return
	 */
	public List<PaymentScheduleLine> monthlyPaymentAfterVentilateInvoiceProcess(List<PaymentScheduleLine> pslListMPAVI, PaymentScheduleLine pslMonthlyPaymentAfterVentilateInvoice, InterbankCodeLine causeReject, BigDecimal amountReject)  {
		
		if(pslMonthlyPaymentAfterVentilateInvoice != null)  {
			
			LOG.debug("Paiement d'un rejet d'une échéance de paiement après que la facture est été ventilé");
			pslMonthlyPaymentAfterVentilateInvoice.setInterbankCodeLine(causeReject);
			
			if(pslMonthlyPaymentAfterVentilateInvoice.getAmountRejected().compareTo(BigDecimal.ZERO) == 1)  {
				pslListMPAVI.add(pslMonthlyPaymentAfterVentilateInvoice);
			}
			
			// Afin de pouvoir associer le montant rejeté à l'échéance
			pslMonthlyPaymentAfterVentilateInvoice.setAmountRejected(amountReject);
		
		}
		return pslListMPAVI;
	}

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createRejectMove(Company company, LocalDate date) throws AxelorException  {
		Journal rejectJournal = company.getAccountConfig().getRejectJournal();
		
		Move move = ms.createMove(rejectJournal, company, null, null, date, null, true);
		move.setRejectOk(true);
		move.save();
		return move;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move validateMove(Move move) throws AxelorException  {
		ms.validateMove(move);
		move.save();
		return move;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteMove(Move move) throws AxelorException  {
		move.remove();
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public MoveLine createRejectOppositeMoveLine(Company company, Move move, int ref, LocalDate rejectDate) throws AxelorException  {
		//On récupère l'objet mode de paiement pour pouvoir retrouver le numéro de compte associé
		PaymentMode pm = PaymentMode.all().filter("self.code = 'DD'").fetchOne();
		Account paymentModeAccount = pms.getCompanyAccount(pm, company);
		
		// Création d'une seule contrepartie
		LOG.debug("Création d'une seule contrepartie");
		MoveLine moveLine = mls.createMoveLine(move, null, paymentModeAccount, this.getTotalDebit(move), false, false, rejectDate, ref, null);
		move.getMoveLineList().add(moveLine);		
		
		moveLine.save();
		
		return moveLine;
	}
	
	
	public BigDecimal getTotalDebit(Move move)  {
		LOG.debug("move.getMoveLineList() {}", move.getMoveLineList());

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
	 */
	public MoveLine createMajorAccountRejectMoveLine(PaymentScheduleLine paymentScheduleLine, Company company, Account customerAccount, Move move, int ref) throws AxelorException  {
			
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		if(paymentSchedule.getCompany().equals(company))  {
				
			// Création d'une ligne d'écriture par rejet
			LOG.debug("Création d'une ligne d'écriture par rejet");
			MoveLine moveLine = mls.createMoveLine(move, paymentSchedule.getPartner(), customerAccount, paymentScheduleLine.getAmountRejected(),  
					true, false, paymentScheduleLine.getRejectDate(), paymentScheduleLine.getRejectDate(), ref, paymentScheduleLine.getName());
			moveLine.setPaymentScheduleLine(paymentScheduleLine);
			move.getMoveLineList().add(moveLine);
			moveLine.save();
			
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
	 */
	public MoveLine createInvoiceRejectMoveLine(Invoice invoice, Company company, Account customerAccount, Move move, int ref) throws AxelorException  {
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
		
		MoveLine rejectMoveLine = mls.createMoveLine(moveGenerated, invoice.getPartner(), customerAccount, invoice.getAmountRejected(), true, false, 
				invoice.getRejectDate(), invoice.getRejectDate(), ref, invoice.getInvoiceId());
		
		moveGenerated.getMoveLineList().add(rejectMoveLine);
		
		LOG.debug("PaymentScheduleRejectProcessing - ajout de la ligne de rejet à l'écriture de rejet");
	
		rejectMoveLine.setInvoiceReject(invoice);
		
		rejectMoveLine.save();
		
		invoice.setRejectMoveLine(rejectMoveLine);
		
		return rejectMoveLine;
	}
	
	
	/**
	 * Procédure permettant de déclencher des actions si le nombre limite de rejet autorisé est dépassé
	 * @param psl
	 * 			Une ligne d'échéancier
	 * @throws AxelorException 
	 */
	public void rejectLimitExceeded(PaymentScheduleLine paymentScheduleLine) throws AxelorException  {
		
		LOG.debug("Action suite à un rejet sur une échéancier");
		PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
		Company company = paymentSchedule.getCompany();
		Partner partner = paymentSchedule.getPartner();
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(partner.getRejectCounter() >= accountConfig.getPaymentScheduleRejectNumLimit())  {
			// Génération du COURRIER
			LOG.debug("COURRIER Paiement");
			mas.createImportRejectMail(partner, company, accountConfig.getRejectPaymentScheduleMailModel(), paymentScheduleLine.getRejectMoveLine()).save();
			// Changement du mode de paiement de l'échéancier, du tiers
			this.setPaymentMode(paymentSchedule);
			// Alarme générée dans l'historique du client ?
			LOG.debug("Alarme générée dans l'historique du client");
		}
			
		// Mise à jour de la date de la dernière relance sur le tiers
		rs.getReminder(partner, company).setReminderDate(today);
	}
	
	
	
	/**
	 * Procédure permettant de déclencher des actions si le nombre limite de rejet autorisé est dépassé
	 * @param psl
	 * 			Une facture
	 * @throws AxelorException 
	 */
	public void rejectLimitExceeded(Invoice invoice) throws AxelorException  {
		LOG.debug("Action suite à un rejet sur une facture");
		Partner partner = invoice.getPartner();
		Company company = invoice.getCompany();
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(partner.getRejectCounter() >= accountConfig.getInvoiceRejectNumLimit())  {
			// Génération du COURRIER
			LOG.debug("COURRIER Facture");
			mas.createImportRejectMail(invoice.getPartner(), company, accountConfig.getRejectPaymentScheduleMailModel(), invoice.getRejectMoveLine()).save();
			// Mise à jour de la date de la dernière relance sur le tiers
			rs.getReminder(partner, company).setReminderDate(today);
			// Changement du mode de paiement de la facture, du tiers
			this.setPaymentMode(invoice);
			// Alarme générée dans l'historique du client ?
			LOG.debug("Alarme générée dans l'historique du client");
			invoice.save(); 
		}	
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
	 * @param valLigne
	 * 				Un dictionnaire contenant l'ensemble des valeurs d'une ligne de rejet
	 * @param statusUpr
	 * 				Le statut "en cours"
	 * @param statusClo
	 * 				Le statut "cloturé"
	 * @param echeancierPaiment
	 * 				La ligne d'échéancier psl appartient t'elle à un échéancier de paiement
	 * @param moveLine
	 * 				La ligne d'écriture de rejet utilisée si l'on est en présence d'un échéancier de paiment
	 */
	public PaymentScheduleLine paymentScheduleRejectProcessing(PaymentScheduleLine psl, Status statusUpr, boolean paymentScheduleOk, BigDecimal amountReject)  {
		
		// Création d'une nouvelle ligne identique à l'originale
		PaymentScheduleLine pslNew = this.paymentScheduleRejectProcessing(psl, statusUpr, paymentScheduleOk, amountReject, psl.getScheduleDate());
		
		return pslNew;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentScheduleLine paymentScheduleRejectProcessing(PaymentScheduleLine psl, Status statusUpr, boolean paymentScheduleOk, BigDecimal amountReject, LocalDate ScheduleDate)  {
		
		LOG.debug("Begin PaymentScheduleRejectProcessing...");
		LOG.debug("PaymentScheduleRejectProcessing - Création d'une nouvelle ligne identique à l'originale");
		
		// Création d'une nouvelle ligne identique à l'originale
		PaymentScheduleLine pslNew = new PaymentScheduleLine();
		pslNew.setDebitBlockingOk(psl.getDebitBlockingOk());
		pslNew.setInTaxAmount(amountReject);
		pslNew.setInTaxAmountPaid(BigDecimal.ZERO);
		pslNew.setName(psl.getName());
		pslNew.setPaymentSchedule(psl.getPaymentSchedule());
		psl.getPaymentSchedule().getPaymentScheduleLineList().add(pslNew);
		pslNew.setScheduleDate(ScheduleDate);
		pslNew.setScheduleLineSeq(psl.getScheduleLineSeq());
		pslNew.setFromReject(true);
		
		pslNew.setStatus(statusUpr);
		LOG.debug("PaymentScheduleRejectProcessing - save pslNew");
		pslNew.save();
		
		LOG.debug("End PaymentScheduleRejectProcessing");
		return pslNew;
	}
	
	
	/**
	 * Procédure permettant de passer une ligne d'échéancier validé en ligne d'échéancier rejetée
	 * @param paymentScheduleLine
	 * 				Une ligne d'échéancier
	 * @param dateReject
	 * 				Une date de rejet
	 * @param causeReject
	 * 				Un motif de rejet
	 * @param statusClo
	 * 				Un status 'clo' ie cloturé
	 */
	public void setRejectOnPaymentScheduleLine(PaymentScheduleLine paymentScheduleLine, LocalDate dateReject, InterbankCodeLine causeReject, Status statusClo)  {
		// Maj de la ligne originale en rejet
		paymentScheduleLine.setRejectedOk(true);

		paymentScheduleLine.setRejectDate(dateReject);
		paymentScheduleLine.setInterbankCodeLine(causeReject);
		paymentScheduleLine.setStatus(statusClo);
	}
	
	

}
