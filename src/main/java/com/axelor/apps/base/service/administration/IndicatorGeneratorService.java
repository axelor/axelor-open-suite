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
package com.axelor.apps.base.service.administration;

import java.math.BigInteger;

import javax.persistence.Query;

import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class IndicatorGeneratorService {


	@Transactional
	public String run(IndicatorGenerator indicatorGenerator) throws AxelorException  {
		
		String log = "";
		
		int requestType = indicatorGenerator.getRequestLanguage();
		
		String request = indicatorGenerator.getRequest();
		
		if(request == null || request.isEmpty())  {
			log = String.format("Erreur : Aucun requête de paramêtrée pour le générateur d'indicateur %s", indicatorGenerator.getCode());
		}
		
		String result = "";
		
		try {
			if(request != null && !request.isEmpty())  {
				if(requestType == 0)  {
					
					result = this.runSqlRequest(request);
					
					
				}
				else if(requestType == 1) {
				
					result = this.runJpqlRequest(request);
					
				}
			}
		}
		catch (Exception e) {
			
			log += String.format("Erreur : Requête incorrect pour le générateur d'indicateur %s", indicatorGenerator.getCode());
			
		}
		
		indicatorGenerator.setLog(log);
		
		indicatorGenerator.setResult(result);
		
		indicatorGenerator.save();
		
		return result;
	}
	
	
	public String runSqlRequest(String request)  {
		String result = "";
	
		Query query = JPA.em().createNativeQuery(request);

		BigInteger requestResult = (BigInteger)query.getSingleResult();
		
		result = String.format("%s", requestResult);
		
		return result;
	}
	
	public String runJpqlRequest(String request)  {
		String result = "";
	
		Query query = JPA.em().createQuery(request);

		Long requestResult = (Long)query.getSingleResult();
		
		result = String.format("%s", requestResult);
		
		return result;
	}
	
	
	

	
}