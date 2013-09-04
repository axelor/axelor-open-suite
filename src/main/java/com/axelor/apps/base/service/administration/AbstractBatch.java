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

import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Batch;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

public abstract class AbstractBatch {

	static final Logger LOG = LoggerFactory.getLogger(AbstractBatch.class);
		
	protected Batch batch;
	protected Model model;
	
	private int done, anomaly;
	
	protected AbstractBatch(  ){
	
		this.batch = new Batch();

		this.batch.setStartDate(new DateTime());
		
		this.done = 0;
		this.anomaly = 0;
		
		this.batch.setDone(this.done);
		this.batch.setAnomaly(this.anomaly);

	}
	
	public Batch getBatch(){
		
		return batch;
		
	}

	public Batch run( AuditableModel model ){

		Preconditions.checkNotNull(model);
				
		if ( isRunnable( model ) ) {
			
			try {
				
				start();
				process();
				stop();
				return batch;
			
			} catch (Exception e) {
				unarchived();  throw new RuntimeException(e);
			}
		}
		else { throw new RuntimeException("This batch is not runnable !"); }
		
	}
	
	abstract protected void process();

	protected boolean isRunnable ( Model model ) {		
		this.model = model; 
		if (model.getArchived() != null) { 
			return !model.getArchived() ; 
		}
		else { return true; }
		
	}
	
	protected void start() throws IllegalArgumentException, IllegalAccessException {

		LOG.info("Début batch {} ::: {}", new Object[]{ model, batch.getStartDate() });
		
		model.setArchived(true);
		associateModel();
		checkPoint();
		
	}

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	protected void stop(){

		batch = Batch.find( batch.getId() );

		batch.setEndDate( new DateTime() );
		batch.setDuration( getDuring() );

		checkPoint();
		unarchived();
		
		LOG.info("Fin batch {} ::: {}", new Object[]{ model, batch.getEndDate() });
		
		
	}
	
	protected void incrementDone(){

		batch = Batch.find( batch.getId() );
		
		done += 1;
		batch.setDone( done );
		checkPoint();
		
		LOG.debug("Done ::: {}", done);
		
	}
	
	protected void incrementAnomaly(){

		batch = Batch.find( batch.getId() );
		
		anomaly += 1;
		batch.setAnomaly(anomaly);
		checkPoint();
		
		LOG.debug("Anomaly ::: {}", anomaly);
		
	}
	
	protected void addComment(String comment){
		
		batch = Batch.find( batch.getId() );
		
		batch.setComment(comment);
		
		checkPoint();
		
	}
	
	@Transactional
	protected Batch checkPoint(){
		
		return batch.save();
		
	}


	@Transactional
	protected void unarchived() {
		
		model = JPA.find(persistentClass(), model.getId());
		model.setArchived( false );
		
	}
	

	private int getDuring(){
		
		return new Interval(batch.getStartDate(), batch.getEndDate()).toDuration().toStandardSeconds().toStandardMinutes().getMinutes();
		
	}
	
	private void associateModel() throws IllegalArgumentException, IllegalAccessException{
		
		LOG.debug("ASSOCIATE batch:{} TO model:{}", new Object[] { batch, model });
		
		for (Field field : batch.getClass().getDeclaredFields()){
		
			LOG.debug("TRY TO ASSOCIATE field:{} TO model:{}", new Object[] { field.getType().getName(), model.getClass().getName() });
			if ( isAssociable(field) ){
				
				LOG.debug("FIELD ASSOCIATE TO MODEL");
				field.setAccessible(true);
				field.set(batch, model);
				field.setAccessible(false);
				
				break;
				
			}
			
		}
		
	}
	
	private boolean isAssociable(Field field){
		
		return field.getType().equals( persistentClass() );
		
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Model> persistentClass(){
		
		if (model instanceof HibernateProxy) {
		      return ((HibernateProxy) model).getHibernateLazyInitializer().getPersistentClass();
		}
		else { return model.getClass(); }
		
	}

}
