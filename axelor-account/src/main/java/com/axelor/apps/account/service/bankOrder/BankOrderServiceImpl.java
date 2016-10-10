package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BankOrderServiceImpl implements BankOrderService{
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BankOrderRepository bankOrderRepo;
	protected InvoicePaymentRepository invoicePaymentRepo;
	
	@Inject
	public BankOrderServiceImpl(BankOrderRepository bankOrderRepo, InvoicePaymentRepository invoicePaymentRepo){
		
		this.bankOrderRepo = bankOrderRepo;
		this.invoicePaymentRepo = invoicePaymentRepo;
	}
	
	@Override
	@Transactional
	public void validate(BankOrder bankOrder) throws AxelorException{
		LocalDate brankOrderDate = bankOrder.getBankOrderDate();
		Integer orderType = bankOrder.getOrderType();
		Integer partnerType = bankOrder.getPartnerTypeSelect();
		BigDecimal amount = bankOrder.getAmount();
		
		if (brankOrderDate != null){
			if(brankOrderDate.isBefore(LocalDate.now())){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE), IException.INCONSISTENCY);
			}
		}else{
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE_MISSING), IException.INCONSISTENCY);
		}
		
		if(orderType == 0){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_TYPE_MISSING), IException.INCONSISTENCY);
		}
		else{
			if(orderType !=  BankOrderRepository.BANK_TO_BANK_TRANSFER  && partnerType == 0){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING), IException.INCONSISTENCY);
			}
		}
		if(bankOrder.getPaymentMode() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PAYMENT_MODE_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSenderCompany() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_COMPANY_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSenderBankDetails() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getCurrency() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_CURRENCY_MISSING), IException.INCONSISTENCY);
		}
		if(amount.compareTo(BigDecimal.ZERO) <= 0){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
		}
		if(bankOrder.getSignatoryUser() == null){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_SIGNATORY_MISSING), IException.INCONSISTENCY);
		}
	}
	
	@Override
	@Transactional
	public BigDecimal updateAmount(BankOrder bankOrder) throws AxelorException {
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		BigDecimal  totalAmount = BigDecimal.ZERO;
		if(bankOrderLines != null){
			if (!bankOrder.getBankOrderLineList().isEmpty()){
				for (BankOrderLine bankOrderLine : bankOrderLines) {
					BigDecimal  amount = bankOrderLine.getAmount();
					if(amount != null){
						totalAmount = totalAmount.add(bankOrderLine.getAmount());
					}
					
				}
			}
		}
		return totalAmount;
	}
	
	@Override
	@Transactional
	public BankOrder generateSequence(BankOrder bankOrder) {
		if(bankOrder.getBankOrderSeq() == null && bankOrder.getId() != null){
			bankOrder.setBankOrderSeq("* "+bankOrder.getId());
			bankOrderRepo.save(bankOrder);
		}
		return bankOrder;
	}
	
	@Override
	public void checkLines(BankOrder bankOrder) throws AxelorException {
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		if(bankOrderLines.isEmpty()){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINES_MISSING), IException.INCONSISTENCY);
		}
		else{
			validateBankOrderLines(bankOrderLines, bankOrder.getOrderType(), bankOrder.getAmount());
		}
	}

	
	public void validateBankOrderLines(List<BankOrderLine> bankOrderLines, int orderType, BigDecimal bankOrderAmount)throws AxelorException{
		BigDecimal  totalAmount = BigDecimal.ZERO;
		for (BankOrderLine bankOrderLine : bankOrderLines) {
			BigDecimal amount = bankOrderLine.getAmount();
			if (orderType == BankOrderRepository.BANK_TO_BANK_TRANSFER){
				if (bankOrderLine.getReceiverCompany() == null ){
					throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_COMPANY_MISSING), IException.INCONSISTENCY);
				}
			}
			if(bankOrderLine.getPartner() == null) {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_PARTNER_MISSING), IException.INCONSISTENCY);
			}
			if (bankOrderLine.getReceiverBankDetails() == null ){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_COMPANY_MISSING), IException.INCONSISTENCY);
			}
			if(amount.compareTo(BigDecimal.ZERO) <= 0){
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
			}else{
				totalAmount = totalAmount.add(amount);
			}
		}
		if (!totalAmount.equals(bankOrderAmount)){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID), IException.INCONSISTENCY);
		}
	}

	@Override
	@Transactional
	public void validatePayment(BankOrder bankOrder) {
		
		InvoicePayment invoicePayment = invoicePaymentRepo.all().filter("self.bankOrder.id = ?1", bankOrder.getId()).fetchOne();
		if(invoicePayment != null){
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		}
	}

	@Override
	@Transactional
	public void cancelPayment(BankOrder bankOrder) {
		InvoicePayment invoicePayment = invoicePaymentRepo.all().filter("self.bankOrder.id = ?1", bankOrder.getId()).fetchOne();
		if(invoicePayment != null){
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
		}
		
	}
	
	@Override
	@Transactional
	public void send(BankOrder bankOrder) {
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_AWAITING_SIGNATURE);
		bankOrderRepo.save(bankOrder);
	}

	@Override
	@Transactional
	public void sign(BankOrder bankOrder) {
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_VALIDATED);
		bankOrderRepo.save(bankOrder);
	}

	@Override
	@Transactional
	public void cancelBankOrder(BankOrder bankOrder) {
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_CANCELED);
		bankOrderRepo.save(bankOrder);
		
	}

}
