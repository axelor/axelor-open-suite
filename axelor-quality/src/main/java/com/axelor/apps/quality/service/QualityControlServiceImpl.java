/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.apps.quality.db.ControlPointModel;
import com.axelor.apps.quality.db.QualityControl;
import com.axelor.apps.quality.db.QualityCorrectiveAction;
import com.axelor.apps.quality.db.QualityMeasuringPoint;
import com.axelor.apps.quality.db.QualityProcess;
import com.axelor.apps.quality.db.repo.ControlPointRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.db.repo.QualityCorrectiveActionRepository;
import com.axelor.apps.quality.db.repo.QualityMeasuringPointRepository;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import wslite.json.JSONException;

public class QualityControlServiceImpl implements QualityControlService {

  @Inject ControlPointRepository controlPointRepo;

  /**
   * Copy control point model to control point and set it to the quality control.
   *
   * @param qualityControl Set control point model to control point of this object.
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void preFillOperations(QualityControl qualityControl, QualityProcess process)
      throws AxelorException {

    List<ControlPointModel> controlPointModelList = process.getControlPointModelList();
    List<ControlPointModel> optionalControlPointModelList =
        process.getOptionalControlPointModelList();
    List<QualityCorrectiveAction> qualityCorrectiveActionList =
        process.getQualityCorrectiveActionList();

    if (controlPointModelList != null) {
      qualityControl.getControlPointList().clear();

      for (ControlPointModel model : controlPointModelList) {
        ControlPoint point = new ControlPoint();
        this.createControlPointListItem(model, point, qualityControl);
        qualityControl.addControlPointListItem(point);
      }
    }

    if (optionalControlPointModelList != null) {
      qualityControl.getOptionalControlPointList().clear();

      for (ControlPointModel model : optionalControlPointModelList) {
        ControlPoint point = new ControlPoint();
        this.createControlPointListItem(model, point, qualityControl);
        qualityControl.addOptionalControlPointListItem(point);
      }
    }

    if (qualityCorrectiveActionList != null) {
      qualityControl.getQualityCorrectiveActionList().clear();

      for (QualityCorrectiveAction qualityCorrectiveAction : qualityCorrectiveActionList) {
        qualityCorrectiveAction =
            Beans.get(QualityCorrectiveActionRepository.class).copy(qualityCorrectiveAction, true);
        qualityControl.addQualityCorrectiveActionListItem(qualityCorrectiveAction);
      }
    }
  }

  @Transactional
  public void createControlPointListItem(
      ControlPointModel model, ControlPoint point, QualityControl qualityControl) {

    point.setStatusSelect(ControlPointRepository.STATUS_ON_HOLD);
    point.setName(model.getName());
    point.setSequence(model.getSequence());
    point.setNotes(model.getNotes());
    point.setControlFrequency(model.getControlFrequency());
    point.setControlPointType(model.getControlPointType());

    for (QualityMeasuringPoint measuringPoint : model.getMeasuringPointList()) {
      measuringPoint = Beans.get(QualityMeasuringPointRepository.class).copy(measuringPoint, true);
      point.addMeasuringPointListItem(measuringPoint);
    }

    point.setControlPointDate(qualityControl.getStartDate());

    controlPointRepo.save(point);
  }

  @Override
  @Transactional
  public void preFillOperationsFromOptionals(
      QualityControl qualityControl, List<ControlPoint> optionalControlPointList) {

    for (ControlPoint optionalControlPoint : optionalControlPointList) {
      optionalControlPoint = controlPointRepo.copy(optionalControlPoint, true);
      optionalControlPoint.setOptionalQualityControl(null);
      qualityControl.addControlPointListItem(optionalControlPoint);
    }

    Beans.get(QualityControlRepository.class).save(qualityControl);
  }

  @Override
  public void sendEmail(QualityControl qualityControl)
      throws ClassNotFoundException, IOException, JSONException {
    Template template =
        Beans.get(AppQualityService.class).getAppQuality().getQualityControlTemplate();

    if (template != null) {
      Beans.get(TemplateMessageService.class).generateAndSendMessage(qualityControl, template);
    }
  }
}
