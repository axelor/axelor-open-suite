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
package com.axelor.apps.account.service.cfonb;

import java.util.List;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class CfonbToolService {


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
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.CFONB_TOOL_1),
							GeneralServiceImpl.EXCEPTION, s), IException.CONFIGURATION_ERROR);
				case 1:
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.CFONB_TOOL_2),
							GeneralServiceImpl.EXCEPTION, s), IException.CONFIGURATION_ERROR);
				case 2:
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.CFONB_TOOL_3),
							GeneralServiceImpl.EXCEPTION, s), IException.CONFIGURATION_ERROR);

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
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CFONB_TOOL_4),
					GeneralServiceImpl.EXCEPTION,size,s,concerned,company.getName()), IException.CONFIGURATION_ERROR);
		}
	}


}
