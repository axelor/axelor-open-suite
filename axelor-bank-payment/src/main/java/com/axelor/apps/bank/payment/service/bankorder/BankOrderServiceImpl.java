/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bank.payment.service.bankorder;

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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.bank.payment.db.BankOrder;
import com.axelor.apps.bank.payment.db.BankOrderFileFormat;
import com.axelor.apps.bank.payment.db.BankOrderLine;
import com.axelor.apps.bank.payment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bank.payment.db.repo.BankOrderRepository;
import com.axelor.apps.bank.payment.ebics.service.EbicsService;
import com.axelor.apps.bank.payment.service.bankorder.file.transfer.BankOrderFile00100102Service;
import com.axelor.apps.bank.payment.service.bankorder.file.transfer.BankOrderFile00100103Service;
import com.axelor.apps.bank.payment.service.bankorder.file.transfer.BankOrderFileAFB160ICTService;
import com.axelor.apps.bank.payment.service.bankorder.file.transfer.BankOrderFileAFB320XCTService;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.tool.StringTool;
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
	protected EbicsService ebicsService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	public BankOrderServiceImpl(BankOrderRepository bankOrderRepo, InvoicePaymentRepository invoicePaymentRepo, 
			BankOrderLineService bankOrderLineService, EbicsService ebicsService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.invoicePaymentRepo = invoicePaymentRepo;
		this.bankOrderLineService = bankOrderLineService;
		this.ebicsService = ebicsService;
		
	}
	
	
	public void checkPreconditions(BankOrder bankOrder) throws AxelorException  {
		
		LocalDate brankOrderDate = bankOrder.getBankOrderDate();
		
		if (brankOrderDate != null)  {
			if(brankOrderDate.isBefore(LocalDate.now()))  {
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE), IException.INCONSISTENCY);
			}
		}else{
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_DATE_MISSING), IException.INCONSISTENCY);
		}
		
		if(bankOrder.getOrderTypeSelect() == 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_TYPE_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getPartnerTypeSelect() == 0)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING), IException.INCONSISTENCY);
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
		if(!bankOrder.getIsMultiCurrency() && bankOrder.getBankOrderCurrency() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_CURRENCY_MISSING), IException.INCONSISTENCY);
		}
		if(bankOrder.getSignatoryUser() == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_SIGNATORY_MISSING), IException.INCONSISTENCY);
		}
		
	}
	
	
	@Override
	public BigDecimal computeBankOrderTotalAmount(BankOrder bankOrder) throws AxelorException  {

		BigDecimal bankOrderTotalAmount = BigDecimal.ZERO;
		
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		if(bankOrderLines != null){
			for (BankOrderLine bankOrderLine : bankOrderLines) {
				BigDecimal  amount = bankOrderLine.getBankOrderAmount();
				if(amount != null)  {
					bankOrderTotalAmount = bankOrderTotalAmount.add(amount);
				}
					
			}
		}
		return bankOrderTotalAmount;
	}
	
	@Override
	public BigDecimal computeCompanyCurrencyTotalAmount(BankOrder bankOrder) throws AxelorException  {

		BigDecimal  companyCurrencyTotalAmount = BigDecimal.ZERO;
		
		List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
		if(bankOrderLines != null){
			for (BankOrderLine bankOrderLine : bankOrderLines) {
				BigDecimal  amount = bankOrderLine.getCompanyCurrencyAmount();
				if(amount != null)  {
					companyCurrencyTotalAmount = companyCurrencyTotalAmount.add(amount);
				}
					
			}
		}
		return companyCurrencyTotalAmount;
	}
	
	
	public void updateTotalAmounts(BankOrder bankOrder) throws AxelorException  {
		bankOrder.setArithmeticTotal(this.computeBankOrderTotalAmount(bankOrder));
		
		if(!bankOrder.getIsMultiCurrency())  {
			bankOrder.setBankOrderTotalAmount(bankOrder.getArithmeticTotal());
		}
		
		bankOrder.setCompanyCurrencyTotalAmount(this.computeCompanyCurrencyTotalAmount(bankOrder));
	}
	
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BankOrder generateSequence(BankOrder bankOrder) {
		if(bankOrder.getBankOrderSeq() == null && bankOrder.getId() != null){
			bankOrder.setBankOrderSeq("*" + StringTool.fillStringLeft(Long.toString(bankOrder.getId()), '0', 6));
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
			validateBankOrderLines(bankOrderLines, bankOrder.getOrderTypeSelect(), bankOrder.getArithmeticTotal());
		}
	}

	
	public void validateBankOrderLines(List<BankOrderLine> bankOrderLines, int orderType, BigDecimal arithmeticTotal)  throws AxelorException{
		BigDecimal  totalAmount = BigDecimal.ZERO;
		for (BankOrderLine bankOrderLine : bankOrderLines) {
			
			bankOrderLineService.checkPreconditions(bankOrderLine);
			totalAmount = totalAmount.add(bankOrderLine.getBankOrderAmount());
			
		}
		if (!totalAmount.equals(arithmeticTotal))  {
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_LINE_TOTAL_AMOUNT_INVALID), IException.INCONSISTENCY);
		}
	}

	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validatePayment(BankOrder bankOrder) {
		
		InvoicePayment invoicePayment = invoicePaymentRepo.findByBankOrder(bankOrder).fetchOne();
		if(invoicePayment != null)  {
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
		}
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelPayment(BankOrder bankOrder) {
		InvoicePayment invoicePayment = invoicePaymentRepo.findByBankOrder(bankOrder).fetchOne();
		if(invoicePayment != null)  {
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
		}
		
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirm(BankOrder bankOrder) throws AxelorException {
		
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_AWAITING_SIGNATURE);
		
		Sequence sequence = getSequence(bankOrder);
		setBankOrderSeq(bankOrder, sequence);
		
		bankOrderRepo.save(bankOrder);
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void sign(BankOrder bankOrder) {

	//TODO
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {
		
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_VALIDATED);
		bankOrder.setValidationDateTime(new LocalDateTime());
		
		this.setSequenceOnBankOrderLines(bankOrder);
		
		this.setNbOfLines(bankOrder);
		
		File fileToSend = this.generateFile(bankOrder);
		
		Beans.get(BankOrderMoveService.class).generateMoves(bankOrder);
		
		ebicsService.sendFULRequest(bankOrder.getEbicsUser(), null, fileToSend, bankOrder.getBankOrderFileFormat().getOrderFileFormatSelect());
		
		bankOrderRepo.save(bankOrder);

	}
	
	
	private void setSequenceOnBankOrderLines(BankOrder bankOrder)  {
		
		if(bankOrder.getBankOrderLineList() == null)  {  return;  }
		
		String bankOrderSeq = bankOrder.getBankOrderSeq();
		
		int counter = 1;
		
		for(BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList())  {
			
			bankOrderLine.setCounter(counter);
			bankOrderLine.setSequence(bankOrderSeq + "-" + Integer.toString(counter++));
		}
		
	}
	
	private void setNbOfLines(BankOrder bankOrder)  {
		
		if(bankOrder.getBankOrderLineList() == null)  {  return;  }
		
		bankOrder.setNbOfLines(bankOrder.getBankOrderLineList().size());
		
	}
	

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelBankOrder(BankOrder bankOrder) {
		bankOrder.setStatusSelect(BankOrderRepository.STATUS_CANCELED);
		bankOrderRepo.save(bankOrder);
		
	}
	
	
	public File generateFile(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException  {
		
		bankOrder.setFileGenerationDateTime(new LocalDateTime());
		
		BankOrderFileFormat bankOrderFileFormat = bankOrder.getBankOrderFileFormat();
		
		File file = null;
		
		switch (bankOrderFileFormat.getOrderFileFormatSelect()) {
		case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_02_SCT :
			
			file = new BankOrderFile00100102Service(bankOrder).generateFile();
			break;
			
		case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_03_SCT :
			
			file = new BankOrderFile00100103Service(bankOrder).generateFile();
			break;
			
		case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB320_XCT :
			
			file = new BankOrderFileAFB320XCTService(bankOrder).generateFile();
			break;
			
		case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB160_ICT :
			
			file = new BankOrderFileAFB160ICTService(bankOrder).generateFile();
			break;

		default:
			
			throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_FILE_UNKNOW_FORMAT), IException.INCONSISTENCY);
		}
		
		if(file == null)  {
			throw new AxelorException(I18n.get(String.format(IExceptionMessage.BANK_ORDER_ISSUE_DURING_FILE_GENERATION, bankOrder.getBankOrderSeq())), IException.INCONSISTENCY);
		}	
		
		MetaFiles metaFiles = Beans.get(MetaFiles.class);
		
		try (InputStream is = new FileInputStream(file)) {
			metaFiles.attach(is, file.getName(), bankOrder);
			bankOrder.setFileToSend(metaFiles.upload(file));
		}			

		return file;
	}
	
	
	protected Sequence getSequence(BankOrder bankOrder) throws AxelorException {
		AccountConfig accountConfig = accountConfigService.getAccountConfig(bankOrder.getSenderCompany());

		switch (bankOrder.getOrderTypeSelect()) {
			case BankOrderRepository.ORDER_TYPE_SEPA_DIRECT_DEBIT:
				return accountConfigService.getSepaDirectDebitSeq(accountConfig);

			case BankOrderRepository.ORDER_TYPE_SEPA_CREDIT_TRANSFER:
				return accountConfigService.getSepaCreditTransSeq(accountConfig);

			case BankOrderRepository.ORDER_TYPE_INTERNATIONAL_DIRECT_DEBIT:
				return accountConfigService.getIntDirectDebitSeq(accountConfig);

			case BankOrderRepository.ORDER_TYPE_INTERNATIONAL_CREDIT_TRANSFER:
				return accountConfigService.getIntCreditTransSeq(accountConfig);

			default:
				throw new AxelorException(I18n.get(IExceptionMessage.BANK_ORDER_TYPE_MISSING), IException.MISSING_FIELD);
		}
	}
	
	protected void setBankOrderSeq(BankOrder bankOrder, Sequence sequence) throws AxelorException {
		bankOrder.setBankOrderSeq((sequenceService.setRefDate(bankOrder.getBankOrderDate()).getSequenceNumber(sequence)));

		if (bankOrder.getBankOrderSeq() != null) { return; }

		throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_COMPANY_NO_SEQUENCE), bankOrder.getSenderCompany().getName()), IException.CONFIGURATION_ERROR);
	}
}



























