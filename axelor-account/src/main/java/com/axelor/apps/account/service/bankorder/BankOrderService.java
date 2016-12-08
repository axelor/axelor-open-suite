package com.axelor.apps.account.service.bankorder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BankOrderService {

	
	public BigDecimal computeBankOrderTotalAmount(BankOrder bankOrder) throws AxelorException;
	
	public BigDecimal computeCompanyCurrencyTotalAmount(BankOrder bankOrder) throws AxelorException;
	
	public void updateTotalAmounts(BankOrder bankOrder) throws AxelorException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirm(BankOrder bankOrder);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void sign(BankOrder bankOrder);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;
	
	public File generateFile(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BankOrder generateSequence(BankOrder bankOrder);
	
	public void checkLines(BankOrder bankOrder)throws AxelorException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validatePayment(BankOrder bankOrder);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelPayment(BankOrder bankOrder);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancelBankOrder(BankOrder bankOrder);
	
}
