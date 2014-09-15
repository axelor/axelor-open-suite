/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service.batch;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.TargetConfiguration;
import com.axelor.apps.crm.db.repo.TargetConfigurationRepository;
import com.axelor.apps.crm.service.TargetService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchTarget extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchTarget.class);
	
	@Inject
	private TargetConfigurationRepository targetConfigurationRepo;

	@Inject
	public BatchTarget(TargetService targetService) {
		
		super(targetService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
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
						targetConfigurationRepo.find(targetConfiguration.getId()).getCode()), e), IException.CRM, batch.getId());  //TODO
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le rappel de l'évènement {}", targetConfigurationRepo.find(targetConfiguration.getId()).getCode());
				
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
