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
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class RejectImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RejectImportService.class); 

	private DateTime todayTime;

	@Inject
	private CfonbImportService cfonbImportService;
		
	@Inject
	public RejectImportService() {
		
		this.todayTime = GeneralService.getTodayDateTime();
		
	}
	
	public String getDestFilename(String src, String dest)  {
		// chemin du fichier de destination :
		LOG.debug("Chemin de destination : {}", dest);
		String newDest = ((dest).split("\\."))[0];
		String timeString = this.todayTime.toString();
		timeString = timeString.replace("-", "");
		timeString = timeString.replace(":", "");
		timeString = timeString.replace(".", "");
		timeString = timeString.replace("+", "");
		
		newDest += "_"+timeString+".";
		
		if(dest.split("\\.").length == 2)  {
			newDest += dest.split("\\.")[1];
		}
		else  {
			newDest += src.split("\\.")[1];
		}
		
		LOG.debug("Chemin de destination généré : {}", newDest);
		
		return newDest;
	}
	
	
	public String getDestCFONBFile(String src, String temp) throws AxelorException, IOException  {
		String dest = this.getDestFilename(src, temp);
		
		// copie du fichier d'import dans un repetoire temporaire
		FileTool.copy(src, dest);
		
		return dest; 
	}

	
	/**
	 * 
	 * @param src
	 * @param temp
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public List<String[]> getCFONBFile(String src, String temp, Company company, int operation) throws AxelorException, IOException  {
		
		String dest = this.getDestCFONBFile(src, temp);
		
		return cfonbImportService.importCFONB(dest, company, operation);
	}
	
	
	
	/**
	 * 
	 * @param src
	 * @param temp
	 * @param company
	 * @param operation
	 * 	 	Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement</li>
     *      <li>2 = TIP impayé</li>
     *      <li>3 = TIP</li>
     *      <li>4 = TIP + chèque</li>
     *  	</ul>
	 * @return
	 * @throws AxelorException
	 * @throws IOException
	 */
	public Map<List<String[]>,String> getCFONBFileByLot(String src, String temp, Company company, int operation) throws AxelorException, IOException  {
		
		String dest = this.getDestCFONBFile(src, temp);
		
		return cfonbImportService.importCFONBByLot(dest, company, operation);
	}
	
	
	
	/**
	 * Fonction permettant de récupérer le motif de rejet/retour
	 * @param reasonCode
	 * 			Un code motifs de rejet/retour
	 * @param interbankCodeOperation
	 * 		Le type d'opération :
	 * 		<ul>
     *      <li>0 = Virement</li>
     *      <li>1 = Prélèvement/TIP/Télérèglement</li>
     *      <li>2 = Prélèvement SEPA</li>
     *      <li>3 = LCR/BOR</li>
     *      <li>4 = Cheque</li>
     *  	</ul>
	 * @return
	 * 			Un motif de rejet/retour
	 */
	public InterbankCodeLine getInterbankCodeLine(String reasonCode, int interbankCodeOperation)  {
		switch(interbankCodeOperation)  {
		case 0:
			return InterbankCodeLine.filter("self.code = ?1 AND self.interbankCode = ?2 AND self.transferCfonbOk = 'true'", reasonCode, GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 1:
			return InterbankCodeLine.filter("self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitAndTipCfonbOk = 'true'", reasonCode, GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 2:
			return InterbankCodeLine.filter("self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitSepaOk = 'true'", reasonCode, GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 3:
			return InterbankCodeLine.filter("self.code = ?1 AND self.interbankCode = ?2 AND self.lcrBorOk = 'true'", reasonCode, GeneralService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 4:
			return InterbankCodeLine.filter("self.code = ?1 AND self.interbankCode = ?2 AND self.chequeOk = 'true'", reasonCode, GeneralService.getGeneral().getChequeInterbankCode()).fetchOne();
		default:
			return null;
		}
	}
	
	
	/**
	 * Méthode permettant de construire une date de rejet depuis le texte récupéré du fichier CFONB
	 * @param dateReject
	 * @return
	 */
	public LocalDate createRejectDate(String dateReject)  {
		return new LocalDate(
				Integer.parseInt(dateReject.substring(4, 6))+2000, 
				Integer.parseInt(dateReject.substring(2, 4)), 
				Integer.parseInt(dateReject.substring(0, 2)));
	}
}

