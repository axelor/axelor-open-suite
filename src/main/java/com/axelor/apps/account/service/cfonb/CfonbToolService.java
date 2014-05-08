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
package com.axelor.apps.account.service.cfonb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.service.config.CfonbConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class CfonbToolService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CfonbToolService.class);
	
	
	@Inject
	private CfonbConfigService cfonbConfigService;
	
	
	
	/**
	 * Procédure permettant de vérifier que la chaine de caractère ne contient que des entier
	 * @param s
	 * 			La chaine de caractère à tester
	 * @param company
	 * 			Une société
	 * @param type
	 * 		Le type d'enregistrement :
	 * 		<ul>
     *      <li>0 = émetteur</li>
     *      <li>1 = destinataire</li>
     *      <li>2 = total</li>
     *  	</ul>
	 * @throws AxelorException
	 */
	public void testDigital(String s, int type) throws AxelorException  {
		if(!StringTool.isDigital(s))  {
			switch(type)  {
				case 0:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour l'émetteur",
							GeneralService.getExceptionAccountingMsg(), s), IException.CONFIGURATION_ERROR);
				case 1:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le destinataire",
							GeneralService.getExceptionAccountingMsg(), s), IException.CONFIGURATION_ERROR);
				case 2:
					throw new AxelorException(String.format("%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le total",
							GeneralService.getExceptionAccountingMsg(), s), IException.CONFIGURATION_ERROR);
				
				default:
					break;
			}	
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier la longueur d'un CFONB
	 * @param senderCFONB
	 * 			Un enregistrement 'emetteur'
	 * @param totalCFONB
	 * 			Un enregistrement 'total'
	 * @param multiRecipientCFONB
	 * 			Une liste d'enregistrement 'destinataire'
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testLength(String senderCFONB, String totalCFONB, List<String> multiRecipientCFONB, Company company) throws AxelorException  {
		this.testLength(senderCFONB, company, 0, 160);
		this.testLength(totalCFONB, company, 2, 160);
		for(String s : multiRecipientCFONB)  {
			this.testLength(s, company, 1, 160);
		}
	}
	
	
	
	
	/**
	 * Procédure permettant de vérifier la longueur d'un enregistrement CFONB
	 * @param s
	 * 			Un enregistrement CFONB
	 * @param company
	 * 			Une société
	 * @param type
	 * 	 	Le type d'enregistrement :
	 * 		<ul>
     *      <li>0 = émetteur</li>
     *      <li>1 = destinataire</li>
     *      <li>2 = total</li>
     *      <li>3 = entête</li>
     *      <li>4 = détail</li>
     *      <li>5 = fin</li>
     *  	</ul>
	 * 
	 * @param size
	 * 			La longueur de l'enregistrement
	 * @throws AxelorException
	 */
	public void testLength(String s, Company company, int type, int size) throws AxelorException  {
		if(s.length() != size)  {
			String concerned = "";
			switch(type)  {
				case 0:
					concerned = "émetteur";
					break;
				case 1:
					concerned = "destinataire";
					break;
				case 2:
					concerned = "total";
					break;
				case 3:
					concerned = "entête";
					break;
				case 4:
					concerned = "détail";
					break;
				case 5:
					concerned = "fin";
					break;
				default:
					break;
			}	
			throw new AxelorException(String.format("%s :\n Annomlie détectée (l'enregistrement ne fait pas %s caractères : %s) pour l'enregistrement %s, société %s",
					GeneralService.getExceptionAccountingMsg(),size,s,concerned,company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
}
