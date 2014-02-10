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
package com.axelor.apps.base.service.administration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.apps.base.db.IndicatorGeneratorGrouping;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class IndicatorGeneratorGroupingService {

	@Inject
	private IndicatorGeneratorService indicatorGeneratorService;

	@Transactional
	public void run(IndicatorGeneratorGrouping indicatorGeneratorGrouping) throws AxelorException  {
		
		String log = "";
		
		String result = "";
		
		for(IndicatorGenerator indicatorGenerator : indicatorGeneratorGrouping.getIndicatorGeneratorSet())  {
			
			indicatorGeneratorService.run(indicatorGenerator);
			
			result = result + "\n" + indicatorGenerator.getCode() + " "+ indicatorGenerator.getName() + " : " +indicatorGenerator.getResult();
			
			if(indicatorGenerator.getLog() != null && !indicatorGenerator.getLog().isEmpty())  {
				log = log + "\n" + indicatorGenerator.getLog();
			}
		}
		
		indicatorGeneratorGrouping.setResult(result);
		
		indicatorGeneratorGrouping.setLog(log);
		
		indicatorGeneratorGrouping.save();
	}
	
	
	@Transactional
	public void export(IndicatorGeneratorGrouping indicatorGeneratorGrouping) throws AxelorException  {
		
		String log = "";
		
		if(indicatorGeneratorGrouping.getPath() == null || indicatorGeneratorGrouping.getPath().isEmpty())  {
			
			log += String.format("\nErreur : Aucun chemin d'export de paramétré ");

		}
		
		if(indicatorGeneratorGrouping.getCode() == null || indicatorGeneratorGrouping.getCode().isEmpty())  {
			
			log += String.format("\nErreur : Aucun code de paramétré ");

		}
		
		List<String[]> resultList = new ArrayList<String[]>();
		
		for(IndicatorGenerator indicatorGenerator : indicatorGeneratorGrouping.getIndicatorGeneratorSet())  {
			
			String[] result = {indicatorGenerator.getCode(), indicatorGenerator.getName(), indicatorGenerator.getResult()};

			resultList.add(result);
			
			log = log + "\n" + indicatorGenerator.getLog();
		}
		
		try {
			CsvTool.csvWriter(indicatorGeneratorGrouping.getPath(), indicatorGeneratorGrouping.getCode()+".csv", ';',null, resultList);
		} catch (IOException e) {
			log += String.format("Erreur lors de l'écriture du fichier");
		}
		
		if(!log.isEmpty() && log.length() != 0)  {
			String log2 = indicatorGeneratorGrouping.getLog();
		
			log2 += "\n ---------------------------------------------------";
			
			log2 += log;
		
			indicatorGeneratorGrouping.setLog(log2);
		}
		
		indicatorGeneratorGrouping.save();
	}
	
	
}