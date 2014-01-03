/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.service.batch;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.TargetConfiguration;
import com.axelor.apps.crm.service.TargetService;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Injector;

public class BatchTarget extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchTarget.class);

	@Inject
	private Injector injector;
	
	@Inject
	public BatchTarget(TargetService targetService) {
		
		super(targetService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
		
		super.start();
		
	}

	
	@Override
	protected void process() {
	
		int i = 0;
		
		List<TargetConfiguration> targetConfigurationList = new ArrayList<TargetConfiguration>();
		if(batch.getCrmBatch().getTargetConfigurationSet() != null && !batch.getCrmBatch().getTargetConfigurationSet().isEmpty())  {
			targetConfigurationList.addAll(batch.getCrmBatch().getTargetConfigurationSet());
		}
		
		for(TargetConfiguration targetConfiguration : targetConfigurationList)  {
			
			try {
			
				targetService.createsTargets(targetConfiguration);
//					updateEventReminder(eventReminder);
				i++;
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Event reminder %s", 
						TargetConfiguration.find(targetConfiguration.getId()).getCode()), e), IException.CRM, batch.getId());  //TODO
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le rappel de l'évènement {}", TargetConfiguration.find(targetConfiguration.getId()).getCode());
				
			} finally {
				
				if (i % 1 == 0) { JPA.clear(); }
	
			}	
		}
	}
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = "Compte rendu de la génération des objectifs :\n";
		comment += String.format("\t* %s Configuration des objectifs(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		super.stop();
		addComment(comment);
		
	}

}
