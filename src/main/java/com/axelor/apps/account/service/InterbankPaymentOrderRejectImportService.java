/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InterbankPaymentOrderRejectImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(InterbankPaymentOrderRejectImportService.class); 
	
	@Inject
	private RejectImportService rejectImportService;
	
	@Inject
	private CfonbService cfonbService;
	
	@Inject
	private PaymentModeService paymentModeService;
	
	@Inject
	private MoveService moveService;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private AccountConfigService accountConfigService;
	

	public List<String[]> getCFONBFile(Company company) throws AxelorException, IOException  {
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		String dest = rejectImportService.getDestFilename(accountConfig.getInterbankPaymentOrderRejectImportPathCFONB(), accountConfig.getTempInterbankPaymentOrderRejectImportPathCFONB());
		
		// copie du fichier d'import dans un repetoire temporaire
		FileTool.copy(accountConfig.getInterbankPaymentOrderRejectImportPathCFONB(), dest);
		
		return cfonbService.importCFONB(dest, company, 2);
	}
	
	
	/**
	 * Procédure permettant de 
	 * @param rejectList
	 * @param company
	 * @param interbankPaymentOrderRejectImport
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInterbankPaymentOrderRejectMove(String[] reject, Company company) throws AxelorException  {
			String dateReject = reject[0];
			String refReject = reject[1];
			BigDecimal amountReject = new BigDecimal(reject[2]);
			InterbankCodeLine causeReject = rejectImportService.getInterbankCodeLine(reject[3], 0);
			
			Invoice invoice = Invoice.all().filter("UPPER(self.invoiceId) = ?1 AND self.company = ?2", refReject, company).fetchOne();
			if(invoice == null)  {
				throw new AxelorException(String.format("%s \nAucune facture trouvée pour le numéro de facture %s et la société %s",
						GeneralService.getExceptionAccountingMsg(), refReject, company.getName()), IException.INCONSISTENCY);
			}
			
			Partner partner = invoice.getPartner();
			if(invoice.getPaymentMode() == null)  {
				throw new AxelorException(String.format("%s - Aucun mode de paiement configuré pour la facture %s",
						GeneralService.getExceptionAccountingMsg(), refReject), IException.INCONSISTENCY);
			}
			
			Account bankAccount = paymentModeService.getCompanyAccount(invoice.getPaymentMode(), company);
			
			AccountConfig accountConfig = company.getAccountConfig();
			
			Move move = moveService.createMove(accountConfig.getRejectJournal(), company, null, partner, null, true);
			
			// Création d'une ligne au crédit
			MoveLine debitMoveLine = moveLineService.createMoveLine(move , partner, accountConfig.getCustomerAccount(), amountReject, true, false, rejectImportService.createRejectDate(dateReject), 1, refReject);
			move.getMoveLineList().add(debitMoveLine);	
			debitMoveLine.setInterbankCodeLine(causeReject);
			
			// Création d'une ligne au crédit
			MoveLine creditMoveLine = moveLineService.createMoveLine(move , partner, bankAccount, amountReject, false, false, rejectImportService.createRejectDate(dateReject), 2, null);
			move.getMoveLineList().add(creditMoveLine);		
			moveService.validateMove(move);
			move.save();
			
			return invoice;
	}
	
	
	/**
	 * Fonction permettant de récupérer le compte comptable associé au mode de paiement
	 * @param company
	 * 			Une société
	 * @param isTIPCheque
	 * 			Récupérer le compte comptable du mode de paiement TIP chèque ou seulement TIP ?
	 * @return
	 * @throws AxelorException
	 */
	public Account getAccount(Company company, boolean isTIPCheque) throws AxelorException  {
		PaymentMode paymentMode = null;
		
		if(isTIPCheque)  {
			paymentMode = PaymentMode.all().filter("self.code = 'TIC'").fetchOne();
			if(paymentMode == null)  {
				throw new AxelorException(String.format("%s :\n Le mode de paiement dont le code est 'TIC' n'a pas été trouvé",
						GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			}
		}
		else  {
			paymentMode = PaymentMode.all().filter("self.code = 'TIP'").fetchOne();
			if(paymentMode == null)  {
				throw new AxelorException(String.format("%s :\n Le mode de paiement dont le code est 'TIP' n'a pas été trouvé",
						GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			}
		}
		return paymentModeService.getCompanyAccount(paymentMode, company);
	}
	
	
	/**
	 * Procédure permettant de tester la présence des champs et des séquences nécessaires aux rejets de paiement par TIP et TIP chèque.
	 *
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
	
		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		accountConfigService.getRejectJournal(accountConfig);
		accountConfigService.getInterbankPaymentOrderRejectImportPathCFONB(accountConfig);
		accountConfigService.getTempInterbankPaymentOrderRejectImportPathCFONB(accountConfig);
		
	}
	
	
}
