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
package com.axelor.apps.bankpayment.ebics.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.service.BankStatementService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EbicsPartnerServiceImpl implements EbicsPartnerService {
	
	protected BankStatementService bankStatementService;
	protected EbicsService ebicsService;
	protected BankStatementRepository bankStatementRepository;
	
	@Inject
	public EbicsPartnerServiceImpl(BankStatementService bankStatementService, EbicsService ebicsService, BankStatementRepository bankStatementRepository)  {
		
		this.bankStatementService = bankStatementService;
		this.ebicsService = ebicsService;
		this.bankStatementRepository = bankStatementRepository;
		
	}
	
	@Transactional
	public void getBankStatements(EbicsPartner ebicsPartner) throws AxelorException, IOException  {
		
		EbicsUser ebicsUser = ebicsPartner.getBankStatementEbicsUser();
		
		if(ebicsPartner.getBankStatementFileFormatSet() == null || ebicsPartner.getBankStatementFileFormatSet().isEmpty() || ebicsUser == null)  {  return;  }
		
		LocalDateTime executionDateTime = LocalDateTime.now();
		
		Date startDate = null;
		Date endDate = null;
		LocalDate bankStatementStartDate = null;
		LocalDate bankStatementToDate = null;
		
		if(ebicsPartner.getBankStatementGetModeSelect() == EbicsPartnerRepository.GET_MODE_PERIOD)  {
			bankStatementStartDate = ebicsPartner.getBankStatementStartDate();
			if(bankStatementStartDate != null)  {
				startDate = bankStatementStartDate.toDate();
			}
			bankStatementToDate = ebicsPartner.getBankStatementEndDate();
			if(bankStatementToDate != null)  {
				endDate = bankStatementToDate.toDate();
			}
		}
		else if(ebicsPartner.getBankStatementLastExeDateT() != null) {
			bankStatementStartDate = ebicsPartner.getBankStatementLastExeDateT().toLocalDate();
			bankStatementToDate = executionDateTime.toLocalDate();
		}
		
		for(BankStatementFileFormat bankStatementFileFormat : ebicsPartner.getBankStatementFileFormatSet())  {
			
			File file = ebicsService.sendFDLRequest(ebicsUser, null, startDate, endDate, bankStatementFileFormat.getStatementFileFormatSelect());
			
			bankStatementRepository.save(bankStatementService.createBankStatement(file, bankStatementStartDate, bankStatementToDate, bankStatementFileFormat, ebicsPartner, executionDateTime));
			
		}
		
		//TODO LIER LES RIB AU EBICS PARTNER ??
		
		ebicsPartner.setBankStatementLastExeDateT(executionDateTime);
		
		Beans.get(EbicsPartnerRepository.class).save(ebicsPartner);
		
	}
	
	

	
}
