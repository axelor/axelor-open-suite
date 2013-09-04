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
package com.axelor.apps.account.service.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoice;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherService  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherService.class); 
	
	@Inject
	private ReconcileService rs;
	
	@Inject 
	private MoveLineService mls;
	
	@Inject 
	private MoveService ms;
	
	@Inject
	private PaymentService pas;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private PaymentScheduleService pss;
	
	@Inject
	private PaymentModeService pms;
	
	@Inject
	private PaymentInvoiceToPayService pitps;
	
	@Inject
	private CurrencyService cs;

	private DateTime todayTime;

	@Inject
	public PaymentVoucherService() {

		this.todayTime = GeneralService.getTodayDateTime();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher createPaymentVoucherIPO(Invoice invoice, DateTime dateTime, BigDecimal amount, PaymentMode paymentMode) throws AxelorException  {
		MoveLine customerMoveLine = mls.getCustomerMoveLine(invoice, invoice.getRejectMoveLine() != null);
		
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - facture : {}",invoice.getInvoiceId());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - mode de paiement : {}",paymentMode.getCode());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - société : {}",invoice.getCompany().getName());  }
		if (LOG.isDebugEnabled())  {  LOG.debug("Création d'une saisie paiement par TIP ou TIP chèque - tiers payeur : {}",invoice.getClientPartner().getName());  }
		
		PaymentVoucher paymentVoucher = this.createPaymentVoucher(invoice.getCompany(), 
				null, 
				paymentMode, 
				dateTime, 
				invoice.getClientPartner(), 
				amount, 
				null,
				invoice,
				null, 
				null, 
				null);
		
		paymentVoucher.setAutoOk(true);
		
		List<PaymentInvoiceToPay> lines = new ArrayList<PaymentInvoiceToPay>();
	
		lines.add(pitps.createPaymentInvoiceToPay(paymentVoucher, 
				1, 
				invoice, 
				customerMoveLine, 
				customerMoveLine.getDebit(), 
				customerMoveLine.getAmountRemaining(),
				amount));
		
		paymentVoucher.setPaymentInvoiceToPayList(lines);
		
		paymentVoucher.save();
		
		this.confirmPaymentVoucher(paymentVoucher, false);
		return paymentVoucher;
	}
	
	
	/**
	 * Generic method to create a payment voucher
	 * @param seq
	 * @param pm
	 * @param partner
	 * @return
	 * @throws AxelorException 
	 */
	public PaymentVoucher createPaymentVoucher(Company company, String seq, PaymentMode pm, DateTime dateTime, Partner partner, BigDecimal amount, MoveLine ml, Invoice invoiceToPay, MoveLine rejectToPay, PaymentScheduleLine scheduleToPay, PaymentSchedule paymentScheduleToPay) throws AxelorException  {
		
		LOG.debug("\n\n createPaymentVoucher ....");
		DateTime dateTime2 = dateTime;
		if(dateTime2 == null)  {
			dateTime2 = this.todayTime;
		}
		
		BigDecimal amount2 = amount;
		if(amount2 == null )  {
			amount2 = BigDecimal.ZERO;
		}
		
		//create the move
		PaymentVoucher pv= new PaymentVoucher();
		if (company != null && pm != null && partner != null){
			pv.setCompany(company);
			pv.setPaymentMode(pm);
			pv.setPartner(partner);
			pv.setPaymentDateTime(dateTime2);
			pv.setMoveLine(ml);
			
			pv.setInvoiceToPay(invoiceToPay);
			pv.setRejectToPay(rejectToPay);
			pv.setPaymentScheduleToPay(paymentScheduleToPay);
			pv.setScheduleToPay(scheduleToPay);
			pv.setPaidAmount(amount2);
		
			if (seq != null){
				LOG.debug("if : : : :");
				pv.setRef(seq);
			}
			else{
				LOG.debug("else : : : :");
				
				if(pm.getBankJournal() == null)  {
					throw new AxelorException(String.format("%s :\n Merci de paramétrer un journal pour le mode de paiement {}", 
							GeneralService.getExceptionAccountingMsg(), pm.getName()), IException.CONFIGURATION_ERROR);
				}
				
				String sequence = sgs.getSequence(IAdministration.PAYMENT_VOUCHER, company, pm.getBankJournal(), false);
				if(sequence != null)  {
					pv.setRef(sequence);
				}
				else  {
					throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de saisie paiement pour la société %s et le journal %s.", 
							GeneralService.getExceptionAccountingMsg(), company.getName(), pm.getBankJournal().getName()), IException.CONFIGURATION_ERROR);
				}
			}
			LOG.debug("End createPaymentVoucher IF.");
			return pv;
		}
		else{
			LOG.debug("End createPaymentVoucher ELSE.");
			return null;
		}
	}
	
	
	/**
	 * Searching move lines to pay
	 * @param pv paymentVoucher
	 * @param mlToIgnore moveLine list to ignore
	 * @return moveLines a list of moveLines
	 */
	public List<MoveLine> getMoveLines(PaymentVoucher pv, MoveLine excludeMoveLine) {
		LOG.debug("In getMoveLines ....");
		List<MoveLine> moveLines = null;
		
		moveLines = MoveLine
					.all()
					.filter("partner = ?1 and debit > 0 " +
							"and account.reconcileOk = 't' " +
							"and amountRemaining > 0 " +
							"and move.state = 'validated' " +
							"and ignoreInReminderOk = 'f' " +
							"and move.company = ?2 "
							,pv.getPartner(), pv.getCompany()).fetch();
		
		moveLines.remove(excludeMoveLine);
		
		LOG.debug("Move lines : : : : {}",moveLines);
		LOG.debug("End getMoveLines.");
		return moveLines;
	}
	
	
	/**
	 * According to the passed invoice, get the debit line to pay
	 * @param invoice
	 * @return moveLine a moveLine
	 */
	public MoveLine getInvoiceDebitMoveline(Invoice invoice) {
		LOG.debug("In getInvoiceDebitMoveline ....");
		if (invoice.getMove() != null && invoice.getMove().getMoveLineList() != null)  {
			for (MoveLine moveLine : invoice.getMove().getMoveLineList())  {
				if ((moveLine.getAccount().equals(invoice.getPartnerAccount())) && moveLine.getAccount().getReconcileOk() && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
					return moveLine;
				}	
			}
		}
		LOG.debug("End getInvoiceDebitMoveline.");
		return null;
	}
	
	/**
	 * According to the passed invoice, get the debit line to pay
	 * @param invoice
	 * @return moveLine a moveLine
	 */
	public MoveLine getInvoiceCreditMoveline(Invoice invoice) {
		LOG.debug("In getInvoiceDebitMoveline ....");
		if (invoice.getMove() != null && invoice.getMove().getMoveLineList() != null)  {
			for (MoveLine moveLine : invoice.getMove().getMoveLineList())  {
				if ((moveLine.getAccount().equals(invoice.getPartnerAccount())) && moveLine.getAccount().getReconcileOk() && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
					return moveLine;
				}	
			}
		}
		LOG.debug("End getInvoiceDebitMoveline.");
		return null;
	}
	
	
	/**
	 * Allows to load the moveLine selected in header (invoice, schedule or rejected moveLine) directly in the 2nd O2M
	 * @param paymentVoucher
	 * @param moveLine
	 * @param lineSeq
	 * @param paymentVoucherContext
	 * @return
	 * @throws AxelorException 
	 */
	public List<PaymentInvoiceToPay> loadOneLine(PaymentVoucher paymentVoucher,MoveLine moveLine,int lineSeq) throws AxelorException{
		LOG.debug("In loadOneLine ....");

		List<PaymentInvoiceToPay> piToPayLine = new ArrayList<PaymentInvoiceToPay>();
		
		PaymentInvoiceToPay piToPay = new PaymentInvoiceToPay();
		if (paymentVoucher.getPaidAmount() == null)  {
			throw new AxelorException(String.format("%s :\n Merci de renseigner le montant payé svp.", GeneralService.getExceptionAccountingMsg()), IException.MISSING_FIELD);
		}
		
		if(moveLine != null)  {
			if(moveLine.getPaymentScheduleLine() != null && !moveLine.getPaymentScheduleLine().getFromReject())  {
				piToPay.setPaymentScheduleLine(moveLine.getPaymentScheduleLine());
			}
			
			Move move = moveLine.getMove();
			
			BigDecimal paidAmount = null;
			
			// Si la facture a une devise différente du tiers (de l'écriture)
			if(move.getInvoice() != null && move.getInvoice().getCurrency() != move.getCurrency())  {
				LOG.debug("La facture a une devise différente du tiers (de l'écriture)");
				piToPay.setCurrency(move.getInvoice().getCurrency());
				piToPay.setTotalAmount(move.getInvoice().getInvoiceInTaxTotal());
				piToPay.setRemainingAmount(move.getInvoice().getInvoiceInTaxTotal().subtract(move.getInvoice().getInvoiceAmountPaid()));
				
				// on convertit le montant imputé de la devise de la saisie paiement vers la devise de la facture
				paidAmount = cs.getAmountCurrencyConverted(move.getInvoice().getCurrency(), paymentVoucher.getCurrency(), piToPay.getRemainingAmount(), paymentVoucher.getPaymentDateTime().toLocalDate());

			}
			// sinon la facture à une devise identique à l'écriture, ou l'écriture ne possède pas de facture
			else  {
				LOG.debug("La facture à une devise identique à l'écriture, ou l'écriture ne possède pas de facture");
				piToPay.setCurrency(move.getCurrency());
				if(moveLine.getDebit().compareTo(moveLine.getCredit()) == 1)  {
					piToPay.setTotalAmount(moveLine.getDebit());
				}
				else  {
					piToPay.setTotalAmount(moveLine.getCredit());
				}
				piToPay.setRemainingAmount(moveLine.getAmountRemaining());
				
				paidAmount = cs.getAmountCurrencyConverted(move.getCurrency(), paymentVoucher.getCurrency(), moveLine.getAmountRemaining(), paymentVoucher.getPaymentDateTime().toLocalDate());
			}
			
			LOG.debug("Montant de la créance {}",paidAmount);
			LOG.debug("Montant réglée de la saisie paiement {}",paymentVoucher.getPaidAmount());
			BigDecimal amountToPay = paidAmount.min(paymentVoucher.getPaidAmount());

			piToPay.setSequence(lineSeq);
			piToPay.setMoveLine(moveLine);
			
			
			
			piToPay.setAmountToPay(amountToPay);
			piToPay.setPaymentVoucher(paymentVoucher);
			piToPayLine.add(piToPay);
			LOG.debug("END loadOneLine.");
			return piToPayLine;
		}
		return piToPayLine;
		
	}
	
	
	public void setNum(PaymentVoucher paymentVoucher) throws AxelorException  {
		if (paymentVoucher.getRef() == null || paymentVoucher.getRef().equals("")){
			
			PaymentMode paymentMode = paymentVoucher.getPaymentMode();
			
			if(paymentMode.getBankJournal() == null)  {
				throw new AxelorException(String.format("%s :\n Merci de paramétrer un journal pour le mode de paiement {}", 
						GeneralService.getExceptionAccountingMsg(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
			}
			
			String sequence = sgs.getSequence(IAdministration.PAYMENT_VOUCHER, paymentVoucher.getCompany(), paymentMode.getBankJournal(), false);
			if(sequence != null)  {
				paymentVoucher.setRef(sequence);
			}
			else  {
				throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence de saisie paiement pour la société %s et le journal %s.", 
						GeneralService.getExceptionAccountingMsg(), paymentVoucher.getCompany().getName(), paymentMode.getBankJournal().getName()), IException.CONFIGURATION_ERROR);
			}
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void loadMoveLines(PaymentVoucher paymentVoucher) throws AxelorException  {
		MoveLine moveLineToPay = this.getMoveLineToPay (paymentVoucher);
		
		if (paymentVoucher.getPaymentInvoiceToPayList() == null)  {
			paymentVoucher.setPaymentInvoiceToPayList(new ArrayList<PaymentInvoiceToPay>());
			paymentVoucher.getPaymentInvoiceToPayList().addAll(this.loadOneLine(paymentVoucher,moveLineToPay,1));
		}
		else  {
			paymentVoucher.getPaymentInvoiceToPayList().clear();
			paymentVoucher.getPaymentInvoiceToPayList().addAll(this.loadOneLine(paymentVoucher,moveLineToPay,1));
		}
		
		if (paymentVoucher.getPaymentInvoiceList() == null)  {
			paymentVoucher.setPaymentInvoiceList(new ArrayList<PaymentInvoice>());
			paymentVoucher.getPaymentInvoiceList().addAll(this.setPaymentInvoiceList(paymentVoucher, moveLineToPay));
		}
		else  {
			paymentVoucher.getPaymentInvoiceList().clear();
			paymentVoucher.getPaymentInvoiceList().addAll(this.setPaymentInvoiceList(paymentVoucher, moveLineToPay));
		}
		
		this.setNum(paymentVoucher);
		paymentVoucher.save();
		
		
	}
	

	/**
	 * Allows to load selected lines (from 1st 02M) to the 2nd O2M
	 * and dispatching amounts according to amountRemainnig for the loaded move and the paid amount remaining of the paymentVoucher 
	 * @param paymentVoucher
	 * @param paymentVoucherContext
	 * @return 
	 * @return 
	 * @return values Map of data
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PaymentVoucher loadSelectedLines(PaymentVoucher paymentVoucher,PaymentVoucher paymentVoucherContext) throws AxelorException {
		LOG.debug("In loadSelectedLinesService ...");

		List<PaymentInvoice> newPiList = new ArrayList<PaymentInvoice>();

		if (paymentVoucherContext.getPaymentInvoiceList() != null){
			List<PaymentInvoiceToPay> piToPayLine = new ArrayList<PaymentInvoiceToPay>();

			BigDecimal paidAmount = BigDecimal.ZERO;
			if (paymentVoucherContext.getPaidAmount() == null){
				throw new AxelorException(
					String.format("%s :\n Merci de renseigner le montant payé svp.", GeneralService.getExceptionAccountingMsg()), IException.MISSING_FIELD);			
			}
			else{
				paidAmount = paymentVoucherContext.getPaidAmount();
				BigDecimal amountToPay = BigDecimal.ZERO;
				int lineSeq = 1;
				List<PaymentInvoice> pilSelected = new ArrayList<PaymentInvoice>();
				for (PaymentInvoice pilContext : paymentVoucherContext.getPaymentInvoiceList())  {
					PaymentInvoice paymentInvoiceFromContext = PaymentInvoice.find(pilContext.getId());
					LOG.debug("Selected line : : : : {}",paymentInvoiceFromContext);
					LOG.debug("pilContext.isSelected() : : : : {}",pilContext.isSelected());
					if (pilContext.isSelected()){
						pilSelected.add(paymentInvoiceFromContext);
					}
					else{//creation du nouveau tableau des lignes restant à payer sans les lignes déjà sélectionnées
						PaymentInvoice pi = new PaymentInvoice();
						if(paymentInvoiceFromContext.getMoveLine() != null)  {
							pi.setMoveLine(paymentInvoiceFromContext.getMoveLine());
						}
						pi.setInvoiceAmount(paymentInvoiceFromContext.getInvoiceAmount());
						pi.setPaidAmount(paymentInvoiceFromContext.getPaidAmount());
						pi.setDueDate(paymentInvoiceFromContext.getDueDate());
						pi.setPaymentVoucher(paymentInvoiceFromContext.getPaymentVoucher());
						newPiList.add(pi);
					}
				}
		
				paymentVoucher.getPaymentInvoiceList().clear();
				paymentVoucher.getPaymentInvoiceToPayList().clear();
				
				if (pilSelected != null && !pilSelected.isEmpty())  {
					//récupérer les lignes déjà remplies
					// + initialiser le restant à payer
					// + initialiser la sequence
					if (paymentVoucherContext.getPaymentInvoiceToPayList() != null)  {
						for (PaymentInvoiceToPay pToPay : paymentVoucherContext.getPaymentInvoiceToPayList())  {
							PaymentInvoiceToPay piToPayFromContext = PaymentInvoiceToPay.find(pToPay.getId());
							PaymentInvoiceToPay piToPayOld = new PaymentInvoiceToPay();
							piToPayOld.setSequence(piToPayFromContext.getSequence());
							piToPayOld.setMoveLine(piToPayFromContext.getMoveLine());
							if(piToPayFromContext.getMoveLine() != null && piToPayFromContext.getMoveLine().getId() != null)  {
								piToPayOld.setMoveLine(piToPayFromContext.getMoveLine());
							}
							piToPayOld.setPaymentScheduleLine(piToPayFromContext.getPaymentScheduleLine());
							piToPayOld.setTotalAmount(piToPayFromContext.getTotalAmount());
							piToPayOld.setRemainingAmount(piToPayFromContext.getRemainingAmount());
							piToPayOld.setAmountToPay(piToPayFromContext.getAmountToPay());
							piToPayOld.setPaymentVoucher(piToPayFromContext.getPaymentVoucher());
							piToPayLine.add(piToPayOld);
							if (paidAmount.compareTo(BigDecimal.ZERO) > 0)  {
								paidAmount = paidAmount.subtract(piToPayFromContext.getAmountToPay());
							}
							lineSeq += 1;
						}
					}
					LOG.debug("PITOPAY LINE AFTER first FOR : : : : : {}",piToPayLine);
					LOG.debug("Nombre de ligne selectionné {}",pilSelected.size());
					//Ajouter les nouvelles lignes sélectionnées
					//Incrementation de la liste avec les lignes récupérées au dessus piToPayLine
					for (PaymentInvoice pil : pilSelected)  {
						PaymentInvoiceToPay piToPay = new PaymentInvoiceToPay();
						
						MoveLine moveLine = pil.getMoveLine();
						Move move = moveLine.getMove();
						
//						BigDecimal paidAmount = null;
						
						BigDecimal amountRemainingConverted = null;
						
						if(move.getInvoice() != null)  {
							amountRemainingConverted = cs.getAmountCurrencyConverted(move.getInvoice().getCurrency(), 
									paymentVoucher.getCurrency(), 
									pil.getInvoiceAmount().subtract(pil.getPaidAmount()), 
									paymentVoucher.getPaymentDateTime().toLocalDate());
							piToPay.setCurrency(move.getInvoice().getCurrency());
						}
						else  {
							amountRemainingConverted = cs.getAmountCurrencyConverted(move.getCurrency(), 
									paymentVoucher.getCurrency(), 
									pil.getInvoiceAmount().subtract(pil.getPaidAmount()), 
									paymentVoucher.getPaymentDateTime().toLocalDate());
							piToPay.setCurrency(move.getCurrency());
						}
						
//						amountToPay = paidAmount.min(pil.getInvoiceAmount().subtract(pil.getPaidAmount()));
						amountToPay = paidAmount.min(amountRemainingConverted);

						piToPay.setSequence(lineSeq);
						piToPay.setMoveLine(moveLine);
						piToPay.setPaymentScheduleLine(pil.getPaymentScheduleLine());
						piToPay.setTotalAmount(pil.getInvoiceAmount());
						piToPay.setRemainingAmount(pil.getInvoiceAmount().subtract(pil.getPaidAmount()));
						piToPay.setAmountToPay(amountToPay);
						piToPay.setPaymentVoucher(paymentVoucher);
						piToPayLine.add(piToPay);
						paidAmount = paidAmount.subtract(amountToPay);
						lineSeq += 1;
					}
				}
				
				if (piToPayLine != null && !piToPayLine.isEmpty())  {
					paymentVoucher.getPaymentInvoiceToPayList().addAll(piToPayLine);
				}
				paymentVoucher.getPaymentInvoiceList().addAll(newPiList);
			}
		}
		
		paymentVoucher.save();
		LOG.debug("End loadSelectedLinesService.");
		return paymentVoucher;
	}
	
	
	/**
	 * Fonction qui crée une liste des factures ou échéances non payées susceptible de l'être
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 * @param moveLineToPay
	 * 			Une écriture à payer
	 * @return
	 * 			Une liste des factures ou échéances non payées
	 */
	public List<PaymentInvoice> setPaymentInvoiceList(PaymentVoucher paymentVoucher, MoveLine moveLineToPay)  {
		List<MoveLine> moveLineList = this.getMoveLines(paymentVoucher,moveLineToPay);
		List<PaymentInvoice> pil = new ArrayList<PaymentInvoice>();
		for (MoveLine moveLine : moveLineList){
			PaymentInvoice pi = new PaymentInvoice();
			pi.setMoveLine(moveLine);
			pi.setInvoiceAmount(moveLine.getDebit());
			pi.setPaidAmount(moveLine.getAmountPaid());
			pi.setDueDate(moveLine.getDueDate());
			pi.setPaymentVoucher(paymentVoucher);
			if(moveLine.getPaymentScheduleLine() != null && !moveLine.getPaymentScheduleLine().getFromReject())  {
				pi.setPaymentScheduleLine(moveLine.getPaymentScheduleLine());
			}
			Move move = moveLine.getMove();
			if(move.getInvoice() != null)  {  pi.setCurrency(move.getInvoice().getCurrency());  }
			else  {  pi.setCurrency(move.getCurrency());  }
			
			pil.add(pi);
		}
		return pil;
	}

	/**
	 * Fonction permettant de récupérer la prochaine échéance à payer
	 * @param paymentSchedule
	 * 				Un échéancier
	 * @return
	 * 				Une échéance
	 */
	public PaymentScheduleLine getPaymentScheduleLine(PaymentSchedule paymentSchedule)  {

		if(paymentSchedule != null)  {
			List<PaymentScheduleLine> paymentScheduleLineList = PaymentScheduleLine.all()
					.filter("self.paymentSchedule = ?1 ORDER BY self.scheduleDate ASC ",paymentSchedule).fetch();
			
			if(paymentScheduleLineList != null)  {
				for(PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList)  {
					if(paymentScheduleLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
						return paymentScheduleLine;
					}
				}
			}
		}
		LOG.debug("End getPaymentScheduleLine");
		return null;
	}
	
	
	/**
	 * Fonction permettant de récupérer la ligne d'écriture à payer
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 * @return
	 * 			Une écriture à payer
	 * @throws AxelorException
	 */
	public MoveLine getMoveLineToPay (PaymentVoucher paymentVoucher) throws AxelorException  {
		// Paiement d'un rejet | EN : paying a rejected move line
		if(paymentVoucher.getRejectToPay() != null)  {
			return paymentVoucher.getRejectToPay();
		}
		// Paiement d'une échéance | EN : Paying a schedule
		else if(paymentVoucher.getScheduleToPay() != null)  {
			paymentVoucher.getScheduleToPay().getMoveLineGenerated().setPaymentScheduleLine(paymentVoucher.getScheduleToPay());
			return paymentVoucher.getScheduleToPay().getMoveLineGenerated();
		}
		// Paiement d'une facture | EN : Paying an invoice
		else if (paymentVoucher.getInvoiceToPay() != null){
			if (paymentVoucher.getInvoiceToPay().getSchedulePaymentOk() && paymentVoucher.getScheduleToPay() == null)  {
					throw new AxelorException(String.format("%s :\n La facture est selectionnée sur un échéancier dont toutes les échéances sont payées.<br/> Merci de sélectionner une échéance.", 
							GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			}
			else  {		
				if(this.isDebitToPay(paymentVoucher))  {
					return this.getInvoiceDebitMoveline(paymentVoucher.getInvoiceToPay());   
				}
				else  {
					return this.getInvoiceCreditMoveline(paymentVoucher.getInvoiceToPay());   
				}
			}
		}
		return null;
	}
	
	
	
	/**
	 * 
	 * @param paymentVoucher : Une saisie Paiement
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isDebitToPay(PaymentVoucher paymentVoucher) throws AxelorException  {
		boolean isDebitToPay;
		
		switch(paymentVoucher.getOperationTypeSelect())  {
		case 1:
			isDebitToPay = false;
			break;
		case 2:
			isDebitToPay = true;
			break;
		case 3:
			isDebitToPay = true;
			break;
		case 4:
			isDebitToPay = false;
			break;
		
		default:
			throw new AxelorException(String.format("Type de la saisie paiement absent de la saisie paiement %s", paymentVoucher.getRef()), IException.MISSING_FIELD);
		}	
		
		return isDebitToPay;
	}
	
	
	
	/**
	 * 
	 * @param paymentVoucher : Une saisie Paiement
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isPurchase(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		boolean isPurchase;
		
		switch(paymentVoucher.getOperationTypeSelect())  {
		case 1:
			isPurchase = true;
			break;
		case 2:
			isPurchase = true;
			break;
		case 3:
			isPurchase = false;
			break;
		case 4:
			isPurchase = false;
			break;
		
		default:
			throw new AxelorException(String.format("Type de la saisie paiement absent de la saisie paiement %s", paymentVoucher.getRef()), IException.MISSING_FIELD);
		}	
		
		return isPurchase;
	}
	
	

	/**
	 * Confirms the payment voucher
	 * if the selected lines PiToPay 2nd O2M belongs to different companies -> error
	 * I - Payment with an amount
	 * 		If we pay a classical moveLine (invoice, reject ..) -> just create a payment
	 * 		If we pay a schedule 2 payments are created 1st reconciled with the invoice and the second reconciled with the schedule
	 * II - Payment with an excess Payment
	 * 		If we pay a moveLine having the same account, we just reconcile
	 * 		If we pay a with different account -> 1- switch money to the good account 2- reconcile then
	 * @param paymentVoucher
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirmPaymentVoucher(PaymentVoucher paymentVoucher, boolean updateCustomerAccount)  throws AxelorException {
		LOG.debug("In confirmPaymentVoucherService ....");
		this.setNum(paymentVoucher);

		Partner payerPartner = paymentVoucher.getPartner();
		PaymentMode paymentMode = paymentVoucher.getPaymentMode();
		Company company = paymentVoucher.getCompany();
		Journal journal = paymentMode.getBankJournal();
		LocalDate paymentDate = paymentVoucher.getPaymentDateTime().toLocalDate();
		
		boolean scheduleToBePaid = false;
		Account paymentModeAccount = pms.getCompanyAccount(paymentVoucher.getPaymentMode(), company);
		
		this.checkPaymentVoucherField(paymentVoucher, company, paymentModeAccount, journal);	
		
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0 && !journal.getExcessPaymentOk())  {
			throw new AxelorException(String.format("%s :\n Attention - Vous ne pouvez pas régler un montant supérieur aux factures selectionnées.", 
					GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}
			
		if(paymentVoucher.getPayboxPaidOk())  {
			this.checkPayboxAmount(paymentVoucher);
		}
		
		// TODO VEIRIFER QUE LES ELEMENTS A PAYER NE CONCERNE QU'UNE SEULE DEVISE
		
		// TODO RECUPERER DEVISE DE LA PREMIERE DETTE
		Currency currencyToPay = null;
			
		// If paid by a moveline check if all the lines selected have the same account + company
		// Excess payment
		boolean allRight = this.checkIfSameAccount(paymentVoucher.getPaymentInvoiceToPayList(), paymentVoucher.getMoveLine());
		//Check if allright=true (means companies and accounts in lines are all the same and same as in move line selected for paying
		LOG.debug("allRight : {}", allRight);
		
		if (allRight){	scheduleToBePaid = this.toPayWithExcessPayment(paymentVoucher.getPaymentInvoiceToPayList(), paymentVoucher.getMoveLine(), scheduleToBePaid, paymentDate); }
		
		if(paymentVoucher.getMoveLine() == null || (paymentVoucher.getMoveLine() != null && !allRight) || (scheduleToBePaid && !allRight && paymentVoucher.getMoveLine() != null))  {
		
			PaymentScheduleLine lastPaymentScheduleLine = null;
			
			//Manage all the cases in the same way. As if a move line (Excess payment) is selected, we cancel it first
			Move move = ms.createMove(paymentVoucher.getPaymentMode().getBankJournal(),company,null,payerPartner, paymentDate, paymentMode,false, paymentVoucher.getCashRegister());
			
			move.setPaymentVoucher(paymentVoucher);
			
			paymentVoucher.setGeneratedMove(move);
			// Create move lines for payment lines
			BigDecimal paidLineTotal = BigDecimal.ZERO;
			int moveLineNo=1;
			
			boolean isDebitToPay = this.isDebitToPay(paymentVoucher);
			
			for (PaymentInvoiceToPay paymentInvoiceToPay : this.getPaymentInvoiceToPayList(paymentVoucher))  {
				MoveLine moveLineToPay = paymentInvoiceToPay.getMoveLine();
				LOG.debug("PV moveLineToPay debit : {}", moveLineToPay.getDebit());
				LOG.debug("PV moveLineToPay amountPaid : {}", moveLineToPay.getAmountPaid());
//				BigDecimal amountToPay = paymentInvoiceToPay.getAmountToPay();
				PaymentScheduleLine paymentScheduleLine = paymentInvoiceToPay.getPaymentScheduleLine();
				
				BigDecimal amountToPay = this.getAmountCurrencyConverted(moveLineToPay, paymentVoucher, paymentInvoiceToPay.getAmountToPay());
				
				if (amountToPay.compareTo(BigDecimal.ZERO) > 0)  {								
					
					// Rejected MoveLine case (Normal + FromSchedule)
					if(moveLineToPay.getFromSchedulePaymentOk() && paymentScheduleLine == null)  {
						LOG.debug("Reject case");
						paidLineTotal = paidLineTotal.add(amountToPay);
						
						this.toPayRejectMoveLine(move, moveLineNo, payerPartner, moveLineToPay, amountToPay, paymentInvoiceToPay, isDebitToPay, paymentDate, updateCustomerAccount);
						
						moveLineNo +=1;
					}
					// normal moveline (not from payment schedule)
					else if (!moveLineToPay.getFromSchedulePaymentOk())  {
						LOG.debug("Normal case");
						paidLineTotal = paidLineTotal.add(amountToPay);
						
						this.toPayInvoice(move, moveLineNo, payerPartner, moveLineToPay, amountToPay, paymentInvoiceToPay, isDebitToPay, paymentDate, updateCustomerAccount);
					
						moveLineNo +=1;
					}
					// payment of a Schedule move line
					else if(isDebitToPay)  {
						LOG.debug("Schedule payment case");
						paidLineTotal = paidLineTotal.add(amountToPay);
						
						PaymentSchedule paymentSchedule = this.getPaymentSchedule(moveLineToPay.getMove(), payerPartner);
						
						LOG.debug("paymentSchedule : : : : : :{}",paymentSchedule);
						if (paymentSchedule != null)  {
							
							lastPaymentScheduleLine = paymentScheduleLine;
							
							moveLineNo = this.toPayPaymentScheduleLine(paymentInvoiceToPay, paymentSchedule, payerPartner, moveLineNo, 
									amountToPay, company, paymentScheduleLine, moveLineToPay, paymentMode, move, paymentDate, updateCustomerAccount);
						}
					}
				}
			}
			// Create move line for the payment amount
			MoveLine moveLine = null;
			
			// cancelling the moveLine (excess payment) by creating the balance of all the payments
			// on the same account as the moveLine (excess payment)
			// in the else case we create a classical balance on the bank account of the payment mode
			if (paymentVoucher.getMoveLine() != null){
				moveLine = mls.createMoveLine(move,paymentVoucher.getPartner(),paymentVoucher.getMoveLine().getAccount(),
						paymentVoucher.getPaidAmount(),isDebitToPay,false,paymentDate,moveLineNo,false,false,false, null);
				
				Reconcile reconcile = rs.createReconcile(moveLine,paymentVoucher.getMoveLine(),moveLine.getDebit(), !isDebitToPay);
				rs.confirmReconcile(reconcile, updateCustomerAccount);
			}
			else{
				moveLine = mls.createMoveLine(move,payerPartner,paymentModeAccount,
						paymentVoucher.getPaidAmount(),isDebitToPay,false,paymentDate,moveLineNo,false,false,false, null);
			}
			move.getMoveLineList().add(moveLine);
			// Check if the paid amount is > paid lines total
			// Then Use Excess payment on old invoices / moveLines
			if (paymentVoucher.getPaidAmount().compareTo(paidLineTotal) > 0){
				BigDecimal remainingPaidAmount = paymentVoucher.getRemainingAmount();
				
				moveLine = mls.createMoveLine(move,paymentVoucher.getPartner(),company.getCustomerAccount(),
						remainingPaidAmount,!isDebitToPay,false,paymentDate,moveLineNo++,false,false,false, null);
				move.getMoveLineList().add(moveLine);
				
				if(lastPaymentScheduleLine == null || pss.isLastSchedule(lastPaymentScheduleLine))  {
					if(isDebitToPay)  {
						rs.balanceCredit(moveLine, company, updateCustomerAccount);
					}
				}
				
			}
			ms.validateMove(move);
			paymentVoucher.setGeneratedMove(move);
		}
		paymentVoucher.setState("2");
		this.fillReceiptNo(paymentVoucher, company, journal);
		paymentVoucher.save();
	}
	
	
	/**
	 * Récupérer les éléments à payer dans le bon ordre
	 * @return
	 */
	public List<PaymentInvoiceToPay>  getPaymentInvoiceToPayList(PaymentVoucher paymentVoucher)  {
		List<PaymentInvoiceToPay> allPaymentInvoiceToPayList = new ArrayList<PaymentInvoiceToPay>();	
		
		List<PaymentInvoiceToPay> schedulePaymentInvoiceToPayList = PaymentInvoiceToPay.all().
				filter("paymentVoucher = ?1 and paymentScheduleLine is not null ORDER by paymentScheduleLine.scheduleDate ASC", paymentVoucher).fetch();
		
		List<PaymentInvoiceToPay> otherPaymentInvoiceToPayList = PaymentInvoiceToPay.all().	filter("paymentVoucher = ?1 and paymentScheduleLine is null", paymentVoucher).fetch();
		
		if(schedulePaymentInvoiceToPayList!=null)  { allPaymentInvoiceToPayList.addAll(schedulePaymentInvoiceToPayList);  }
		if(otherPaymentInvoiceToPayList!=null)  { allPaymentInvoiceToPayList.addAll(otherPaymentInvoiceToPayList);  }
		return allPaymentInvoiceToPayList;
	}
	
	
	/**
	 * 	 If paid by a moveline check if all the lines selected have the same account + company
	 *	 Excess payment
	 *	Check if allright=true (means companies and accounts in lines are all the same and same as in move line selected for paying
	 * @param paymentInvoiceToPayList
	 * 		  		Liste des paiement a réaliser
	 * @param creditMoveLine
	 * 				Le trop-perçu
	 * @param scheduleToBePaid
	 * @return
	 * 				Une échéance doit-elle être payée?
	 * @throws AxelorException
	 */
	public boolean toPayWithExcessPayment(List<PaymentInvoiceToPay> paymentInvoiceToPayList, MoveLine creditMoveLine, boolean scheduleToBePaid, LocalDate paymentDate) throws AxelorException  {
		boolean scheduleToBePaid2 = scheduleToBePaid;
		
		List<MoveLine> debitMoveLines = new ArrayList<MoveLine>();
		for (PaymentInvoiceToPay paymentInvoiceToPay : paymentInvoiceToPayList)  {
			
			// Récupération des ligne d'écriture de facture de l'échéance
			if(paymentInvoiceToPay.getPaymentScheduleLine() != null && !paymentInvoiceToPay.getPaymentScheduleLine().getFromReject())  {
				scheduleToBePaid2 = true;
				List<MoveLine> moveLineInvoiceToPay = pss.getPaymentSchedulerMoveLineToPay(paymentInvoiceToPay.getPaymentScheduleLine().getPaymentSchedule());
				debitMoveLines.addAll(this.assignMaxAmountToReconcile (moveLineInvoiceToPay, paymentInvoiceToPay.getAmountToPay()));
			
				// Manage the double payment in the case of a payment schedule
				//Copy the payment line and reconcile it with piToPay.moveLine
				this.createSchedulePaymentMoveLine(paymentInvoiceToPay, paymentDate);
				// End copy the payment line
			}
			else  {
				debitMoveLines.add(paymentInvoiceToPay.getMoveLine());
			}
		}	
		List<MoveLine> creditMoveLines = new ArrayList<MoveLine>();
		creditMoveLines.add(creditMoveLine);
		pas.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLines);
		return scheduleToBePaid2;
	}
	
	
	
	/**
	 * Procédure permettant de vérifier le remplissage et le bon contenu des champs de la saisie paiement et de la société
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 * @param company
	 * 			Une société
	 * @param paymentModeAccount
	 * 			Le compte de trésoreie du mode de règlement
	 * @throws AxelorException
	 */
	public void checkPaymentVoucherField(PaymentVoucher paymentVoucher, Company company, Account paymentModeAccount, Journal journal) throws AxelorException  {
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0)  {
			throw new AxelorException(String.format("%s :\n Attention, saisie paiement n° %s, le total des montants imputés par ligne est supérieur au montant payé par le client", 
					GeneralService.getExceptionAccountingMsg(), paymentVoucher.getRef()), IException.INCONSISTENCY);
		}
		
		// Si on a des lignes à payer (dans le deuxième tableau)
		if(!paymentVoucher.getAutoOk() && (paymentVoucher.getPaymentInvoiceToPayList() == null || paymentVoucher.getPaymentInvoiceToPayList().size() == 0))  {
			throw new AxelorException(String.format("%s :\n Aucune ligne à payer.", GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}	
		
		if (company.getCustomerAccount() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un compte client dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		if(journal == null || paymentModeAccount == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez renseigner un journal et un compte de trésorerie dans le mode de règlement.", 
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		
		if(journal.getEditReceiptOk())  {
			String seq = sgs.getSequence(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company, true);
			if(seq == null)  {
				throw new AxelorException(String.format("%s :\n Veuillez configurer une séquence Numéro de reçu (Saisie paiement) pour la société %s",
						GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
			}
		}
	}
	
	
	public void checkPayboxAmount(PaymentVoucher paymentVoucher) throws AxelorException  {
		if(paymentVoucher.getPayboxAmountPaid() != null && paymentVoucher.getPayboxAmountPaid().compareTo(paymentVoucher.getPaidAmount()) != 0)  {
				throw new AxelorException(String.format("%s :\n Le montant de la saisie paiement (%s) est différent du montant encaissé par Paybox (%s)",
						GeneralService.getExceptionAccountingMsg(),paymentVoucher.getPaidAmount(),paymentVoucher.getPayboxAmountPaid()), IException.INCONSISTENCY);
		}
	}
	
	
	/**
	 * Fonction vérifiant si l'ensemble des lignes à payer ont le même compte et que ce compte est le même que celui du trop-perçu
	 * @param paymentInvoiceToPayList
	 * 			La liste des lignes à payer
	 * @param moveLine
	 * 			Le trop-perçu à utiliser
	 * @return
	 */
	public boolean checkIfSameAccount(List<PaymentInvoiceToPay> paymentInvoiceToPayList, MoveLine moveLine)  {
		if(moveLine != null)  {
			Account account = moveLine.getAccount();
			for (PaymentInvoiceToPay paymentInvoiceToPay : paymentInvoiceToPayList)  {
				if(!paymentInvoiceToPay.getMoveLine().getAccount().equals(account))  {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param paymentMove
	 * @param moveLineSeq
	 * @param payerPartner
	 * @param moveLineToPay
	 * @param amountToPay
	 * @param paymentInvoiceToPay
	 * @throws AxelorException
	 */
	public MoveLine toPayRejectMoveLine(Move paymentMove, int moveLineSeq, Partner payerPartner, MoveLine moveLineToPay, BigDecimal amountToPay, 
			PaymentInvoiceToPay paymentInvoiceToPay, boolean isDebitToPay, LocalDate paymentDate, boolean updateCustomerAccount) throws AxelorException  {
		String invoiceName = "";
		if(moveLineToPay.getMove().getInvoice()!=null)  {
			invoiceName = moveLineToPay.getMove().getInvoice().getInvoiceId();
		}
		else  {
			invoiceName = paymentInvoiceToPay.getPaymentVoucher().getRef();
		}
		MoveLine moveLine = mls.createMoveLine(paymentMove,
				payerPartner,
				moveLineToPay.getAccount(),
				amountToPay,
				!isDebitToPay,
				false,
				paymentDate,
				moveLineSeq,
				false,
				false,
				false,
				invoiceName);
		moveLine.setPaymentScheduleLine(moveLineToPay.getPaymentScheduleLine());
		
		paymentMove.getMoveLineList().add(moveLine);
		paymentInvoiceToPay.setMoveLineGenerated(moveLine);

		Reconcile reconcile = null;
		PaymentScheduleLine paymentScheduleLine = moveLineToPay.getPaymentScheduleLine();
		if(pss.isLastSchedule(paymentScheduleLine))  {
			reconcile = rs.createGenericReconcile(moveLineToPay,moveLine,amountToPay, true, false, !isDebitToPay);
		}
		else  {
			reconcile = rs.createGenericReconcile(moveLineToPay,moveLine,amountToPay, false, false, !isDebitToPay);
		}
		LOG.debug("Reconcile : : : {}", reconcile);
		rs.confirmReconcile(reconcile, updateCustomerAccount);
		
		this.closePaymentScheduleLineProcess(moveLineToPay);
		return moveLine;
	}
	
	
	/**
	 * 
	 * @param paymentMove
	 * @param moveLineSeq
	 * @param payerPartner
	 * @param moveLineToPay
	 * @param amountToPay
	 * @param paymentInvoiceToPay
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine toPayInvoice(Move paymentMove, int moveLineSeq, Partner payerPartner, MoveLine moveLineToPay, BigDecimal amountToPay, PaymentInvoiceToPay paymentInvoiceToPay,
			boolean isDebitToPay, LocalDate paymentDate, boolean updateCustomerAccount) throws AxelorException  {
		String invoiceName = "";
		if(moveLineToPay.getMove().getInvoice()!=null)  {
			invoiceName = moveLineToPay.getMove().getInvoice().getInvoiceId();
		}
		else  {
			invoiceName = paymentInvoiceToPay.getPaymentVoucher().getRef();
		}
		MoveLine moveLine = mls.createMoveLine(paymentMove,
				payerPartner,
				moveLineToPay.getAccount(),
				amountToPay,
				!isDebitToPay,
				false,
				paymentDate,
				moveLineSeq,
				false,
				false,
				false,
				invoiceName);
		moveLine.setPaymentScheduleLine(moveLineToPay.getPaymentScheduleLine());
		
		paymentMove.getMoveLineList().add(moveLine);
		paymentInvoiceToPay.setMoveLineGenerated(moveLine);
		
		Reconcile reconcile = rs.createGenericReconcile(moveLineToPay,moveLine,amountToPay,true, false, !isDebitToPay);
		LOG.debug("Reconcile : : : {}", reconcile);
		rs.confirmReconcile(reconcile, updateCustomerAccount);
		return moveLine;
	}
	
	
	
	
	public BigDecimal getAmountCurrencyConverted(MoveLine moveLineToPay, PaymentVoucher paymentVoucher, BigDecimal amountToPay) throws AxelorException  {
		
		Currency moveCurrency = moveLineToPay.getMove().getCurrency();
		
		Currency paymentVoucherCurrency = paymentVoucher.getCurrency();
		
		LocalDate paymentVoucherDate = paymentVoucher.getPaymentDateTime().toLocalDate();
		
		return cs.getAmountCurrencyConverted(paymentVoucherCurrency, moveCurrency, amountToPay, paymentVoucherDate);
		
	}
	
	
	
	
	
	
	public boolean mustBeBalanced(MoveLine moveLineToPay, PaymentVoucher paymentVoucher, BigDecimal amountToPay)  {
		
		Invoice invoice = moveLineToPay.getMove().getInvoice();
		
		Currency invoiceCurrency = invoice.getCurrency();
				
		Currency paymentVoucherCurrency = paymentVoucher.getCurrency();
		
		// Si la devise de paiement est la même que le devise de la facture, 
		// Et que le montant déjà payé en devise sur la facture, plus le montant réglé par la nouvelle saisie paiement en devise, est égale au montant total en devise de la facture
		// Alors on solde la facture
		if(paymentVoucherCurrency.equals(invoiceCurrency) && invoice.getInvoiceAmountPaid().add(amountToPay).compareTo(invoice.getInvoiceInTaxTotal()) == 0)  {
			//SOLDER
			return true;
		}
		
		return false;
		
	}
	
	
	
	/**
	 * 
	 * @param paymentInvoiceToPay
	 * 			 	not null only for paymentVoucher
	 * @param paymentSchedule
	 * @param payerPartner
	 * @param moveLineSeq
	 * @param amountToPay
	 * @param company
	 * @param paymentScheduleLine
	 * @param moveLine
	 * @param paymentMode
	 * @param paymentMove
	 * @throws AxelorException
	 */
	public int toPayPaymentScheduleLine(PaymentInvoiceToPay paymentInvoiceToPay, PaymentSchedule paymentSchedule, Partner payerPartner, int moveLineSeq, 
			BigDecimal amountToPay, Company company, PaymentScheduleLine paymentScheduleLine, MoveLine moveLine, PaymentMode paymentMode, Move paymentMove,
			LocalDate paymentDate, boolean updateCustomerAccount) throws AxelorException  {
		
		int moveLineSeq2 = moveLineSeq;
		
		List<MoveLine> moveLinesToPay = new ArrayList<MoveLine>();
		// Paying a normal schedule line
		if(paymentScheduleLine != null 
				&& !paymentScheduleLine.getFromReject())  {

			List<MoveLine> moveLineInvoiceToPay = pss.getPaymentSchedulerMoveLineToPay(paymentScheduleLine.getPaymentSchedule());
			moveLinesToPay.addAll(this.assignMaxAmountToReconcile (moveLineInvoiceToPay, amountToPay));
		}
		
		moveLineSeq2 = pas.createExcessPaymentWithAmount(moveLinesToPay, amountToPay, 
				paymentMove, moveLineSeq2, payerPartner, company, 
				paymentInvoiceToPay, company.getCustomerAccount(), paymentDate, updateCustomerAccount);

		
		
		// Manage the double payment in the case of a payment schedule
		// Copy the payment line and reconcile it with piToPay.moveLine
		this.createSchedulePaymentMoveLine(paymentScheduleLine, 
				moveLine, 
				company, 
				paymentMode, 
				amountToPay,
				paymentDate,
				updateCustomerAccount);
		// End copy the payment line
		
		this.closePaymentScheduleLineProcess(moveLine);
		return moveLineSeq2;
	}
	
	
	
	/**
	 * Fonction permettant de récupérer un échéancier à payer
	 * @param move
	 * 			Une écriture 
	 * @param payerPartner
	 * 			Un tiers payeur
	 * @return
	 */
	public PaymentSchedule getPaymentSchedule(Move move, Partner payerPartner)  {
		return PaymentSchedule.all()
				.filter("generatedMove = ?1 and payerPartner = ?2"
						, move
						, payerPartner).fetchOne();
	}
	
	
	/**
	 * Procédure permettant de cloturer une échéance de paiement
	 * @param moveLine
	 * 			Une ligne d'écriture d'une échéance de paiement
	 */
	public void closePaymentScheduleLineProcess(MoveLine moveLine)  {
		if(moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) == 0)  {
			LOG.debug("Ligne à cloturer : {}",moveLine);
			this.closePaymentScheduleLineProcess(moveLine.getPaymentScheduleLine());
		}
	}
	
	
	/**
	 * Procédure permettant de cloturer une échéance de paiement
	 * @param paymentScheduleLine
	 * 			Une échéance de paiement
	 */
	public void closePaymentScheduleLineProcess(PaymentScheduleLine paymentScheduleLine)  {
		LOG.debug("Echéance à cloturer : {}",paymentScheduleLine);
		if(paymentScheduleLine != null)  {
			paymentScheduleLine.setStatus(Status.all().filter("self.code = 'clo'").fetchOne());
			paymentScheduleLine.save();
		}
	}
	
	
	
	/**
	 * Pay the schedule
	 * 
	 * Used to Manage the double payment in the case of a payment schedule
	 * Copy the payment line and reconcile it with piToPay.moveLine
	 * @param paymentInvoiceToPay
	 * 				Un ligne de saisie paiement  à payer (2ième O2M)
	 * @param moveLine
	 * 				La ligne d'écriture de paiement
	 * @throws AxelorException
	 */
	public void createSchedulePaymentMoveLine(PaymentInvoiceToPay paymentInvoiceToPay, LocalDate paymentDate) throws AxelorException  {
		this.createSchedulePaymentMoveLine(paymentInvoiceToPay.getPaymentScheduleLine(), 
				paymentInvoiceToPay.getMoveLine(), 
				paymentInvoiceToPay.getCompany(), 
				paymentInvoiceToPay.getPaymentVoucher().getPaymentMode(), 
				paymentInvoiceToPay.getAmountToPay(),
				paymentDate,
				true);
	}
	
	
	/**
	 * Pay the schedule
	 * 
	 * Used to Manage the double payment in the case of a payment schedule
	 * Copy the payment line and reconcile it with piToPay.moveLine
	 * @param paymentInvoiceToPay
	 * 				Un ligne de saisie paiement  à payer (2ième O2M)
	 * @param moveLine
	 * 				La ligne d'écriture de paiement
	 * @throws AxelorException
	 */
	public void createSchedulePaymentMoveLine(PaymentScheduleLine paymentScheduleLine, MoveLine moveLine, Company company, PaymentMode paymentMode, BigDecimal amountToPay,
			LocalDate paymentDate, boolean updateCustomerAccount) throws AxelorException  {
		if(paymentScheduleLine != null 
				&& !paymentScheduleLine.getFromReject())  {
			Partner partner = moveLine.getPartner();
			
			Move move = ms.createMove(company.getTechnicalJournal(), company, null, partner, paymentDate, paymentMode, false, null);
			
			MoveLine copiedMoveLine = mls.createMoveLine(move,
					partner,
					moveLine.getAccount(),
					amountToPay,
					false,
					false,
					paymentDate,
					1,
					true,
					false,
					false,
					null);
		
			move.getMoveLineList().add(copiedMoveLine);
			
			Account paymentModeAccount = pms.getCompanyAccount(paymentMode, company);
			
			MoveLine creditMoveLine = mls.createMoveLine(move,
					partner,
					paymentModeAccount,
					amountToPay,
					true,
					false,
					paymentDate,
					2,
					true,
					false,
					false,
					null);
			move.getMoveLineList().add(creditMoveLine);
			
			ms.validateMove(move, updateCustomerAccount);
			
			LOG.debug("copiedMoveLine : : : : : :{}",copiedMoveLine);
			
			Reconcile reconcile = rs.createReconcile(moveLine,copiedMoveLine,amountToPay);
			LOG.debug("Reconcile with copied move line: : : ", reconcile);		
			rs.confirmReconcile(reconcile, updateCustomerAccount);
		}
	}
	
	
	/**
	 *  
	 * @param moveLineInvoiceToPay
	 * 				Les lignes de factures récupérées depuis l'échéance
	 * @param paymentInvoiceToPay
	 * 				La Ligne de saisie paiement
	 * @return
	 */
	public List<MoveLine> assignMaxAmountToReconcile (List<MoveLine> moveLineInvoiceToPay, BigDecimal amountToPay)  {
		List<MoveLine>  debitMoveLines = new ArrayList<MoveLine>();
		if(moveLineInvoiceToPay != null && moveLineInvoiceToPay.size()!=0)  {
			// Récupération du montant imputé sur l'échéance, et assignation de la valeur dans la moveLine (champ temporaire)
			BigDecimal maxAmountToPayRemaining = amountToPay;
			for(MoveLine moveLine : moveLineInvoiceToPay)  {
				if(maxAmountToPayRemaining.compareTo(BigDecimal.ZERO) > 0)  {
					BigDecimal amountPay = maxAmountToPayRemaining.min(moveLine.getAmountRemaining());
					moveLine.setMaxAmountToReconcile(amountPay);
					debitMoveLines.add(moveLine);
					maxAmountToPayRemaining = maxAmountToPayRemaining.subtract(amountPay);
				}
			}
		}
		return debitMoveLines;
	}
	
	
	public void fillReceiptNo(PaymentVoucher paymentVoucher, Company company, Journal journal)  {
		if(journal.getEditReceiptOk())  {
			String seq = sgs.getSequence(IAdministration.PAYMENT_VOUCHER_RECEIPT_NUMBER, company, false);
			paymentVoucher.setReceiptNo(seq);
		}
	}
	
	
	/**
	 * Procédure permettant d'autauriser la confirmation de la saisie paiement
	 * @param paymentVoucher
	 * 			Une saisie paiement
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void authorizeConfirmPaymentVoucher(PaymentVoucher paymentVoucher, String bankCardTransactionNumber, String payboxAmountPaid)  {
		
		paymentVoucher.setPayboxPaidOk(true);
		paymentVoucher.setBankCardTransactionNumber(bankCardTransactionNumber);
		paymentVoucher.setPayboxAmountPaid(new BigDecimal(payboxAmountPaid).divide(new BigDecimal("100")));
		
		paymentVoucher.save();
	}
	
}
