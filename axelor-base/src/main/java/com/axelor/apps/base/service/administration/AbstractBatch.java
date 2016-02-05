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
package com.axelor.apps.base.service.administration;

import java.lang.reflect.Field;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public abstract class AbstractBatch {

	@Inject
	protected GeneralService generalService;

	static final Logger LOG = LoggerFactory.getLogger(AbstractBatch.class);

	protected Batch batch;
	protected Model model;

	private int done, anomaly;

	@Inject
	protected BatchRepository batchRepo;

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
		else { throw new RuntimeException(I18n.get(IExceptionMessage.ABSTRACT_BATCH_1)); }

	}

	abstract protected void process();

	protected boolean isRunnable ( Model model ) {
		this.model = model;
		if (model.getArchived() != null) {
			return !model.getArchived() ;
		}
		else { return true; }

	}

	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {

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

		batch = batchRepo.find( batch.getId() );

		batch.setEndDate( new DateTime() );
		batch.setDuration( getDuring() );

		checkPoint();
		unarchived();

		LOG.info("Fin batch {} ::: {}", new Object[]{ model, batch.getEndDate() });


	}

	protected void incrementDone(){

		batch = batchRepo.find( batch.getId() );

		done += 1;
		batch.setDone( done );
		checkPoint();

		LOG.debug("Done ::: {}", done);

	}

	protected void incrementAnomaly(){

		batch = batchRepo.find( batch.getId() );

		anomaly += 1;
		batch.setAnomaly(anomaly);
		checkPoint();

		LOG.debug("Anomaly ::: {}", anomaly);

	}

	protected void addComment(String comment){

		batch = batchRepo.find( batch.getId() );

		batch.setComments(comment);

		checkPoint();

	}

	@Transactional
	protected Batch checkPoint(){

		return batchRepo.save(batch);

	}


	@Transactional
	protected void unarchived() {

		model = JPA.find(generalService.getPersistentClass(model), model.getId());
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

		return field.getType().equals( generalService.getPersistentClass(model) );

	}

}
