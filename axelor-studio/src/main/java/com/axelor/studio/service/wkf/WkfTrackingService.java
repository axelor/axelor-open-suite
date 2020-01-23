/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
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
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.script.SimpleBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service handle tracking of workflow instance for particular record. Creates WkfTracking,
 * WkfTrackingLine, WkfTrackingTime and WkfTrackingTotal records.
 *
 * @author axelor
 */
public class WkfTrackingService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String ACTION_TRACK = "action-method-wkf-track";

  public static final String ACTION_OPEN_TRACK = "action-wkf-open-wkf-tracking";

  @Inject private WkfRepository wkfRepo;

  @Inject private WkfTrackingRepository wkfTrackingRepo;

  @Inject private WkfTrackingLineRepository trackingLineRepo;

  @Inject private WkfTrackingTotalRepository trackingTotalRepo;

  @Inject private WkfTrackingTimeRepository trackingTimeRepo;

  private BigDecimal durationHrs;

  private String oldStatus;

  /**
   * Root method to access the service. It creates WkfTracking record. WkfTracking is linked with
   * record of model and workflow of model.
   *
   * @param model Model having workflow.
   * @param modelId Record id of model to track.
   * @param status Current wkfStatus of record.
   * @throws ClassNotFoundException
   */
  public void track(Object object) {

    if (object != null) {

      SimpleBindings ctx = null;

      object = EntityHelper.getEntity(object);
      Model model = (Model) object;

      if (object instanceof MetaJsonRecord) {
        log.debug("Meta json record context");
        MetaJsonRecord metaJsonRecord = (MetaJsonRecord) object;
        log.debug(
            "Json id: {}, Json model: {}", metaJsonRecord.getId(), metaJsonRecord.getJsonModel());
        ctx = new JsonContext((MetaJsonRecord) object);
        ctx.put("id", metaJsonRecord.getId());
        ctx.put("jsonModel", metaJsonRecord.getJsonModel());
      } else {
        ctx = new Context(model.getId(), object.getClass());
      }

      WkfTracking wkfTracking = getWorkflowTracking(ctx, object.getClass().getName());

      if (wkfTracking == null) {
        return;
      }

      MetaJsonField wkfField = wkfTracking.getWkf().getStatusField();

      Object status = null;
      status = ctx.get(wkfField.getName());
      log.debug("Status value: {}", status);

      if (status == null) {
        return;
      }

      Option item = MetaStore.getSelectionItem(wkfField.getSelection(), status.toString());

      log.debug("Fetching option {} from selection {}", status, wkfField.getSelection());
      if (item == null) {
        return;
      }

      durationHrs = BigDecimal.ZERO;
      WkfTrackingLine trackingLine = updateTrackingLine(wkfTracking, item.getTitle());
      if (trackingLine != null) {
        updateTrackingTotal(wkfTracking, item.getTitle());
        updateTrackingTime(wkfTracking, item.getTitle());
      }
    }
  }

  /**
   * Method find or create WkfTracking for model record.
   *
   * @param model Model of record.
   * @param modelId Id of record.
   * @return WkfTracking instance created/found.s
   */
  @Transactional
  public WkfTracking getWorkflowTracking(SimpleBindings ctx, String model) {

    String jsonModel = (String) ctx.get("jsonModel");

    log.debug("Context json model: {}", jsonModel);

    if (jsonModel != null) {
      model = jsonModel;
    }

    List<Wkf> wkfs = wkfRepo.all().filter("self.model = ?1", model).fetch();

    if (wkfs.isEmpty()) {
      log.debug("Workflow not found for model: {}", model);
      return null;
    }

    Wkf wkf = null;

    if (wkfs.size() > 1) {
      for (Wkf w : wkfs) {
        if (ctx.get(w.getJsonField()) != null) {
          wkf = w;
          break;
        }
      }
      if (wkf == null) {
        return null;
      }
    } else {
      wkf = wkfs.get(0);
    }

    WkfTracking wkfTracking =
        wkfTrackingRepo
            .all()
            .filter(
                "self.wkf = ?1 and self.recordModel = ?2 and self.recordId = ?3",
                wkf,
                model,
                ctx.get("id"))
            .fetchOne();

    if (wkfTracking == null) {
      wkfTracking = new WkfTracking();
      wkfTracking.setWkf(wkf);
      wkfTracking.setRecordModel(model);
      wkfTracking.setRecordId((Long) ctx.get("id"));
      wkfTracking = wkfTrackingRepo.save(wkfTracking);
    }

    return wkfTracking;
  }

  /**
   * Method add new WkfTrackingLine in WkfTracking for given status if that status is not last
   * status added.
   *
   * @param wkfTracking WkfTracking to update with new line.
   * @param status Status to check for update.
   * @return WkfTrackingLine created or return null if status not changed.
   */
  @Transactional
  public WkfTrackingLine updateTrackingLine(WkfTracking wkfTracking, String status) {

    WkfTrackingLine trackingLine =
        trackingLineRepo.all().filter("self.wkfTracking = ?1", wkfTracking).fetchOne();

    if (trackingLine == null || !trackingLine.getStatus().equals(status)) {

      LocalDateTime now = LocalDateTime.now();

      if (trackingLine != null) {
        oldStatus = trackingLine.getStatus();
        LocalDateTime lastUpdated = trackingLine.getCreatedOn();
        long minutes = Duration.between(lastUpdated, now).toMinutes();
        log.debug("Minutes between {} and {} : {}", lastUpdated, now, minutes);
        durationHrs = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        log.debug("Hours between {} and {} : {}", lastUpdated, now, durationHrs);
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
   * Update o2m to WkfTrackingTotal in WkfTracking. Create or Update WkfTrackingTotal for given
   * status with updated count of status.
   *
   * @param wkfTracking WkfTracking to update for WkfTrackingTotal.
   * @param status Status to check for total update.
   */
  @Transactional
  public void updateTrackingTotal(WkfTracking wkfTracking, String status) {

    WkfTrackingTotal trackingTotal =
        trackingTotalRepo
            .all()
            .filter("self.wkfTracking = :wkfTracking and self.status = :status")
            .bind("wkfTracking", wkfTracking)
            .bind("status", status)
            .fetchOne();

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
   * This method create or update WkfTrackingTime record for given WkfTracking and status. For
   * existing WkfTrackingTime it update total time in days and hours.
   *
   * @param wkfTracking WkfTracking to update for WkfTrackingTime.
   * @param status Status to check for WkfTrackingTime.
   */
  @Transactional
  public void updateTrackingTime(WkfTracking wkfTracking, String status) {

    WkfTrackingTime trackingTime =
        trackingTimeRepo
            .all()
            .filter("self.wkfTracking = ?1 and self.status = ?2", wkfTracking, oldStatus)
            .fetchOne();

    if (trackingTime != null) {
      BigDecimal days = durationHrs.divide(new BigDecimal(24), 2, RoundingMode.HALF_UP);
      BigDecimal totalTimeDays = trackingTime.getTotalTimeDays().add(days);
      trackingTime.setTotalTimeDays(totalTimeDays);
      BigDecimal totalTimeHrs = trackingTime.getTotalTimeHours().add(durationHrs);
      trackingTime.setTotalTimeHours(totalTimeHrs);
      trackingTimeRepo.save(trackingTime);
    }

    trackingTime =
        trackingTimeRepo
            .all()
            .filter("self.wkfTracking = ?1 and self.status = ?2", wkfTracking, status)
            .fetchOne();

    if (trackingTime == null) {
      trackingTime = new WkfTrackingTime();
      trackingTime.setWkfTracking(wkfTracking);
      trackingTime.setStatus(status);
      trackingTimeRepo.save(trackingTime);
    }
  }
}
