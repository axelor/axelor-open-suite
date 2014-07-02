/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityTransaction;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceLineType;
import com.axelor.apps.account.db.Irrecoverable;
import com.axelor.apps.account.db.IrrecoverableCustomerLine;
import com.axelor.apps.account.db.IrrecoverableInvoiceLine;
import com.axelor.apps.account.db.IrrecoverablePaymentScheduleLineLine;
import com.axelor.apps.account.db.IrrecoverableReportLine;
import com.axelor.apps.account.db.ManagementObject;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class IrrecoverableService {
	
	private static final Logger LOG = LoggerFactory.getLogger(IrrecoverableService.class); 

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private ReconcileService reconcileService;
	
	@Inject
	private TaxService taxService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	@Inject
	private TaxAccountService taxAccountService;
	
	@Inject
	private PaymentScheduleService paymentScheduleService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	private LocalDate date;

	@Inject
	public IrrecoverableService() {
		
		this.date = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Procédure permettant de remplir la liste des factures et échéances rejetées à passer en irrécouvrable d'une société, 
	 * ansi que de remplir le champ nom de l'objet irrécouvrable
	 * @param irrecoverable
	 * 			Un objet Irrécouvrable
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void getIrrecoverable(Irrecoverable irrecoverable) throws AxelorException  {
		
		Company company = irrecoverable.getCompany();
		
		this.testCompanyField(company);
		
		if(irrecoverable.getName() == null)  {
			irrecoverable.setName(this.getSequence(company));
		}
		
		irrecoverable.setInvoiceSet(new HashSet<Invoice>());
		irrecoverable.getInvoiceSet().addAll(this.getInvoiceList(company));
		irrecoverable.getInvoiceSet().addAll(this.getRejectInvoiceList(company));
		
		irrecoverable.setPaymentScheduleLineSet(new HashSet<PaymentScheduleLine>());
		irrecoverable.getPaymentScheduleLineSet().addAll(this.getPaymentScheduleLineList(company));
		
		irrecoverable.save();
	}
	
	
	
	
	
	
	
	
	/**
	 * Fonction permettant de récupérer la liste des tiers payeur associés à une liste de factures
	 * @param invoiceList
	 * 			Une liste de factures
	 * @return
	 */
	public List<Partner> getPayerPartnerList(Set<Invoice> invoiceList)  {
		List<Partner> partnerList = new ArrayList<Partner>();
		
		for(Invoice invoice : invoiceList)  {
			if(!partnerList.contains(invoice.getPartner()))  {
				partnerList.add(invoice.getPartner());
			}
		}
		return partnerList;
	}
	
	
	/**
	 * Fonction permettant de récupérer la liste des factures à passer en irrécouvrable d'une société
	 * @param company
	 * 			Une société
	 * @return
	 */
	public List<Invoice> getInvoiceList(Company company)   {
		return Invoice.filter("self.irrecoverableStateSelect = ?1 AND self.company = ?2 AND self.status.code = 'dis' AND self.inTaxTotalRemaining > 0 AND self.rejectMoveLine IS NULL ORDER BY self.dueDate ASC", IInvoice.TO_PASS_IN_IRRECOUVRABLE, company).fetch();
	}
	
	/**
	 * Fonction permettant de récupérer la liste des factures rejetées à passer en irrécouvrable d'une société
	 * @param company
	 * 			Une société
	 * @return
	 */
	public List<Invoice> getRejectInvoiceList(Company company)   {
		return Invoice.filter("self.irrecoverableStateSelect = ?1 AND self.company = ?2 AND self.status.code = 'dis' AND self.inTaxTotalRemaining = 0 AND self.rejectMoveLine IS NOT NULL ORDER BY self.dueDate ASC", IInvoice.TO_PASS_IN_IRRECOUVRABLE, company).fetch();
	}
	
	
	/**
	 * Fonction permettant de récupérer la liste des échéances rejetées à passer une irrécouvrable d'une société
	 * @param company
	 * 			Une société
	 * @return
	 */
	public List<PaymentScheduleLine> getPaymentScheduleLineList(Company company)   {
		return PaymentScheduleLine
				.filter("self.paymentSchedule.irrecoverableStateSelect = ?1 AND self.paymentSchedule.company = ?2 AND self.paymentSchedule.state = '4' AND self.rejectMoveLine.amountRemaining > 0 ORDER BY self.scheduleDate ASC", IAccount.TO_PASS_IN_IRRECOUVRABLE, company).fetch();
	}
	
	
	/**
	 * Fonction permettant de récupérer les factures à passer en irrécouvrable d'un tiers
	 * @param company
	 * 			Une société
	 * @param payerPartner
	 * 			Un tiers payeur
	 * @param allInvoiceList
	 * 			La liste des factures à passer en irrécouvrable de la société
	 * @return
	 */
	public List<Invoice> getInvoiceList(Partner partner,Set<Invoice> allInvoiceList)  {
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		
		for(Invoice invoice : allInvoiceList)  {
			if(invoice.getPartner().equals(partner))  {
				invoiceList.add(invoice);
			}
		}
		
		LOG.debug("Nombre de facture à passer en irrécouvrable pour le tiers : {}", invoiceList.size());
		
		return invoiceList;
	}
	
	
	/**
	 * Fonction permettant de récupérer les échéances rejetées à passer en irrécouvrable d'un tiers
	 * @param company
	 * 			Une société
	 * @param payerPartner
	 * 			Un tiers payeur
	 * @param allPaymentScheduleLineList
	 * 			La liste des échéances rejetées à passer en irrécouvrable de la société
	 * @return
	 */
	public List<PaymentScheduleLine> getPaymentScheduleLineList(Partner payerPartner, Set<PaymentScheduleLine> allPaymentScheduleLineList)   {
		List<PaymentScheduleLine> paymentScheduleLineList = new ArrayList<PaymentScheduleLine>();
		
		for(PaymentScheduleLine paymentScheduleLine : allPaymentScheduleLineList)  {
			if(paymentScheduleLine.getPaymentSchedule().getPartner().equals(payerPartner))  {
				paymentScheduleLineList.add(paymentScheduleLine);
			}
		}
		
		LOG.debug("Nombre d'échéances à passer en irrécouvrable pour le tiers : {}", paymentScheduleLineList.size());
		
		return paymentScheduleLineList;
	}
	
	
	/**
	 * Procédure permettant de passer en irrécouvrables les factures et échéances rejetées récupéré sur l'objet Irrécouvrable
	 * @param irrecoverable
	 * 			Un objet Irrécouvrable
	 */
	@Transactional
	public void createIrrecoverableReport(Irrecoverable irrecoverable)  {
				
		Set<Invoice> invoiceSet = irrecoverable.getInvoiceSet();
		Set<PaymentScheduleLine> paymentScheduleLineSet = irrecoverable.getPaymentScheduleLineSet();
		
		irrecoverable.setMoveSet(new HashSet<Move>());
		
		List<Partner> payerPartnerList = this.getPayerPartnerList(invoiceSet);
		
		EntityTransaction transaction = JPA.em().getTransaction();
		
		int i = 0;
		if(payerPartnerList != null && payerPartnerList.size() != 0)  {
			for(Partner payerPartner : payerPartnerList)  {
				
			    if (!transaction.isActive())  {
			    	transaction.begin();		
			    }
			    
				i++;
				try {
					LOG.debug("Tiers : {}", payerPartner.getName());
					this.createIrrecoverableCustomerLine(
							irrecoverable,
							payerPartner, 
							this.getInvoiceList(payerPartner, invoiceSet), 
							this.getPaymentScheduleLineList(payerPartner, paymentScheduleLineSet));
					irrecoverable.save();
					transaction.commit();
						
					if (i % 50 == 0)  {
						JPA.flush();
						JPA.clear();
					}
					
				} catch (Exception e) {
					TraceBackService.trace(e);
					LOG.error("Bug(Anomalie) généré(e) pour le tiers : {}", payerPartner.getName());
		
				} finally {
					if (!transaction.isActive())  {
						transaction.begin();
					}
				}
			}
		}
	}
	
	
	/**
	 * Fonction permettant de créer une ligne Client
	 * @param irrecoverable
	 * 				Un objet Irrécouvrable
	 * @param payerPartner
	 * 				Un tiers payeur
	 * @param invoiceList
	 * 				Une liste de facture du tiers payeur
	 * @param paymentScheduleLineSet
	 * 				Une liste d'échéancier du tiers payeur
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public IrrecoverableCustomerLine createIrrecoverableCustomerLine(Irrecoverable irrecoverable, Partner payerPartner, List<Invoice> invoiceList, List<PaymentScheduleLine> paymentScheduleLineList) throws AxelorException  {
		IrrecoverableCustomerLine icl = new IrrecoverableCustomerLine();
		icl.setIrrecoverable(irrecoverable);
		icl.save();
		irrecoverable.getIrrecoverableCustomerLineList().add(icl);	
		icl.setPartner(payerPartner);
		icl.setIrrecoverablePaymentScheduleLineLineList(this.createIrrecoverablePaymentScheduleLineLineList(icl, paymentScheduleLineList));
		icl.setIrrecoverableInvoiceLineList(this.createIrrecoverableInvoiceLineList(icl, invoiceList));
				
		LOG.debug("Ligne client : {}", icl);
		
		return icl;
	}
	
	
	
	/**
	 * Procédure permettant de
	 * @param irrecoverable
	 * @throws AxelorException
	 */
	public int passInIrrecoverable(Irrecoverable irrecoverable) throws AxelorException  {
				
		irrecoverable.setMoveSet(new HashSet<Move>());
		
		EntityTransaction transaction = JPA.em().getTransaction();
		
		int anomaly = 0;
		
		this.testCompanyField(irrecoverable.getCompany());
		
		int i = 0;
		if(irrecoverable.getInvoiceSet() != null && irrecoverable.getInvoiceSet().size() != 0)  {
			for(Invoice invoice : irrecoverable.getInvoiceSet())  {
				i++;
				
				if (!transaction.isActive())  {
					transaction.begin();		
				}
				
				try {
					LOG.debug("Facture : {}", invoice.getInvoiceId());
					
					this.createIrrecoverableInvoiceLineMove(irrecoverable, invoice);

					irrecoverable.save();
						
					if (i % 50 == 0)  {
						JPA.flush();
						JPA.clear();
					}
					
				} catch (AxelorException e) {
					anomaly++;
					TraceBackService.trace(new AxelorException(String.format("Facture %s", invoice.getInvoiceId()), e, e.getcategory()), IException.IRRECOVERABLE, irrecoverable.getId());
					LOG.error("Bug(Anomalie) généré(e) pour la facture : {}", invoice.getInvoiceId());	
					
				} catch (Exception e) {
					anomaly++;
					TraceBackService.trace(new Exception(String.format("Facture %s", invoice.getInvoiceId()), e), IException.IRRECOVERABLE, irrecoverable.getId());
					LOG.error("Bug(Anomalie) généré(e) pour la facture : {}", invoice.getInvoiceId());
		
				} finally {
					if (!transaction.isActive())  {
						transaction.begin();
					}
				}
			}		
		}
		if(irrecoverable.getPaymentScheduleLineSet() != null && irrecoverable.getPaymentScheduleLineSet().size() != 0)  {
			for(PaymentScheduleLine paymentScheduleLine : irrecoverable.getPaymentScheduleLineSet())  {
				i++;
				
				if (!transaction.isActive())  {
					transaction.begin();
				}
				
				try {
					LOG.debug("Ligne d'échéancier : {}", paymentScheduleLine.getName());
					
					this.createMoveForPaymentScheduleLineReject(irrecoverable, paymentScheduleLine);

					irrecoverable.save();
						
					if (i % 50 == 0)  {
						JPA.flush();
						JPA.clear();
					}
					
				} catch (AxelorException e) {
					anomaly++;
					TraceBackService.trace(new AxelorException(String.format("Ligne d'échéancier %s", paymentScheduleLine.getName()), e, e.getcategory()), IException.IRRECOVERABLE, irrecoverable.getId());
					LOG.error("Bug(Anomalie) généré(e) pour la ligne d'échéancier : {}", paymentScheduleLine.getName());
					
				} catch (Exception e) {
					anomaly++;
					TraceBackService.trace(new Exception(String.format("Ligne d'échéancier %s", paymentScheduleLine.getName()), e), IException.IRRECOVERABLE, irrecoverable.getId());
					LOG.error("Bug(Anomalie) généré(e) pour la ligne d'échéancier : {}", paymentScheduleLine.getName());
		
				} finally {
					if (!transaction.isActive())  {
						transaction.begin();
					}
				}
			}		
		}
		if (!transaction.isActive())  {
			transaction.begin();			
		}
		irrecoverable.setStatus(Status.findByCode("val"));
		irrecoverable.save();
		transaction.commit();
		
		return anomaly;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createMoveForPaymentScheduleLineReject(Irrecoverable irrecoverable, PaymentScheduleLine paymentScheduleLine) throws AxelorException  {
		
		Move move = this.createIrrecoverableMove(paymentScheduleLine.getRejectMoveLine());
		if(move == null)  {
			throw new AxelorException(String.format("%s :\n Erreur généré lors de la création de l'écriture de passage en irrécouvrable %s",
					GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}
		moveService.validateMove(move);
		irrecoverable.getMoveSet().add(move);
		
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createIrrecoverableInvoiceLineMove(Irrecoverable irrecoverable, Invoice invoice) throws AxelorException  {
		
		BigDecimal prorataRate = this.getProrataRate(invoice, invoice.getRejectMoveLine() != null);
		
		// Ajout de l'écriture générée
		Move move = this.createIrrecoverableMove(invoice, prorataRate, invoice.getRejectMoveLine() != null);
		if(move == null)  {
			throw new AxelorException(String.format("%s :\n Erreur généré lors de la création de l'écriture de passage en irrécouvrable %s",
					GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}
		moveService.validateMove(move);
		irrecoverable.getMoveSet().add(move);
		
		invoice.setIrrecoverableStateSelect(IInvoice.PASSED_IN_IRRECOUVRABLE);
		
		if(invoice.getCanceledPaymentSchedule() != null && this.isAllInvoicePassedInIrrecoverable(invoice.getCanceledPaymentSchedule()))  {
			invoice.getCanceledPaymentSchedule().setIrrecoverableStateSelect(IInvoice.PASSED_IN_IRRECOUVRABLE);
		}
				
	}
	
	
	/**
	 * Fonction permettant de créer une liste de ligne Facture
	 * @param icl
	 * 			Une ligne Client
	 * @param invoiceList
	 * 			Une liste de factures du tiers payeur
	 * @return
	 * @throws AxelorException
	 */
	public List<IrrecoverableInvoiceLine> createIrrecoverableInvoiceLineList(IrrecoverableCustomerLine icl, List<Invoice> invoiceList) throws AxelorException  {
		int seq = 1;
		List<IrrecoverableInvoiceLine> iilList = new ArrayList<IrrecoverableInvoiceLine>();
		for(Invoice invoice : invoiceList)  {
			iilList.add(this.createIrrecoverableInvoiceLine(icl, invoice, seq));
			seq++;
		}
		return iilList;
	}
	
	
	
	/**
	 * Fonction permettant de créer une liste de ligne d'échéance rejetée
	 * @param icl
	 * 			Une ligne Client
	 * @param invoiceList
	 * 			Une liste de d'échéance rejetée du tiers payeur
	 * @return
	 * @throws AxelorException
	 */
	public List<IrrecoverablePaymentScheduleLineLine> createIrrecoverablePaymentScheduleLineLineList(IrrecoverableCustomerLine icl, List<PaymentScheduleLine> paymentScheduleLineList) throws AxelorException  {
		int seq = 1;
		List<IrrecoverablePaymentScheduleLineLine> ipsllList = new ArrayList<IrrecoverablePaymentScheduleLineLine>();
		for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
			ipsllList.add(this.createIrrecoverablePaymentScheduleLineLine(icl, paymentScheduleLine, seq));
			seq++;
		}
		return ipsllList;
	}
	
	
	
	/**
	 * Fonction permettant de créer une ligne Facture
	 * @param icl
	 * 			Une ligne Client
	 * @param invoice
	 * 			Une facture
	 * @param seq
	 * 			Un numéro de séquence
	 * @return
	 * @throws AxelorException
	 */
	public IrrecoverableInvoiceLine createIrrecoverableInvoiceLine(IrrecoverableCustomerLine icl, Invoice invoice, int seq) throws AxelorException  {
		IrrecoverableInvoiceLine iil = new IrrecoverableInvoiceLine();
		iil.setInvoice(invoice);
		iil.setInvoiceLineSeq(seq);
		iil.setIrrecoverableCustomerLine(icl);
		
		BigDecimal prorataRate = this.getProrataRate(invoice, invoice.getRejectMoveLine() != null);

		iil.setIrrecoverableReportLineList(this.createIrrecoverableReportLineList(iil, invoice, prorataRate));

		LOG.debug("Ligne facture : {}", iil);
		
		return iil;
	}
	
	
	/**
	 * Fonction permettant de créer une ligne Echéance rejetée
	 * @param icl
	 * 			Une ligne Client
	 * @param invoice
	 * 			Une échéance rejetée
	 * @param seq
	 * 			Un numéro de séquence
	 * @return
	 * @throws AxelorException
	 */
	public IrrecoverablePaymentScheduleLineLine createIrrecoverablePaymentScheduleLineLine(IrrecoverableCustomerLine icl, PaymentScheduleLine paymentScheduleLine, int seq) throws AxelorException  {
		IrrecoverablePaymentScheduleLineLine ipsll = new IrrecoverablePaymentScheduleLineLine();
		ipsll.setPaymentScheduleLine(paymentScheduleLine);
		ipsll.setIrrecoverableCustomerLine(icl);
		
		Company company = paymentScheduleLine.getPaymentSchedule().getCompany();
		
		Tax tax = accountConfigService.getIrrecoverableStandardRateTax(accountConfigService.getAccountConfig(company));

		ipsll.setIrrecoverableReportLineList(this.createIrrecoverableReportLineList(ipsll, paymentScheduleLine, tax));

		LOG.debug("Ligne échéance rejetée : {}", ipsll);
		
		return ipsll;
	}
	
	
	
	
	
	
	/**
	 * Fonction permettant de savoir si toutes les factures à passer en irrécouvrable d'un échéancier à passer en irrécouvrable, 
	 * ont été passées en irrécouvrable
	 * @param paymentSchedule
	 * 			Un échéancier
	 * @return
	 */
	public boolean isAllInvoicePassedInIrrecoverable (PaymentSchedule paymentSchedule)  {
		for(Invoice invoiceScheduled : paymentSchedule.getInvoiceSet())  {
			if(invoiceScheduled.getIrrecoverableStateSelect().equals(IInvoice.TO_PASS_IN_IRRECOUVRABLE))  {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Fonction permettant de créer une liste de ligne de reporting pour une ligne Facture
	 * @param iil
	 * 			Une ligne Facture
	 * @param invoice
	 * 			Une facture
	 * @param prorataRate
	 * 			Un taux de restant à payer d'une facture
	 * @return
	 */
	public List<IrrecoverableReportLine> createIrrecoverableReportLineList(IrrecoverableInvoiceLine iil, Invoice invoice, BigDecimal prorataRate)  {
		int seq = 1;
		List<IrrecoverableReportLine> irlList = new ArrayList<IrrecoverableReportLine>();

		for(InvoiceLine invoiceLine : consolidateInvoiceLine(invoice.getInvoiceLineList()))  {
			irlList.add(this.createIrrecoverableReportLine(iil, invoiceLine.getInvoiceLineType().getName(), invoiceLine.getExTaxTotal().multiply(prorataRate).setScale(2, RoundingMode.HALF_EVEN), seq));
			seq++;
		}
		for(InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList())  {
			irlList.add(this.createIrrecoverableReportLine(iil, invoiceLineTax.getTaxLine().getTax().getName(), invoiceLineTax.getTaxTotal().multiply(prorataRate).setScale(2, RoundingMode.HALF_EVEN), seq));
			seq++;
		}
		// Afin de ne pas modifier les valeurs des lignes de factures, on les recharges depuis la base
		invoice.refresh();
		return irlList;
	}
	
	
	/**
	 * Fonction permettant de créer une liste de ligne de reporting pour une ligne Echéance rejetée
	 * @param iil
	 * 			Une ligne Echéance rejetée
	 * @param invoice
	 * 			Une échéance rejetée
	 * @param prorataRate
	 * 			Un taux de restant à payer d'une échéance rejetée
	 * @return
	 * @throws AxelorException 
	 */
	public List<IrrecoverableReportLine> createIrrecoverableReportLineList(IrrecoverablePaymentScheduleLineLine ipsll, PaymentScheduleLine paymentScheduleLine, Tax tax) throws AxelorException  {
		List<IrrecoverableReportLine> irlList = new ArrayList<IrrecoverableReportLine>();

		BigDecimal taxRate = taxService.getTaxRate(tax, date);
		
		BigDecimal amount = paymentScheduleLine.getInTaxAmount();

		BigDecimal divid = taxRate.add(BigDecimal.ONE);
		
		// Montant hors-Taxe
		BigDecimal irrecoverableAmount = amount.divide(divid, 6, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
		
		// Montant Tax
		BigDecimal taxAmount = amount.subtract(irrecoverableAmount);
		
		irlList.add(this.createIrrecoverableReportLine(ipsll, "HT", irrecoverableAmount, 1));
		
		irlList.add(this.createIrrecoverableReportLine(ipsll, tax.getName(), taxAmount, 2));
	
		return irlList;
	}

	
	/**
	 * Fonction permettant de créer une ligne Reporting
	 * @param iil
	 * 			Une ligne Facture 
	 * @param label
	 * 			Un libellé
	 * @param value
	 * 			Une valeur
	 * @param seq
	 * 			Un numéro de séquence
	 * @return
	 */
	public IrrecoverableReportLine createIrrecoverableReportLine(IrrecoverableInvoiceLine iil, String label, BigDecimal value, int seq)  {
		IrrecoverableReportLine irl = new IrrecoverableReportLine();
		irl.setReportLineSeq(seq);
		irl.setLabel(label);
		irl.setValue(value);
		irl.setIrrecoverableInvoiceLine(iil);
		
		LOG.debug("Ligne reporting : {}", irl);
		
		return irl;
	}
	
	
	/**
	 * Fonction permettant de créer une ligne Reporting
	 * @param iil
	 * 			Une ligne Echéance rejetée 
	 * @param label
	 * 			Un libellé
	 * @param value
	 * 			Une valeur
	 * @param seq
	 * 			Un numéro de séquence
	 * @return
	 */
	public IrrecoverableReportLine createIrrecoverableReportLine(IrrecoverablePaymentScheduleLineLine ipsll, String label, BigDecimal value, int seq)  {
		IrrecoverableReportLine irl = new IrrecoverableReportLine();
		irl.setReportLineSeq(seq);
		irl.setLabel(label);
		irl.setValue(value);
		irl.setIrrecoverablePaymentScheduleLineLine(ipsll);
		
		LOG.debug("Ligne reporting : {}", irl);
		
		return irl;
	}
	
	
	/**
	 * Fonction permettant de calculer le taux de restant à payer d'une facture
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 */
	public BigDecimal getProrataRate(Invoice invoice, boolean isInvoiceReject)  {
		BigDecimal prorataRate = null;
		if(isInvoiceReject)  {
			prorataRate = (invoice.getRejectMoveLine().getAmountRemaining()).divide(invoice.getInTaxTotal(), 6, RoundingMode.HALF_EVEN);
		}
		else  {	
			prorataRate = invoice.getInTaxTotalRemaining().divide(invoice.getInTaxTotal(), 6, RoundingMode.HALF_EVEN);
		}
		
		LOG.debug("Taux d'impayé pour la facture {} : {}", invoice.getInvoiceId(), prorataRate);
		
		return prorataRate;
	}
	
	
	/**
	 * Foncion permettant de consolider les lignes de factures
	 * @param invoiceLineList
	 * 			Une liste de lignes d'une facture
	 * @return
	 */
	public List<InvoiceLine> consolidateInvoiceLine(List<InvoiceLine> invoiceLineList)  {
		List<InvoiceLine> consolidateInvoiceLineList = new ArrayList<InvoiceLine>();
		List<InvoiceLineType> consolidateInvoiceLineTypeList = new ArrayList<InvoiceLineType>();

		for(InvoiceLine invoiceLine : invoiceLineList)  {
			if(consolidateInvoiceLineTypeList.contains(invoiceLine.getInvoiceLineType()))  {
				InvoiceLine il = consolidateInvoiceLineList.get(consolidateInvoiceLineTypeList.indexOf(invoiceLine.getInvoiceLineType()));
				il.setExTaxTotal(il.getExTaxTotal().add(invoiceLine.getExTaxTotal()));
			}
			else  {
				consolidateInvoiceLineList.add(invoiceLine);
				consolidateInvoiceLineTypeList.add(invoiceLine.getInvoiceLineType());
			}
		}
		return consolidateInvoiceLineList;
	}
	
	
	/**
	 * Fonction permettant de créer l'écriture de passage en irrécouvrable d'une facture
	 * @param invoice
	 * 			Une facture
	 * @param prorataRate
	 * 			Le taux de restant à payer sur la facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 * @throws AxelorException
	 */
	public Move createIrrecoverableMove(Invoice invoice, BigDecimal prorataRate, boolean isInvoiceReject) throws AxelorException  {
		Company company = invoice.getCompany();
		Partner payerPartner = invoice.getPartner();
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Move
		Move move = moveService.createMove(accountConfig.getIrrecoverableJournal(), company, null, payerPartner, null);
		
		int seq = 1;
		
		BigDecimal amount = BigDecimal.ZERO;
		MoveLine debitMoveLine = null;
		BigDecimal creditAmount = null;
		BigDecimal debitAmount = null;
		if(isInvoiceReject)  {
			creditAmount = invoice.getRejectMoveLine().getAmountRemaining();
			debitAmount = creditAmount;
		}
		else  {
			creditAmount = invoice.getInTaxTotalRemaining();
			debitAmount = creditAmount;
		}
		
		// Debits MoveLines Tva
		for(InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList())  {
			amount = (invoiceLineTax.getTaxTotal().multiply(prorataRate)).setScale(2, RoundingMode.HALF_EVEN);
			debitMoveLine = moveLineService.createMoveLine(
					move, payerPartner, taxAccountService.getAccount(invoiceLineTax.getTaxLine().getTax(), company), amount, true, false, date, seq, null);
			move.getMoveLineList().add(debitMoveLine);
			seq++;
			debitAmount = debitAmount.subtract(amount);
		}
		
		// Debit MoveLine 654 (irrecoverable account)
		debitMoveLine = moveLineService.createMoveLine(move, payerPartner, accountConfig.getIrrecoverableAccount(), debitAmount, true, false, date, seq, null);
		move.getMoveLineList().add(debitMoveLine);
	
		seq++;
		
		// Getting customer MoveLine from Facture
		MoveLine customerMoveLine = moveService.getCustomerMoveLineByQuery(invoice);
		if(customerMoveLine == null)  {
			throw new AxelorException(String.format("%s :\n La facture %s ne possède pas de pièce comptable dont le restant à payer est positif",
					GeneralService.getExceptionAccountingMsg(), invoice.getInvoiceId()), IException.INCONSISTENCY);
		}
		customerMoveLine.setIrrecoverableStateSelect(IAccount.PASSED_IN_IRRECOUVRABLE);
		
		// Credit MoveLine Customer account (411, 416, ...)
		MoveLine creditMoveLine = moveLineService.createMoveLine(move, payerPartner, customerMoveLine.getAccount(), creditAmount, false, false, date, seq, null);
		move.getMoveLineList().add(creditMoveLine);
		
		Reconcile reconcile = reconcileService.createReconcile(customerMoveLine, creditMoveLine, creditAmount);
		reconcileService.confirmReconcile(reconcile);
		
		return move;
	}
	
	
	/**
	 * Fonction permettant de créer l'écriture de passage en irrécouvrable d'une échéance
	 * @param moveLine
	 * 			Une écriture d'échéance
	 * @return
	 * @throws AxelorException
	 */
	public Move createIrrecoverableMove(MoveLine moveLine) throws AxelorException  {
		
		Company company = moveLine.getMove().getCompany();
		Partner payerPartner = moveLine.getPartner();
		BigDecimal amount = moveLine.getAmountRemaining();
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		// Move
		Move move = moveService.createMove(accountConfig.getIrrecoverableJournal(), company, null, payerPartner, null);

		int seq = 1;
		
		// Credit MoveLine Customer account (411, 416, ...)
		MoveLine creditMoveLine = moveLineService.createMoveLine(move, payerPartner, moveLine.getAccount(), amount, false, false, date, seq, null);
		move.getMoveLineList().add(creditMoveLine);
		
		Reconcile reconcile = reconcileService.createReconcile(moveLine, creditMoveLine, amount);
		reconcileService.confirmReconcile(reconcile);
		
		Tax tax = accountConfig.getIrrecoverableStandardRateTax();
		
		BigDecimal taxRate = taxService.getTaxRate(tax, date);
		
		// Debit MoveLine 654. (irrecoverable account)
		BigDecimal divid = taxRate.add(BigDecimal.ONE);
		BigDecimal irrecoverableAmount = amount.divide(divid, 6, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
		MoveLine creditMoveLine1 = moveLineService.createMoveLine(move, payerPartner, accountConfig.getIrrecoverableAccount(), irrecoverableAmount, true, false, date, 2, null);
		move.getMoveLineList().add(creditMoveLine1);

		// Debit MoveLine 445 (Tax account)
		Account taxAccount = taxAccountService.getAccount(tax, company);
		BigDecimal taxAmount = amount.subtract(irrecoverableAmount);
		MoveLine creditMoveLine2 = moveLineService.createMoveLine(move, payerPartner, taxAccount, taxAmount, true, false, date, 3, null);
		move.getMoveLineList().add(creditMoveLine2);
		
		return move;
	}
	
	
	/**
	 * Fonction permettant de créer un objet de gestion
	 * @param code
	 * @param message
	 * @return
	 */
	public ManagementObject createManagementObject(String code, String message)  {
		ManagementObject managementObject = ManagementObject.filter("self.code = ?1 AND self.name = ?2", code, message).fetchOne();
		if(managementObject != null)  { return managementObject;  }
		
		managementObject = new ManagementObject();
		managementObject.setCode(code);
		managementObject.setName(message);
		return managementObject;
	}
	
	
	/**
	 * Procédure permettant de vérifier les champs d'une société
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		accountConfigService.getIrrecoverableAccount(accountConfig);
		accountConfigService.getIrrecoverableJournal(accountConfig);
		accountConfigService.getIrrecoverableStandardRateTax(accountConfig);
		
	}
	
	
	public String getSequence(Company company) throws AxelorException  {
		
		String seq = sequenceService.getSequence(IAdministration.IRRECOVERABLE, company, false);
		if(seq == null) {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de Passage en irrécouvrable pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return seq;
		
	}
	
	
	/**
	 * Procédure permettant de passer une facture en irrécouvrable
	 * @param invoice
	 * 			Une facture
	 * @param generateEvent
	 * 			Un évènement à t'il déjà été créé par un autre objet ?
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void passInIrrecoverable(Invoice invoice, boolean generateEvent) throws AxelorException  {
		invoice.setIrrecoverableStateSelect(IInvoice.TO_PASS_IN_IRRECOUVRABLE);
		
		if(generateEvent)  {
			Company company = invoice.getCompany();
			
			ManagementObject managementObject = this.createManagementObject("IRR", accountConfigService.getIrrecoverableReasonPassage(accountConfigService.getAccountConfig(company)));
			invoice.setManagementObject(managementObject);
			
			MoveLine moveLine = moveService.getCustomerMoveLineByQuery(invoice);
			
			if(moveLine == null)  {
				throw new AxelorException(String.format("%s :\n La facture %s ne possède pas de pièce comptable dont le restant à payer est positif",
						GeneralService.getExceptionAccountingMsg(),invoice.getInvoiceId()), IException.INCONSISTENCY);
			}
			
			this.passInIrrecoverable(moveLine, managementObject, false);
			
		}
		
		invoice.save();
	}
	
	
	/**
	 * Procédure permettant de passer une facture en irrécouvrable
	 * @param invoice
	 * 			Une facture
	 * @param managementObject
	 * 			Un objet de gestion (utilisé si procédure appelée depuis un autre objet)
	 * @throws AxelorException
	 */
	public void passInIrrecoverable(Invoice invoice, ManagementObject managementObject) throws AxelorException  {
		this.passInIrrecoverable(invoice, false);
		invoice.setManagementObject(managementObject);
		MoveLine moveLine = moveService.getCustomerMoveLineByQuery(invoice);
		
		if(moveLine == null)  {
			throw new AxelorException(String.format("%s :\n La facture %s ne possède pas de pièce comptable dont le restant à payer est positif",
					GeneralService.getExceptionAccountingMsg(),invoice.getInvoiceId()), IException.INCONSISTENCY);
		}
		
		this.passInIrrecoverable(moveLine, managementObject, false);
		
		invoice.save();
	}
	
	
	/**
	 * Procédure permettant d'annuler le passage en irrécouvrable d'une facture
	 * @param invoice
	 * 			Une facture
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void notPassInIrrecoverable(Invoice invoice) throws AxelorException  {
		invoice.setIrrecoverableStateSelect(IInvoice.NOT_IRRECOUVRABLE);
		
		MoveLine moveLine = moveService.getCustomerMoveLineByQuery(invoice);
		
		if(moveLine != null)  {
			this.notPassInIrrecoverable(moveLine, false);
		}
		
		invoice.save();
	}
	
	
	/**
	 * Procédure permettant de passer en irrécouvrable une ligne d'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @param generateEvent
	 * 			Un évènement à t'il déjà été créé par un autre objet ?
	 * @param passInvoice
	 * 			La procédure doit-elle passer aussi en irrécouvrable la facture ?
	 * 			
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void passInIrrecoverable(MoveLine moveLine, boolean generateEvent, boolean passInvoice) throws AxelorException  {
		moveLine.setIrrecoverableStateSelect(IInvoice.TO_PASS_IN_IRRECOUVRABLE);
		ManagementObject managementObject = null;
		if(generateEvent)  {
			Company company = moveLine.getMove().getCompany();

			managementObject = this.createManagementObject("IRR", accountConfigService.getIrrecoverableReasonPassage(accountConfigService.getAccountConfig(company)));
			moveLine.setManagementObject(managementObject);
			
		}
		
		if(moveLine.getMove().getInvoice() != null && passInvoice)  {
			this.passInIrrecoverable(moveLine.getMove().getInvoice(), managementObject);
		}
		
		moveLine.save();
	}
	
	
	/**
	 * Procédure permettant d'annuler le passage en irrrécouvrable d'une ligne d'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @param managementObject
	 * 			Un objet de gestion (utilisé si procédure appelée depuis un autre objet)
	 * @param passInvoice
	 * 			La procédure doit-elle passer aussi en irrécouvrable la facture ?
	 * 
	 * @throws AxelorException
	 */
	public void passInIrrecoverable(MoveLine moveLine, ManagementObject managementObject, boolean passInvoice) throws AxelorException  {
		this.passInIrrecoverable(moveLine, false, passInvoice);
		
		moveLine.setManagementObject(managementObject);
		
		moveLine.save();
	}
	
	
	/**
	 * Procédure permettant d'annuler le passage en irrrécouvrable d'une ligne d'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @param passInvoice
	 * 			La procédure doit-elle passer aussi en irrécouvrable la facture ?
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void notPassInIrrecoverable(MoveLine moveLine, boolean passInvoice) throws AxelorException  {
		moveLine.setIrrecoverableStateSelect(IInvoice.NOT_IRRECOUVRABLE);
		
		if(moveLine.getMove().getInvoice() != null && passInvoice)  {
			this.notPassInIrrecoverable(moveLine.getMove().getInvoice());
		}
		
		moveLine.save();
	}
	
	
	/**
	 * Procédure permettant de passer un échéancier de lissage de paiement en irrécouvrable
	 * La procédure passera aussi les lignes d'écriture de rejet d'échéance en irrécouvrable, 
	 * ainsi que les factures pas complètement payée selectionnées sur l'échéancier 
	 * 
	 * @param paymentSchedule
	 * 				Un échéancier de paiement
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void passInIrrecoverable(PaymentSchedule paymentSchedule) throws AxelorException  {
		Company company = paymentSchedule.getCompany();
		
		paymentSchedule.setIrrecoverableStateSelect(IInvoice.TO_PASS_IN_IRRECOUVRABLE);
		
		ManagementObject managementObject = this.createManagementObject("IRR", accountConfigService.getIrrecoverableReasonPassage(accountConfigService.getAccountConfig(company)));
		paymentSchedule.setManagementObject(managementObject);
		
		List<MoveLine> paymentScheduleLineRejectMoveLineList = new ArrayList<MoveLine>();
		
		for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
			if(paymentScheduleLine.getRejectMoveLine() != null && paymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				paymentScheduleLineRejectMoveLineList.add(paymentScheduleLine.getRejectMoveLine());
			}
		}
		
		for(MoveLine moveLine : paymentScheduleLineRejectMoveLineList)  {
			this.passInIrrecoverable(moveLine, managementObject, true);
		}
		
		for(Invoice invoice : paymentSchedule.getInvoiceSet())  {
			if(invoice.getInTaxTotalRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				this.passInIrrecoverable(invoice, managementObject);
			}
		}
		
		paymentScheduleService.cancelPaymentSchedule(paymentSchedule);
		
		paymentSchedule.save();
	}
	
	
	/**
	 * Procédure permettant d'annuler le passage en irrécouvrable d'une échéancier de lissage de paiement
	 * La procédure annulera aussi le passage en irrécouvrable des lignes d'écriture de rejet d'échéance en irrécouvrable, 
	 * ainsi que des factures pas complètement payée selectionnées sur l'échéancier 
	 * @param paymentSchedule
	 * 			Un échéancier de paiement
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void notPassInIrrecoverable(PaymentSchedule paymentSchedule) throws AxelorException  {
		paymentSchedule.setIrrecoverableStateSelect(IInvoice.NOT_IRRECOUVRABLE);
		
		List<MoveLine> paymentScheduleLineRejectMoveLineList = new ArrayList<MoveLine>();
		
		for(PaymentScheduleLine paymentScheduleLine : paymentSchedule.getPaymentScheduleLineList())  {
			if(paymentScheduleLine.getRejectMoveLine() != null && paymentScheduleLine.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
				paymentScheduleLineRejectMoveLineList.add(paymentScheduleLine.getRejectMoveLine());
			}
		}
		
		for(MoveLine moveLine : paymentScheduleLineRejectMoveLineList)  {
			this.notPassInIrrecoverable(moveLine, false);
		}
		
		for(Invoice invoice : paymentSchedule.getInvoiceSet())  {
			this.notPassInIrrecoverable(invoice);
		}
		paymentSchedule.save();
	}
}
