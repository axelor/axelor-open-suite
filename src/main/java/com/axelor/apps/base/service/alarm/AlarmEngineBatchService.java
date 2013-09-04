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
package com.axelor.apps.base.service.alarm;

import java.lang.reflect.Field;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.persist.Transactional;

public class AlarmEngineBatchService extends AbstractBatch {

	static final Logger LOG = LoggerFactory
			.getLogger(AlarmEngineBatchService.class);
	
	protected AlarmEngineService<Model> alarmEngineService;
	
	@Inject
	public AlarmEngineBatchService (AlarmEngineService<Model> alarmEngineService) {
		
		this.alarmEngineService = alarmEngineService; 
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void process() {
				
		for (AlarmEngine alarmEngine : batch.getAlarmEngineBatch().getAlarmEngineSet()) {
			
			try {
				
				persistAlarm (
					alarmEngineService.get( 
						alarmEngine, (Class<Model>)Class.forName( alarmEngine.getMetaModel().getFullName() ) 
					)
				);
				

			} catch (Exception e) {

				TraceBackService.trace(new Exception(String.format("Moteur d'alarme %s", alarmEngine.getCode()), e), "", batch.getId());
				incrementAnomaly();

			} finally {
				
				JPA.clear();
				
			}

		}		
		
	}

	@Override
	protected void stop() {

		String comment = "Compte rendu de la relève des alarmes :\n";
		comment += String.format("\t* %s objet(s) en alarme(s)\n", batch.getDone() );
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly() );
		
		super.stop();
		addComment(comment);
		
		
	}
	
	@Transactional
	protected <T extends Model> void persistAlarm(Map<T, Alarm> alarms) throws IllegalArgumentException, IllegalAccessException{
		
		Alarm alarm = null;
		for (T t : alarms.keySet()) {
			
			alarm = alarms.get(t); associateAlarm(alarm, t);
			alarm.save(); incrementDone();
		}
		
	}
	
	private <T extends Model> void associateAlarm(Alarm alarm, T t) throws IllegalArgumentException, IllegalAccessException{
		
		LOG.debug("ASSOCIATE alarm:{} TO model:{}", new Object[] { batch, model });
		
		for (Field field : alarm.getClass().getDeclaredFields()){
		
			LOG.debug("TRY TO ASSOCIATE field:{} TO model:{}", new Object[] { field.getType().getName(), t.getClass().getName() });
			if ( isAssociable(field, t) ){
				
				LOG.debug("FIELD ASSOCIATE TO MODEL");
				field.setAccessible(true);
				field.set(alarm, t);
				field.setAccessible(false);
				
				break;
				
			}
			
		}
		
	}
	
	private <T extends Model> boolean isAssociable(Field field, T t){
		
		return field.getType().equals( persistentClass(t) );
		
	}
	
	private <T extends Model> Class<?> persistentClass(T t){
		
		if (t instanceof HibernateProxy) {
		      return ((HibernateProxy) t).getHibernateLazyInitializer().getPersistentClass();
		}
		else { return t.getClass(); }
		
	}

}
