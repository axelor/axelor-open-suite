/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.paymentvoucher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentInvoice;
import com.axelor.apps.account.db.PaymentInvoiceToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentInvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PaymentVoucherLoadService extends PaymentVoucherRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherLoadService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PaymentVoucherSequenceService paymentVoucherSequenceService;
	
	@Inject
	private PaymentVoucherToolService paymentVoucherToolService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private PaymentInvoiceRepository paymentInvoiceRepo;
	
	@Inject
	private PaymentInvoiceToPayService paymentInvoiceToPayService;
	
	/**
	 * Searching move lines to pay
	 * @param pv paymentVoucher
	 * @param mlToIgnore moveLine list to ignore
	 * @return moveLines a list of moveLines
	 * @throws AxelorException 
	 */
	public List<MoveLine> getMoveLines(PaymentVoucher paymentVoucher, MoveLine excludeMoveLine) throws AxelorException {
		List<? extends MoveLine> moveLines = null;
		
		String query = "self.partner = ?1 " +
				"and self.account.reconcileOk = 't' " +
				"and self.amountRemaining > 0 " +
				"and self.move.statusSelect = ?3 " +
				"and self.move.ignoreInReminderOk = 'f' " +
				"and self.move.company = ?2 ";
		
		if(paymentVoucherToolService.isDebitToPay(paymentVoucher))  {
			query += " and self.debit > 0 ";
		}
		else  {
			query += " and self.credit > 0 ";
		}
		
		moveLines = moveLineService.all().filter(query, paymentVoucher.getPartner(), paymentVoucher.getCompany(), MoveService.STATUS_VALIDATED).fetch();
		
		moveLines.remove(excludeMoveLine);
		
		return (List<MoveLine>) moveLines;
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

		List<PaymentInvoiceToPay> paymentInvoiceToPayList = new ArrayList<PaymentInvoiceToPay>();
		
		PaymentInvoiceToPay paymentInvoiceToPay = new PaymentInvoiceToPay();
		if (paymentVoucher.getPaidAmount() == null)  {
			throw new AxelorException(String.format("%s :\n Merci de renseigner le montant payé svp.", GeneralServiceAccount.getExceptionAccountingMsg()), IException.MISSING_FIELD);
		}
		
		if(moveLine == null)  {  return paymentInvoiceToPayList;  }
			
		Move move = moveLine.getMove();
		
		BigDecimal paidAmount = null;
		
		// Si la facture a une devise différente du tiers (de l'écriture)
		if(move.getInvoice() != null && !move.getInvoice().getCurrency().equals(move.getCurrency()))  {
			LOG.debug("La facture a une devise différente du tiers (de l'écriture)");
			paymentInvoiceToPay.setCurrency(move.getInvoice().getCurrency());
			paymentInvoiceToPay.setTotalAmount(move.getInvoice().getInvoiceInTaxTotal());
			paymentInvoiceToPay.setRemainingAmount(move.getInvoice().getInvoiceInTaxTotal().subtract(move.getInvoice().getInvoiceAmountPaid()));
			
			// on convertit le montant imputé de la devise de la saisie paiement vers la devise de la facture
			paidAmount = currencyService.getAmountCurrencyConverted(paymentVoucher.getCurrency(), move.getInvoice().getCurrency(), paymentInvoiceToPay.getRemainingAmount(), paymentVoucher.getPaymentDateTime().toLocalDate());

		}
		// sinon la facture à une devise identique à l'écriture, ou l'écriture ne possède pas de facture
		else  {
			LOG.debug("La facture à une devise identique à l'écriture, ou l'écriture ne possède pas de facture");
			paymentInvoiceToPay.setCurrency(move.getCurrency());
			if(moveLine.getDebit().compareTo(moveLine.getCredit()) == 1)  {
				paymentInvoiceToPay.setTotalAmount(moveLine.getDebit());
			}
			else  {
				paymentInvoiceToPay.setTotalAmount(moveLine.getCredit());
			}
			paymentInvoiceToPay.setRemainingAmount(moveLine.getAmountRemaining());
			
			paidAmount = currencyService.getAmountCurrencyConverted(paymentVoucher.getCurrency(), move.getCurrency(), moveLine.getAmountRemaining(), paymentVoucher.getPaymentDateTime().toLocalDate());
		}
		
		LOG.debug("Montant de la créance {}",paidAmount);
		LOG.debug("Montant réglée de la saisie paiement {}",paymentVoucher.getPaidAmount());
		BigDecimal amountToPay = paidAmount.min(paymentVoucher.getPaidAmount());

		paymentInvoiceToPay.setSequence(lineSeq);
		paymentInvoiceToPay.setMoveLine(moveLine);
		
		paymentInvoiceToPay.setAmountToPay(amountToPay);
		paymentInvoiceToPay.setPaymentVoucher(paymentVoucher);
		paymentInvoiceToPayList.add(paymentInvoiceToPay);
		
		LOG.debug("END loadOneLine.");
		return paymentInvoiceToPayList;
		
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
		
		paymentVoucherSequenceService.setReference(paymentVoucher);
		save(paymentVoucher);
		
		
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
	public PaymentVoucher loadSelectedLines(PaymentVoucher paymentVoucher, PaymentVoucher paymentVoucherContext) throws AxelorException {
		LOG.debug("In loadSelectedLinesService ...");

		List<PaymentInvoice> newPiList = Lists.newArrayList();

		if (paymentVoucherContext.getPaymentInvoiceList() != null){
			List<PaymentInvoiceToPay> piToPayLine = new ArrayList<PaymentInvoiceToPay>();

			BigDecimal paidAmount = BigDecimal.ZERO;
			if (paymentVoucherContext.getPaidAmount() == null){
				throw new AxelorException(
					String.format("%s :\n Merci de renseigner le montant payé svp.", GeneralServiceAccount.getExceptionAccountingMsg()), IException.MISSING_FIELD);			
			}
			else{
				paidAmount = paymentVoucherContext.getPaidAmount();
				paymentVoucher.setPaidAmount(paidAmount);
				
				int lineSeq = 1;
				List<PaymentInvoice> paymentInvoiceSelectedList = new ArrayList<PaymentInvoice>();
				for (PaymentInvoice pilContext : paymentVoucherContext.getPaymentInvoiceList())  {
					PaymentInvoice paymentInvoiceFromContext = paymentInvoiceRepo.find(pilContext.getId());
					LOG.debug("Selected line : : : : {}",paymentInvoiceFromContext);
					LOG.debug("pilContext.isSelected() : : : : {}",pilContext.isSelected());
					if (pilContext.isSelected()){
						paymentInvoiceSelectedList.add(paymentInvoiceFromContext);
					}
					else{//creation du nouveau tableau des lignes restant à payer sans les lignes déjà sélectionnées
						PaymentInvoice paymentInvoice = new PaymentInvoice();
						if(paymentInvoiceFromContext.getMoveLine() != null)  {
							paymentInvoice.setMoveLine(paymentInvoiceFromContext.getMoveLine());
						}
						paymentInvoice.setInvoiceAmount(paymentInvoiceFromContext.getInvoiceAmount());
						paymentInvoice.setPaidAmount(paymentInvoiceFromContext.getPaidAmount());
						paymentInvoice.setPaymentVoucher(paymentInvoiceFromContext.getPaymentVoucher());
						newPiList.add(paymentInvoice);
					}
				}
		
				paymentVoucher.getPaymentInvoiceList().clear();
				paymentVoucher.getPaymentInvoiceToPayList().clear();
				
				if (paymentInvoiceSelectedList != null && !paymentInvoiceSelectedList.isEmpty())  {
					//récupérer les lignes déjà remplies
					// + initialiser le restant à payer
					// + initialiser la sequence
					if (paymentVoucherContext.getPaymentInvoiceToPayList() != null)  {
						for (PaymentInvoiceToPay pToPay : paymentVoucherContext.getPaymentInvoiceToPayList())  {
							PaymentInvoiceToPay piToPayFromContext = paymentInvoiceToPayService.find(pToPay.getId());
							PaymentInvoiceToPay piToPayOld = new PaymentInvoiceToPay();
							piToPayOld.setSequence(piToPayFromContext.getSequence());
							piToPayOld.setMoveLine(piToPayFromContext.getMoveLine());
							if(piToPayFromContext.getMoveLine() != null && piToPayFromContext.getMoveLine().getId() != null)  {
								piToPayOld.setMoveLine(piToPayFromContext.getMoveLine());
							}
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
					LOG.debug("Nombre de ligne selectionné {}", paymentInvoiceSelectedList.size());
					//Ajouter les nouvelles lignes sélectionnées
					//Incrementation de la liste avec les lignes récupérées au dessus piToPayLine
					for (PaymentInvoice paymentInvoice : paymentInvoiceSelectedList)  {
						PaymentInvoiceToPay paymentInvoiceToPay = new PaymentInvoiceToPay();
						
						MoveLine moveLine = paymentInvoice.getMoveLine();
						Move move = moveLine.getMove();
						
//						BigDecimal paidAmount = null;
						
						BigDecimal amountRemainingConverted = null;
						
						paymentInvoiceToPay.setSequence(lineSeq);
						paymentInvoiceToPay.setMoveLine(moveLine);
						paymentInvoiceToPay.setTotalAmount(paymentInvoice.getInvoiceAmount());
						paymentInvoiceToPay.setRemainingAmount(paymentInvoice.getInvoiceAmount().subtract(paymentInvoice.getPaidAmount()));
						
						paymentInvoiceToPay.setPaymentVoucher(paymentVoucher);
						
						if(move.getInvoice() != null)  {
							paymentInvoiceToPay.setCurrency(move.getInvoice().getCurrency());
						}
						else  {
							paymentInvoiceToPay.setCurrency(move.getCurrency());
						}
						 
						BigDecimal paidAmountConverted = currencyService.getAmountCurrencyConverted(
								paymentVoucher.getCurrency(),
								paymentInvoiceToPay.getCurrency(), 
								paidAmount, 
								paymentVoucher.getPaymentDateTime().toLocalDate());
						
						//On convertit dans la devise de la saisie paiement, pour comparer le restant à payer de la facture avec le restant à utilsier de la saisie paiement
						
//						if(move.getInvoice() != null)  {
//							amountRemainingConverted = currencyService.getAmountCurrencyConverted(
//									move.getInvoice().getCurrency(), 
//									paymentVoucher.getCurrency(),
//									paymentInvoice.getInvoiceAmount().subtract(paymentInvoice.getPaidAmount()), 
//									paymentVoucher.getPaymentDateTime().toLocalDate());
//							piToPay.setCurrency(move.getInvoice().getCurrency());
//						}
//						else  {
//							amountRemainingConverted = currencyService.getAmountCurrencyConverted(
//									move.getCurrency(), 
//									paymentVoucher.getCurrency(), 
//									paymentInvoice.getInvoiceAmount().subtract(paymentInvoice.getPaidAmount()), 
//									paymentVoucher.getPaymentDateTime().toLocalDate());
//							piToPay.setCurrency(move.getCurrency());
//						}

//						amountToPay = paidAmount.min(pil.getInvoiceAmount().subtract(pil.getPaidAmount()));
						BigDecimal amountToPay = paidAmountConverted.min(paymentInvoiceToPay.getRemainingAmount());
						paymentInvoiceToPay.setAmountToPay(amountToPay);
						
						piToPayLine.add(paymentInvoiceToPay);
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
		
		save(paymentVoucher);
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
	 * @throws AxelorException 
	 */
	public List<PaymentInvoice> setPaymentInvoiceList(PaymentVoucher paymentVoucher, MoveLine moveLineToPay) throws AxelorException  {
		List<PaymentInvoice> paymentInvoiceList = new ArrayList<PaymentInvoice>();
		
		for (MoveLine moveLine : this.getMoveLines(paymentVoucher,moveLineToPay))  {
			PaymentInvoice paymentInvoice = new PaymentInvoice();
			
			paymentInvoice.setMoveLine(moveLine);
			
			if(moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
				paymentInvoice.setInvoiceAmount(moveLine.getDebit());
			}
			else  {
				paymentInvoice.setInvoiceAmount(moveLine.getCredit());
			}	
			paymentInvoice.setPaidAmount(moveLine.getAmountPaid());
			paymentInvoice.setPaymentVoucher(paymentVoucher);
			Move move = moveLine.getMove();
			if(move.getInvoice() != null)  {  paymentInvoice.setCurrency(move.getInvoice().getCurrency());  }
			else  {  paymentInvoice.setCurrency(move.getCurrency());  }
			
			paymentInvoiceList.add(paymentInvoice);
		}
		
		return paymentInvoiceList;
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
		// Paiement d'une facture | EN : Paying an invoice
		else if (paymentVoucher.getInvoiceToPay() != null){
			if(paymentVoucherToolService.isDebitToPay(paymentVoucher))  {
				return this.getInvoiceDebitMoveline(paymentVoucher.getInvoiceToPay());   
			}
			else  {
				return this.getInvoiceCreditMoveline(paymentVoucher.getInvoiceToPay());   
			}
		}
		return null;
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
	
	
}
