/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReimbursementImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReimbursementImportService.class); 
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	private RejectImportService ris;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void runReimbursementImport(Company company) throws AxelorException, IOException  {

		this.testCompanyField(company);
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		this.createReimbursementRejectMove(
				ris.getCFONBFile(accountConfig.getReimbursementImportFolderPathCFONB(), accountConfig.getTempReimbImportFolderPathCFONB(),company, 0),
				company);
		
	}
	
	
	public void createReimbursementRejectMove(List<String[]> rejectList, Company company) throws AxelorException  {
		int seq = 1;
		if(rejectList != null && !rejectList.isEmpty())  {
			LocalDate rejectDate = ris.createRejectDate(rejectList.get(0)[0]);
			Move move = this.createMoveReject(company, rejectDate);
			for(String[] reject : rejectList)  {
				
				this.createReimbursementRejectMoveLine(reject, company, seq, move, rejectDate);
				seq++;
			}
			if(move != null)  {
			// Création d'une ligne au débit
				MoveLine debitMoveLine = mls.createMoveLine(move , null, company.getAccountConfig().getReimbursementAccount(), this.getTotalAmount(move), true, false, rejectDate, seq, null);
				move.getMoveLineList().add(debitMoveLine);	
				this.validateMove(move);
			}
		}
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Reimbursement createReimbursementRejectMoveLine(String[] reject, Company company, int seq, Move move, LocalDate rejectDate) throws AxelorException  {
			
		String refReject = reject[1];
	//	String amountReject = reject[2];
		InterbankCodeLine causeReject = ris.getInterbankCodeLine(reject[3], 0);
		
		Reimbursement reimbursement = Reimbursement.all().filter("UPPER(self.ref) = ?1 AND self.company = ?2", refReject, company).fetchOne();
		if(reimbursement == null)  {
			throw new AxelorException(String.format("Aucun remboursement trouvé pour la ref %s et la société %s",
					refReject, company.getName()), IException.INCONSISTENCY);
		}
		
		Partner partner = reimbursement.getPartner();
		BigDecimal amount = reimbursement.getAmountReimbursed();
		
		// Création de la ligne au crédit
		MoveLine creditMoveLine = mls.createMoveLine(move , partner, company.getAccountConfig().getCustomerAccount(), amount, false, false, rejectDate, seq, refReject);
		move.getMoveLineList().add(creditMoveLine);	
		
		creditMoveLine.save(); 

		move.save();
		creditMoveLine.setInterbankCodeLine(causeReject);
		
		reimbursement.setRejectedOk(true);
		reimbursement.setRejectDate(rejectDate);
		reimbursement.setRejectMoveLine(creditMoveLine);
		reimbursement.setInterbankCodeLine(causeReject);
		reimbursement.save();
		
		return reimbursement;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move createMoveReject(Company company, LocalDate date) throws AxelorException  {
		return ms.createMove(company.getAccountConfig().getRejectJournal(), company, null, null, date, null, false).save();

	}
	
	public BigDecimal getTotalAmount(Move move)  {
		BigDecimal totalAmount = BigDecimal.ZERO;

		for(MoveLine moveLine : move.getMoveLineList())  {
			totalAmount = totalAmount.add(moveLine.getCredit());

		}
		return totalAmount;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public MoveLine createOppositeRejectMoveLine(Move move, int seq, LocalDate rejectDate) throws AxelorException  {
		// Création d'une ligne au débit
		MoveLine debitMoveLine = mls.createMoveLine(move , null, move.getCompany().getAccountConfig().getReimbursementAccount(), this.getTotalAmount(move), true, false, rejectDate, seq, null);
		move.getMoveLineList().add(debitMoveLine);	
		move.save();
		return debitMoveLine;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validateMove(Move move) throws AxelorException  {
		ms.validateMove(move);
		move.save();
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteMove(Move move) throws AxelorException  {
		move.remove();
	}
	
	
	/**
	 * Procédure permettant de tester la présence des champs et des séquences nécessaire aux rejets de remboursement.
	 *
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
		LOG.debug("Test de la société {}", company.getName());	
		
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		accountConfigService.getReimbursementAccount(accountConfig);
		accountConfigService.getRejectJournal(accountConfig);
		accountConfigService.getReimbursementImportFolderPathCFONB(accountConfig);
		accountConfigService.getTempReimbImportFolderPathCFONB(accountConfig);
		
	}
}
