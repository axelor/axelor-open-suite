/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public abstract class AbstractBatch {
	private static ThreadLocal<Batch> threadBatch = new ThreadLocal<>();

	public static final int FETCH_LIMIT = 10;

	@Inject
	protected AppBaseService appBaseService;

	protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Batch batch;
	protected Model model;

	private int done, anomaly;

	@Inject
	protected BatchRepository batchRepo;

	protected AbstractBatch() {
		this.batch = new Batch();

		this.batch.setStartDate(ZonedDateTime.now());

		this.done = 0;
		this.anomaly = 0;

		this.batch.setDone(this.done);
		this.batch.setAnomaly(this.anomaly);

	}

	public Batch getBatch() {
		return batch;
	}

	/**
	 * Returns the currently running batch.
	 *
	 * @return The Batch instance currently being run or <code>null</code> if no
	 *         batch is in progress.
	 */
	public static Batch getCurrentBatch() {
		return threadBatch.get();
	}

	/**
	 * Returns the ID of the currently running batch.
	 *
	 * @return The currently running batch's ID or <code>0</code> if no batch is
	 *         being run (allowing it to be directly used with
	 *         {@link TraceBackService}.
	 */
	public static long getCurrentBatchId() {
		Batch b = getCurrentBatch();
		return b == null ? 0 : b.getId();
	}

	public Batch run(AuditableModel model) {
		Preconditions.checkNotNull(model);
		if(threadBatch.get() != null) {
			throw new IllegalStateException(I18n.get(IExceptionMessage.ABSTRACT_BATCH_2));
		}

		if(isRunnable(model)) {
			try {
				threadBatch.set(batch);
				start();
				process();
				stop();
				return batch;
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				unarchived();
				threadBatch.remove();
			}
		} else {
			throw new RuntimeException(I18n.get(IExceptionMessage.ABSTRACT_BATCH_1));
		}

	}

	abstract protected void process();

	protected boolean isRunnable(Model model) {
		this.model = model;
		return model.getArchived() != Boolean.TRUE;
	}

	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {

		LOG.info("Début batch {} ::: {}", new Object[] { model, batch.getStartDate() });

		model.setArchived(true);
		associateModel();
		checkPoint();

	}

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	protected void stop() {

		batch = batchRepo.find(batch.getId());

		batch.setEndDate(ZonedDateTime.now());
		batch.setDuration(getDuring());

		checkPoint();

		LOG.info("Fin batch {} ::: {}", new Object[] { model, batch.getEndDate() });

	}

	protected void incrementDone() {
		findBatch();
		_incrementDone();
	}

	protected void _incrementDone() {
		done += 1;
		batch.setDone(done);
		checkPoint();

		LOG.debug("Done ::: {}", done);
	}

	protected void incrementAnomaly() {
		findBatch();
		_incrementAnomaly();
	}

	protected void _incrementAnomaly() {
		anomaly += 1;
		batch.setAnomaly(anomaly);
		checkPoint();

		LOG.debug("Anomaly ::: {}", anomaly);
	}

	protected void addComment(String comment) {

		batch = batchRepo.find(batch.getId());

		batch.setComments(comment);

		checkPoint();

	}

	@Transactional
	protected Batch checkPoint() {

		return batchRepo.save(batch);

	}

	@Transactional
	protected void unarchived() {

		model = JPA.find(EntityHelper.getEntityClass(model), model.getId());
		model.setArchived(false);

	}

	private Long getDuring() {

		return ChronoUnit.MINUTES.between(batch.getStartDate(), batch.getEndDate());

	}

	private void associateModel() throws IllegalArgumentException, IllegalAccessException {

		LOG.debug("ASSOCIATE batch:{} TO model:{}", new Object[] { batch, model });

		for(Field field : batch.getClass().getDeclaredFields()) {

			LOG.debug("TRY TO ASSOCIATE field:{} TO model:{}", new Object[] { field.getType().getName(), model.getClass().getName() });
			if(isAssociable(field)) {

				LOG.debug("FIELD ASSOCIATE TO MODEL");
				field.setAccessible(true);
				field.set(batch, model);
				field.setAccessible(false);

				break;

			}

		}

	}

	private boolean isAssociable(Field field) {

		return field.getType().equals(EntityHelper.getEntityClass(model));

	}

	protected void findBatch() {
		batch = batchRepo.find(batch.getId());
	}

}
