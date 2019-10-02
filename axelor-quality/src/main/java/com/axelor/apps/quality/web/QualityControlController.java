/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.apps.quality.db.QualityControl;
import com.axelor.apps.quality.db.QualityProcess;
import com.axelor.apps.quality.db.repo.ControlPointRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.db.repo.QualityProcessRepository;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.apps.quality.service.print.QualityControlPrintServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

@Singleton
public class QualityControlController {

  /**
   * Open control point in new tab from quality control.
   *
   * @param request
   * @param response
   */
  public void openControlPoints(ActionRequest request, ActionResponse response) {
    response.setView(
        ActionView.define(I18n.get("Control points"))
            .model("com.axelor.apps.quality.db.ControlPoint")
            .add("grid", "control-point-grid")
            .add("form", "control-point-form")
            .domain(
                "self.qualityControl.id = "
                    + request.getContext().asType(QualityControl.class).getId())
            .map());
  }

  /**
   * Copy control point model to control point of selected quality process.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  @SuppressWarnings("unchecked")
  public void preFillOperations(ActionRequest request, ActionResponse response)
      throws AxelorException {

    LinkedHashMap<String, Object> qualityProcessMap =
        (LinkedHashMap<String, Object>) request.getContext().get("qualityProcess");
    LinkedHashMap<String, Object> qualityControlMap =
        (LinkedHashMap<String, Object>) request.getContext().get("_qualityControl");

    QualityProcess qualityProcess =
        Beans.get(QualityProcessRepository.class)
            .find(((Integer) qualityProcessMap.get("id")).longValue());
    QualityControl qualityControl =
        Beans.get(QualityControlRepository.class)
            .find(((Integer) qualityControlMap.get("id")).longValue());

    Beans.get(QualityControlService.class).preFillOperations(qualityControl, qualityProcess);

    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public void preFillOperationsFromOptionals(ActionRequest request, ActionResponse response) {

    Set<Map<String, Object>> optionalControlPoints = new HashSet<Map<String, Object>>();
    List<ControlPoint> optionalControlPointList = new ArrayList<ControlPoint>();

    Collection<Map<String, Object>> optionalControlPointSet =
        (Collection<Map<String, Object>>) request.getContext().get("optionalControlPointSet");

    if (optionalControlPointSet != null) {
      optionalControlPoints.addAll(optionalControlPointSet);
    }

    for (Map<String, Object> optionalControlPointData : optionalControlPoints) {
      ControlPoint optionalControlPoint =
          Beans.get(ControlPointRepository.class)
              .find(Long.parseLong(optionalControlPointData.get("id").toString()));
      optionalControlPointList.add(optionalControlPoint);
    }

    LinkedHashMap<String, Object> qualityControlMap =
        (LinkedHashMap<String, Object>) request.getContext().get("_qualityControl");
    QualityControl qualityControl =
        Beans.get(QualityControlRepository.class)
            .find(((Integer) qualityControlMap.get("id")).longValue());

    Beans.get(QualityControlService.class)
        .preFillOperationsFromOptionals(qualityControl, optionalControlPointList);

    response.setCanClose(true);
  }

  public void printQualityControl(ActionRequest request, ActionResponse response)
      throws AxelorException {

    QualityControl qualityControl = request.getContext().asType(QualityControl.class);
    qualityControl = Beans.get(QualityControlRepository.class).find(qualityControl.getId());

    String fileLink;
    String title = Beans.get(QualityControlPrintServiceImpl.class).getFileName(qualityControl);
    fileLink =
        Beans.get(QualityControlPrintServiceImpl.class)
            .printQualityControl(qualityControl, ReportSettings.FORMAT_PDF);

    response.setView(ActionView.define(title).add("html", fileLink).map());
  }

  @SuppressWarnings("unchecked")
  public void sendEmail(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      QualityControlService qualityControlService = Beans.get(QualityControlService.class);

      if (!ObjectUtils.isEmpty(context.get("_ids"))) {
        List<Long> idList =
            Lists.transform(
                (List) context.get("_ids"),
                new Function<Object, Long>() {
                  @Nullable
                  @Override
                  public Long apply(@Nullable Object input) {
                    return Long.parseLong(input.toString());
                  }
                });

        QualityControlRepository qualityControlRepo = Beans.get(QualityControlRepository.class);

        for (Long id : idList) {
          QualityControl qualityControl = qualityControlRepo.find(id);
          if (qualityControl.getStatusSelect() == QualityControlRepository.STATUS_FINISHED) {
            qualityControlService.sendEmail(qualityControl);
          }
        }
      } else if (!ObjectUtils.isEmpty(context.get("id"))) {
        QualityControl qualityControl = context.asType(QualityControl.class);
        qualityControlService.sendEmail(qualityControl);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
