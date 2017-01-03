/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.bankorder;

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
