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
package com.axelor.apps.account.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.service.cfonb.CfonbImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class RejectImportService{

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected GeneralService generalService;
	protected CfonbImportService cfonbImportService;
	protected InterbankCodeLineRepository interbankCodeLineRepo;
	
	protected DateTime todayTime;

	@Inject
	public RejectImportService(GeneralService generalService, CfonbImportService cfonbImportService, InterbankCodeLineRepository interbankCodeLineRepo) {
		
		this.generalService = generalService;
		this.cfonbImportService = cfonbImportService;
		this.interbankCodeLineRepo = interbankCodeLineRepo;
		this.todayTime = this.generalService.getTodayDateTime();

	}

	
	public String getDestFilename(String src, String dest)  {
		// chemin du fichier de destination :
		log.debug("Chemin de destination : {}", dest);
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

		log.debug("Chemin de destination généré : {}", newDest);

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
			return interbankCodeLineRepo.all().filter("self.code = ?1 AND self.interbankCode = ?2 AND self.transferCfonbOk = 'true'", reasonCode, generalService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 1:
			return interbankCodeLineRepo.all().filter("self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitAndTipCfonbOk = 'true'", reasonCode, generalService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 2:
			return interbankCodeLineRepo.all().filter("self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitSepaOk = 'true'", reasonCode, generalService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 3:
			return interbankCodeLineRepo.all().filter("self.code = ?1 AND self.interbankCode = ?2 AND self.lcrBorOk = 'true'", reasonCode, generalService.getGeneral().getTransferAndDirectDebitInterbankCode()).fetchOne();
		case 4:
			return interbankCodeLineRepo.all().filter("self.code = ?1 AND self.interbankCode = ?2 AND self.chequeOk = 'true'", reasonCode, generalService.getGeneral().getChequeInterbankCode()).fetchOne();
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

