package com.axelor.apps.account.service.bankorder;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BankOrderService {

	
	@Transactional
	public BigDecimal computeTotalAmount(BankOrder bankOrder)throws AxelorException;
	
	@Transactional
	public void confirm(BankOrder bankOrder);
	
	@Transactional
	public void sign(BankOrder bankOrder);
	
	public void validate(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;
	
	@Transactional
	public void generateFile(BankOrder bankOrder) throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;
	
	@Transactional
	public BankOrder generateSequence(BankOrder bankOrder);
	
	public void checkLines(BankOrder bankOrder)throws AxelorException;
	
	@Transactional
	public void validatePayment(BankOrder bankOrder);
	
	@Transactional
	public void cancelPayment(BankOrder bankOrder);
	
	@Transactional
	public void cancelBankOrder(BankOrder bankOrder);
	
}
