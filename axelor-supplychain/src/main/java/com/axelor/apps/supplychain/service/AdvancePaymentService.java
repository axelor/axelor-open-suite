package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AdvancePaymentAccount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AdvancePaymentAccountRepository;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AdvancePaymentService extends AdvancePaymentRepository{
	
	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderInvoiceServiceImpl.class);
	
	
	@Inject
	private PaymentModeService paymentModeService;
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	

	public void fillAdvancePayment(Invoice invoice, SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException
	{
		if(!saleOrder.getAdvancePaymentList().isEmpty())
		{
			BigDecimal total = BigDecimal.ZERO;
			for (SaleOrderLine saleOrderLine : saleOrderLineList)
			{
				total = total.add(saleOrderLine.getInTaxTotal());
				LOG.debug("Total = {}+{}", total.subtract(saleOrderLine.getInTaxTotal()), saleOrderLine.getInTaxTotal());
			}
			for (AdvancePayment advancePayment : saleOrder.getAdvancePaymentList()) 
			{
				if(advancePayment.getAmountRemainingToUse().compareTo(BigDecimal.ZERO) != 0 && total.compareTo(BigDecimal.ZERO) != 0)
				{
					if(total.max(advancePayment.getAmountRemainingToUse()) == total)
					{
						total = total.subtract(advancePayment.getAmountRemainingToUse());
						AdvancePaymentAccount advancePaymentAccount = null;
						advancePaymentAccount = createAdvancePaymentAccount(advancePayment, invoice, false, total, saleOrder);
						List <AdvancePaymentAccount> APAlist = new ArrayList<>();
						
						if (invoice.getAdvancePaymentList() != null)
							APAlist.addAll(invoice.getAdvancePaymentList());
						
						APAlist.add(advancePaymentAccount);
						invoice.setAdvancePaymentList(APAlist);
						advancePayment.setAmountRemainingToUse(BigDecimal.ZERO);
					}
					else
					{
						advancePayment.setAmountRemainingToUse(advancePayment.getAmountRemainingToUse().subtract(total));
						AdvancePaymentAccount advancePaymentAccount = null;
						advancePaymentAccount = createAdvancePaymentAccount(advancePayment, invoice, true, total, saleOrder);
						List <AdvancePaymentAccount> APAlist = new ArrayList<>();
						if (invoice.getAdvancePaymentList() != null)
						 APAlist.addAll(invoice.getAdvancePaymentList());
						APAlist.add(advancePaymentAccount);
						invoice.setAdvancePaymentList(APAlist);
						total = BigDecimal.ZERO;
					}
				}
			}	
		}
	}
	
	@Transactional
	public AdvancePayment addMoveToAdvancePayment(SaleOrder saleOrder, AdvancePayment advancePayment) throws AxelorException
	{			
		
		if (advancePayment.getMove() != null)
		{
			advancePayment.getMove().setDate(advancePayment.getAdvancePaymentDate());
			advancePayment.getMove().setCompany(saleOrder.getCompany());
			
			for (MoveLine moveLine : advancePayment.getMove().getMoveLineList()) 
			{
				if (moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0 && moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0)
					moveLine.setDebit(currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePayment.getAmount(), advancePayment.getAdvancePaymentDate()));

				if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0 && moveLine.getCredit().compareTo(BigDecimal.ZERO) != 0)
					moveLine.setCredit(currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePayment.getAmount(), advancePayment.getAdvancePaymentDate()));
			}
			
			
				advancePayment.getMove().setPaymentMode(advancePayment.getPaymentMode());
				advancePayment.getMove().setJournal(accountConfigService.getCustomerSalesJournal(accountConfigService
						.getAccountConfig(saleOrder.getCompany())));
				
				if (advancePayment.getMove().getStatusSelect() == 1)
					moveService.validate(advancePayment.getMove());
			
				save(advancePayment);
			return advancePayment;
		}
		else	
			return this.createMoveForAdvancePayment(saleOrder, advancePayment);
	}
	
	@Transactional
	public AdvancePayment createMoveForAdvancePayment(SaleOrder saleOrder, AdvancePayment advancePayment) throws AxelorException
	{
		Move move = null;
		move = moveService.createMove(accountConfigService.getCustomerSalesJournal(accountConfigService.getAccountConfig(saleOrder.getCompany())),
			saleOrder.getCompany(), null, saleOrder.getClientPartner(),
			advancePayment.getAdvancePaymentDate(), saleOrder.getPaymentMode());
		
		MoveLine movelineDebit = null;
		MoveLine movelineCredit = null;
		Account account2 = null;
		account2 = paymentModeService.getCompanyAccount(advancePayment.getPaymentMode(), saleOrder.getCompany());
		/*
		BigDecimal rate = BigDecimal.ONE;
		
		if (!saleOrder.getCurrency().equals(advancePayment.getCurrency()))
		{
			rate = currencyService.getCurrencyConversionRate(saleOrder.getCurrency(), advancePayment.getCurrency());
			LOG.debug("Currency Conversion Rate : 1{} = {}{}",saleOrder.getCurrency().getSymbol(), rate.toString(), advancePayment.getCurrency().getSymbol());
		}
		*/
		
		movelineDebit = moveLineService.createMoveLine(move, saleOrder.getClientPartner(),
				account2,
				//accountConfigService.getIrrecoverableAccount(accountConfigService.getAccountConfig(saleOrder.getCompany())), 
				currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePayment.getAmount(), advancePayment.getAdvancePaymentDate()), 
				true, advancePayment.getAdvancePaymentDate(), null, 1, "");
		movelineCredit = moveLineService.createMoveLine(move, saleOrder.getClientPartner(), 
				accountConfigService.getAdvancePaymentAccount(accountConfigService.getAccountConfig(saleOrder.getCompany())),
				currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePayment.getAmount(), advancePayment.getAdvancePaymentDate()), 
				false, advancePayment.getAdvancePaymentDate(),
				null, 2, "");
		
		move.addMoveLineListItem(movelineCredit);
		move.addMoveLineListItem(movelineDebit);
		
		moveService.validate(move);
		
		advancePayment.setMove(move);
		save(advancePayment);
		saleOrder.addAdvancePaymentListItem(advancePayment);
		return advancePayment;
	}
	
