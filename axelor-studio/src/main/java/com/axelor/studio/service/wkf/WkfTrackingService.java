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
package com.axelor.studio.service.wkf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.mapper.Mapper;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfTracking;
import com.axelor.studio.db.WkfTrackingLine;
import com.axelor.studio.db.WkfTrackingTime;
import com.axelor.studio.db.WkfTrackingTotal;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.db.repo.WkfTrackingLineRepository;
import com.axelor.studio.db.repo.WkfTrackingRepository;
import com.axelor.studio.db.repo.WkfTrackingTimeRepository;
import com.axelor.studio.db.repo.WkfTrackingTotalRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service handle tracking of workflow instance for particular record. Creates
 * WkfTracking, WkfTrackingLine, WkfTrackingTime and WkfTrackingTotal records.
 * 
 * @author axelor
 *
 */
public class WkfTrackingService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected static final String ACTION_TRACK = "action-method-wkf-track";

	protected static final String ACTION_OPEN_TRACK = "action-wkf-open-wkf-tracking";

	@Inject
	private WkfRepository wkfRepo;

	@Inject
	private WkfTrackingRepository wkfTrackingRepo;

	@Inject
	private WkfTrackingLineRepository trackingLineRepo;

	@Inject
	private WkfTrackingTotalRepository trackingTotalRepo;

	@Inject
	private WkfTrackingTimeRepository trackingTimeRepo;

	@Inject
	private WkfService wkfService;

	private BigDecimal durationHrs;

	private String oldStatus;

	/**
	 * Root method to access the service. It create WkfTracking record.
	 * WkfTracking is linked with record of model and workflow of model.
	 * 
	 * @param model
	 *            Model having workflow.
	 * @param modelId
	 *            Record id of model to track.
	 * @param status
	 *            Current wkfStatus of record.
	 */
	public void track(String model, Object object) {
			
		log.debug("Model: {}, Object: {}", model, object);
		if (model != null && object != null) {

			Map<String, Object> obj = Mapper.toMap(object);
			if (obj.get("id") == null) {
				return;
			}
			
			WkfTracking wkfTracking = getWorkflowTracking(model,
					Integer.parseInt(obj.get("id").toString()));

			if (wkfTracking == null) {
				return;
			}
			
			MetaField wkfField =  wkfTracking.getWkf().getWkfField();
			MetaSelect metaSelect = wkfField.getMetaSelect();
			
			if (metaSelect == null) {
				return;
			}
			
			Object status = obj.get(wkfField.getName());
			
			if (status == null) {
				return;
			}
			
			Option item = MetaStore.getSelectionItem(metaSelect.getName(), status.toString());
			
			if (item == null) {
				return;
			}
			
			durationHrs = BigDecimal.ZERO;
			WkfTrackingLine trackingLine = updateTrackingLine(wkfTracking,
					item.getTitle());
			if (trackingLine != null) {
				updateTrackingTotal(wkfTracking, item.getTitle());
				updateTrackingTime(wkfTracking, item.getTitle());
			}
		}

	}

	/**
	 * Method find or create WkfTracking for model record.
	 * 
	 * @param model
	 *            Model of record.
	 * @param modelId
	 *            Id of record.
	 * @return WkfTracking instance created/found.s
	 */
	@Transactional
	public WkfTracking getWorkflowTracking(String model, Integer modelId) {

		Wkf wkf = wkfRepo.all().filter("self.metaModel.fullName = ?1", model)
				.fetchOne();

		if (wkf == null) {
			log.debug("Workflow not found for model: {}", model);
			return null;
		}

		WkfTracking wkfTracking = wkfTrackingRepo
				.all()
				.filter("self.wkf = ?1 and self.recordModel = ?2 and self.recordId = ?3",
						wkf, model, modelId).fetchOne();

		if (wkfTracking == null) {
			wkfTracking = new WkfTracking();
			wkfTracking.setWkf(wkf);
			wkfTracking.setRecordModel(model);
			wkfTracking.setRecordId(modelId);

			// try {
			// Mapper mapper = Mapper.of(Class.forName(model));
			// Property property = mapper.getProperty("namecolumn");
			// if(property != null){
			// String nameColumn = property.getName();
			// log.debug("Model: {} Name column: {}", model, nameColumn);
			// wkfTracking.setRecordName(nameColumn);
			// }
			// } catch (ClassNotFoundException e) {
			// e.printStackTrace();
			// }

			wkfTracking = wkfTrackingRepo.save(wkfTracking);
		}

		return wkfTracking;

	}

	/**
	 * Method add new WkfTrackingLine in WkfTracking for given status if that
	 * status is not last status added.
	 * 
	 * @param wkfTracking
	 *            WkfTracking to update with new line.
	 * @param status
	 *            Status to check for update.
	 * @return WkfTrackingLine created or return null if status not changed.
	 */
	@Transactional
	public WkfTrackingLine updateTrackingLine(WkfTracking wkfTracking,
			String status) {

		WkfTrackingLine trackingLine = trackingLineRepo.all()
				.filter("self.wkfTracking = ?1", wkfTracking).fetchOne();

		if (trackingLine == null || !trackingLine.getStatus().equals(status)) {

			LocalDateTime now = new LocalDateTime();

			if (trackingLine != null) {
				oldStatus = trackingLine.getStatus();
				LocalDateTime lastUpdated = trackingLine.getCreatedOn();
				Minutes minutes = Minutes.minutesBetween(lastUpdated, now);
				log.debug("Minutes between {} and {} : {}", lastUpdated, now,
						minutes.getMinutes());
				durationHrs = new BigDecimal(minutes.getMinutes()).divide(
						new BigDecimal(60), 2, RoundingMode.HALF_UP);
				log.debug("Hours between {} and {} : {}", lastUpdated, now,
						durationHrs);
				trackingLine.setTimeSpent(durationHrs);
				trackingLineRepo.save(trackingLine);
			}

			trackingLine = new WkfTrackingLine();
			trackingLine.setWkfTracking(wkfTracking);
			trackingLine.setStatus(status);
			trackingLine.setWkfTracking(wkfTracking);
			return trackingLineRepo.save(trackingLine);
		}

		return null;
	}

	/**
	 * Update o2m to WkfTrackingTotal in WkfTracking. Create or Update
	 * WkfTrackingTotal for given status with updated count of status.
	 * 
	 * @param wkfTracking
	 *            WkfTracking to update for WkfTrackingTotal.
	 * @param status
	 *            Status to check for total update.
	 */
	@Transactional
	public void updateTrackingTotal(WkfTracking wkfTracking, String status) {

		WkfTrackingTotal trackingTotal = trackingTotalRepo
				.all()
				.filter("self.wkfTracking = ?1 and self.status = ?2",
						wkfTracking, status).fetchOne();

		if (trackingTotal == null) {
			trackingTotal = new WkfTrackingTotal();
			trackingTotal.setWkfTracking(wkfTracking);
			trackingTotal.setTotalCount(0);
			trackingTotal.setStatus(status);
		}

		trackingTotal.setTotalCount(trackingTotal.getTotalCount() + 1);

		trackingTotalRepo.save(trackingTotal);
	}

	/**
	 * This method create or update WkfTrackingTime record for given WkfTracking
	 * and status. For existing WkfTrackingTime it update total time in days and
	 * hours.
	 * 
	 * @param wkfTracking
	 *            WkfTracking to update for WkfTrackingTime.
	 * @param status
	 *            Status to check for WkfTrackingTime.
	 */
	@Transactional
	public void updateTrackingTime(WkfTracking wkfTracking, String status) {

		WkfTrackingTime trackingTime = trackingTimeRepo
				.all()
				.filter("self.wkfTracking = ?1 and self.status = ?2",
						wkfTracking, oldStatus).fetchOne();

		if (trackingTime != null) {
			BigDecimal days = durationHrs.divide(new BigDecimal(24), 2,
					RoundingMode.HALF_UP);
			BigDecimal totalTimeDays = trackingTime.getTotalTimeDays()
					.add(days);
			trackingTime.setTotalTimeDays(totalTimeDays);
			BigDecimal totalTimeHrs = trackingTime.getTotalTimeHours().add(
					durationHrs);
			trackingTime.setTotalTimeHours(totalTimeHrs);
			trackingTimeRepo.save(trackingTime);
		}

		trackingTime = trackingTimeRepo
				.all()
				.filter("self.wkfTracking = ?1 and self.status = ?2",
						wkfTracking, status).fetchOne();

		if (trackingTime == null) {
			trackingTime = new WkfTrackingTime();
			trackingTime.setWkfTracking(wkfTracking);
			trackingTime.setStatus(status);
			trackingTimeRepo.save(trackingTime);
		}
	}

	/**
	 * Add tracking action and button in ViewBuilder.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder to update.
	 */
	@Transactional
	public void addTracking(ViewBuilder viewBuilder) {

		ViewItem viewButton = wkfService.getViewButton(viewBuilder,
				"openWkfTracking");
		viewButton.setTitle("Track workflow");
		viewButton.setWkfButton(true);
		viewButton.setOnClick(ACTION_OPEN_TRACK);

		String onSave = viewBuilder.getOnSave();
		onSave = wkfService.getUpdatedActions(onSave, ACTION_TRACK, true);
		viewBuilder.setOnSave(onSave);
	}

}
