package com.axelor.apps.account.service.bankorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderFileFormat;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.bankorder.file.transfer.BankOrderFile001001002Service;
import com.axelor.apps.account.service.bankorder.file.transfer.BankOrderFile001001003Service;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BankOrderServiceImpl implements BankOrderService  {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected BankOrderRepository bankOrderRepo;
	protected InvoicePaymentRepository invoicePaymentRepo;
	protected BankOrderLineService bankOrderLineService;
	
	@Inject
	public BankOrderServiceImpl(BankOrderRepository bankOrderRepo, InvoicePaymentRepository invoicePaymentRepo, 
			BankOrderLineService bankOrderLineService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.invoicePaymentRepo = invoicePaymentRepo;
		this.bankOrderLineService = bankOrderLineService;
		
	}
	
	
	public void checkPreconditions(BankOrder bankOrder) throws AxelorException  {
		
		LocalDate brankOrderDate = bankOrder.getBankOrderDate();
		Integer orderType = bankOrder.getOrderTypeSelect();
		Integer partnerType = bankOrder.getPartnerTypeSelect();
		BigDecimal amount = bankOrder.getAmount();
		
		if (brankOrderDate != null)  {
			if(brankOrderDate.isBefore(LocalDate.now()))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE), IException.INCONSISTENCY);
			}
		}else{
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE_MISSING), IException.INCONSISTENCY);
		}
		
		if(orderType == 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_TYPE_MISSING), IException.INCONSISTENCY);
		}
		else{
			if(orderType !=  BankOrderRepository.BANK_TO_BANK_TRANSFER  && partnerType == 0)  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING), IException.INCONSISTENCY);
			}
		}
		if(bankOrder.getPaymentMode() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PAYMENT_MODE_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSenderCompany() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_COMPANY_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSenderBankDetails() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_BANK_DETAILS_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getCurrency() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_CURRENCY_MISSING), IException.INCONSISTENCY);
		}
		if(amount.compareTo(BigDecimal.ZERO) <= 0)  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_AMOUNT_NEGATIVE), IException.INCONSISTENCY);
		}
		if(bankOrder.getSignatoryUser() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_SIGNATORY_MISSING), IException.INCONSISTENCY);
		}
		
	}
	
	
	@Override
	public BigDecimal computeTotalAmount(BankOrder bankOrder) throws AxelorException  {

		BigDecimal  totalAmount = BigDecimal.ZERO;
		
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		if(bankOrderLines != null){
			for (BankOrderLine bankOrderLine : bankOrderLines) {
				BigDecimal  amount = bankOrderLine.getAmount();
				if(amount != null)  {
					totalAmount = totalAmount.add(amount);
				}
					
			}
		}
		return totalAmount;
	}
	
	@Override
	@Transactional
	public BankOrder generateSequence(BankOrder bankOrder) {
		if(bankOrder.getBankOrderSeq() == null && bankOrder.getId() != null){
			bankOrder.setBankOrderSeq("* " + bankOrder.getId());
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
			validateBankOrderLines(bankOrderLines, bankOrder.getOrderTypeSelect(), bankOrder.getAmount());
		}
	}

	
	public void validateBankOrderLines(List<BankOrderLine> bankOrderLines, int orderType, BigDecimal bankOrderAmount)  throws AxelorException{
		BigDecimal  totalAmount = BigDecimal.ZERO;
		for (BankOrderLine bankOrderLine : bankOrderLines) {
			
			bankOrderLineService.checkPreconditions(bankOrderLine, orderType);
			totalAmount = totalAmount.add(bankOrderLine.getAmount());
			
		}
		if (!totalAmount.equals(bankOrderAmount)){
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID), IException.INCONSISTENCY);
		}
	}

	
	@Override
	@Transactional
	public void validatePayment(BankOrder bankOrder) {
		
		InvoicePayment invoicePayment = invoicePaymentRepo.findByBankOrder(bankOrder).fetchOne();
		if(invoicePayment != null)  {
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		}
	}

	@Override
	@Transactional
	public void cancelPayment(BankOrder bankOrder) {
		InvoicePayment invoicePayment = invoicePaymentRepo.findByBankOrder(bankOrder).fetchOne();
		if(invoicePayment != null)  {
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
		}
		
	}
	
	@Override
	@Transactional
	public void confirm(BankOrder bankOrder) {
		
		if (bankOrder.getOrderTypeSelect() == BankOrderRepository.SEPA_CREDIT_TRANSFER || bankOrder.getOrderTypeSelect() == BankOrderRepository.INTERNATIONAL_CREDIT_TRANSFER){
			bankOrder.setStatusSelect(BankOrderRepository.STATUS_AWAITING_SIGNATURE);
		}
		else{
			bankOrder.setStatusSelect(BankOrderRepository.STATUS_VALIDATED);
		}
		
		bankOrderRepo.save(bankOrder);
	}

	@Override
	@Transactional
	public void sign(BankOrder bankOrder) {

	//TODO
	}
	
	@Transactional
	public void validate(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {
		
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_VALIDATED);
		bankOrder.setValidationDateTime(new LocalDateTime());
		
		bankOrderRepo.save(bankOrder);

		this.generateFile(bankOrder);
		
		
	}

	@Override
	@Transactional
	public void cancelBankOrder(BankOrder bankOrder) {
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_CANCELED);
		bankOrderRepo.save(bankOrder);
		
	}
	
	
	public void generateFile(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException  {
		
		bankOrder.setFileGenerationDateTime(new LocalDateTime());
		
		PaymentMode paymentMode = bankOrder.getPaymentMode();
		BankOrderFileFormat bankOrderFileFormat = paymentMode.getBankOrderFileFormat();
		
		File file = null;
		
		switch (bankOrderFileFormat.getOrderFileFormatSelect()) {
		case BankOrderFileFormatRepository.FILE_FORMAT_pain_001_001_02 :
			
			file = new BankOrderFile001001002Service(bankOrder).generateFile();
			break;
			
		case BankOrderFileFormatRepository.FILE_FORMAT_pain_001_001_03 :
			
			file = new BankOrderFile001001003Service(bankOrder).generateFile();
			break;

		default:
			break;
		}
		
		try (InputStream is = new FileInputStream(file)) {
			Beans.get(MetaFiles.class).attach(is, file.getName(), bankOrder);
		}
		
		
	}
	
	
	
	
}



