@Transactional
	public AdvancePaymentAccount createAdvancePaymentAccount(AdvancePayment advancePayment, Invoice invoice, boolean isFinished, BigDecimal total, SaleOrder saleOrder) throws AxelorException
	{
		LOG.debug("Creation d'un advancePaymentAccount");
		AdvancePaymentAccount advancePaymentAccount = new AdvancePaymentAccount();
		
		if(isFinished) //Define whether the source AdvancePayment will be completely used or not in this transaction
			advancePaymentAccount.setAmount(total);
		else
			advancePaymentAccount.setAmount(advancePayment.getAmountRemainingToUse());
		
		advancePaymentAccount.setAdvancePaymentDate(advancePayment.getAdvancePaymentDate());
		advancePaymentAccount.setCurrency(advancePayment.getCurrency());
		advancePaymentAccount.setInvoice(invoice);
		advancePaymentAccount.setPaymentMode(advancePayment.getPaymentMode());
		advancePaymentAccount.setTypeSelect(AdvancePaymentAccountRepository.STATUS_ADVANCEPAYMENT);
		
		Move move = null;
		
		move = moveService.createMove(accountConfigService.getCustomerSalesJournal(accountConfigService
				.getAccountConfig(saleOrder.getCompany())),saleOrder.getCompany(),
				null, saleOrder.getClientPartner(), advancePayment.getAdvancePaymentDate(),
				advancePayment.getPaymentMode());
		
		MoveLine movelineDebit = null;
		MoveLine movelineCredit = null;
		Account account2 = null;
		account2 = paymentModeService.getCompanyAccount(saleOrder.getPaymentMode(), saleOrder.getCompany());
		/*
		BigDecimal rate = BigDecimal.ONE;
		if (!saleOrder.getCurrency().equals(advancePayment.getCurrency()))
		{
			rate = currencyService.getCurrencyConversionRate(saleOrder.getCurrency(), advancePayment.getCurrency());
			LOG.debug("Currency Conversion Rate : 1{} = {}{}",saleOrder.getCurrency().getSymbol(), rate.toString(), advancePayment.getCurrency().getSymbol());
		}
		*/
		movelineDebit = moveLineService.createMoveLine(move, saleOrder.getClientPartner(), 
				accountConfigService.getIrrecoverableAccount(accountConfigService.getAccountConfig(saleOrder.getCompany())), 
				currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePaymentAccount.getAmount(), advancePayment.getAdvancePaymentDate()),
				true, advancePayment.getAdvancePaymentDate(), null, 1, "");
		movelineCredit = moveLineService.createMoveLine(move, saleOrder.getClientPartner(), account2, 
				currencyService.getAmountCurrencyConverted(advancePayment.getCurrency(), saleOrder.getCurrency(), advancePaymentAccount.getAmount(), advancePayment.getAdvancePaymentDate()),
				false, advancePayment.getAdvancePaymentDate(),null, 2, "");



		move.addMoveLineListItem(movelineCredit);
		move.addMoveLineListItem(movelineDebit);
		
		moveService.validate(move);
		
		advancePaymentAccount.setMove(move);
		JPA.save(advancePaymentAccount);
		invoice.addAdvancePaymentListItem(advancePaymentAccount);
		
		LOG.debug("2 lignes d'ecriture comptable créées : debit : {}, credit : {}", movelineDebit.getDebit(), movelineCredit.getCredit());
		
		return advancePaymentAccount;
	}

	@Transactional
	public AdvancePaymentAccount addMoveToInvoiceAdvancePayment(Invoice invoice, AdvancePaymentAccount advancePaymentAccount) throws AxelorException
	{
		if (advancePaymentAccount.getMove() != null)
		{
			LOG.debug("L'AdvancePaymentAccount a deja un Move : MaJ");
			advancePaymentAccount.getMove().setDate(advancePaymentAccount.getAdvancePaymentDate());
			advancePaymentAccount.getMove().setCompany(invoice.getCompany());
			/*
			BigDecimal rate = BigDecimal.ONE;
			if (!invoice.getCurrency().equals(advancePaymentAccount.getCurrency()))
			{
				rate = currencyService.getCurrencyConversionRate(invoice.getCurrency(), advancePaymentAccount.getCurrency());
				LOG.debug("Currency Conversion Rate : 1{} = {}{}",invoice.getCurrency().getSymbol(), rate.toString(), advancePaymentAccount.getCurrency().getSymbol());
			}
			*/
			for (MoveLine moveLine : advancePaymentAccount.getMove().getMoveLineList()) 
			{
				if (moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0 && moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0)
					moveLine.setDebit(currencyService.getAmountCurrencyConverted(advancePaymentAccount.getCurrency(), invoice.getCurrency(), advancePaymentAccount.getAmount(), advancePaymentAccount.getAdvancePaymentDate()));

				if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0 && moveLine.getCredit().compareTo(BigDecimal.ZERO) != 0)
					moveLine.setCredit(currencyService.getAmountCurrencyConverted(advancePaymentAccount.getCurrency(), invoice.getCurrency(), advancePaymentAccount.getAmount(), advancePaymentAccount.getAdvancePaymentDate()));
			}
			
			
			advancePaymentAccount.getMove().setPaymentMode(advancePaymentAccount.getPaymentMode());
			advancePaymentAccount.getMove().setJournal(accountConfigService.getCustomerSalesJournal(accountConfigService
						.getAccountConfig(invoice.getCompany())));
				
				if (advancePaymentAccount.getMove().getStatusSelect() == 1)
					moveService.validate(advancePaymentAccount.getMove());
			
				JPA.save(advancePaymentAccount);
				return advancePaymentAccount;
		}
		else	
			return this.createMoveForAdvancePaymentAccount(invoice, advancePaymentAccount);
		
		
		
	}
	
	@Transactional
	public AdvancePaymentAccount createMoveForAdvancePaymentAccount(Invoice invoice, AdvancePaymentAccount advancePaymentAccount) throws AxelorException
	{

		Move move = null;
		move = moveService.createMove(accountConfigService.getCustomerSalesJournal(accountConfigService.getAccountConfig(invoice.getCompany())),
				invoice.getCompany(), null, invoice.getPartner(),
				advancePaymentAccount.getAdvancePaymentDate(), invoice.getPaymentMode());
		
		MoveLine movelineDebit = null;
		MoveLine movelineCredit = null;
		Account account2 = null;
		Account account1 = null;
		
		if (advancePaymentAccount.getTypeSelect() == AdvancePaymentAccountRepository.STATUS_ADVANCEPAYMENT)//Type_Select == "Advance Payment" 
		{
			LOG.debug("Le Type Select est à 1 (AdvancePayment)");
			account1 = accountConfigService.getAdvancePaymentAccount(accountConfigService.getAccountConfig(invoice.getCompany()));
		}
		else	//Type_Select == "Payment"
		{
			LOG.debug("Le Type Select est à 2 (Payment)");
			account1 = invoice.getPartnerAccount();
		}
		
		account2 = paymentModeService.getCompanyAccount(advancePaymentAccount.getPaymentMode(), invoice.getCompany());
		/*
		BigDecimal rate = BigDecimal.ONE;
		
		if (!invoice.getCurrency().equals(advancePaymentAccount.getCurrency()))
		{
			rate = currencyService.getCurrencyConversionRate(invoice.getCurrency(), advancePaymentAccount.getCurrency());
			LOG.debug("Currency Conversion Rate : 1{} = {}{}",invoice.getCurrency().getSymbol(), rate, advancePaymentAccount.getCurrency().getSymbol());
		}
		
		*/
		movelineDebit = moveLineService.createMoveLine(move, invoice.getPartner(), account2,
				currencyService.getAmountCurrencyConverted(advancePaymentAccount.getCurrency(), invoice.getCurrency(), advancePaymentAccount.getAmount(), advancePaymentAccount.getAdvancePaymentDate()), 
				true, advancePaymentAccount.getAdvancePaymentDate(), null, 1, "");
		
		movelineCredit = moveLineService.createMoveLine(move, invoice.getPartner(), account1,
				currencyService.getAmountCurrencyConverted(advancePaymentAccount.getCurrency(), invoice.getCurrency(), advancePaymentAccount.getAmount(), advancePaymentAccount.getAdvancePaymentDate()), 
				false, advancePaymentAccount.getAdvancePaymentDate(), null, 2, "");
		
		
		move.addMoveLineListItem(movelineCredit);
		move.addMoveLineListItem(movelineDebit);
		
		moveService.validate(move);
		
		advancePaymentAccount.setMove(move);
		
		JPA.save(advancePaymentAccount);
		invoice.addAdvancePaymentListItem(advancePaymentAccount);
		
		return advancePaymentAccount;
		
	}
	
	
}
