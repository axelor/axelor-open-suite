/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;

public class EquipmentLineServiceImpl implements EquipmentLineService {

  protected final TrackingNumberService trackingNumberService;
  protected final AppBaseService appBaseService;

  @Inject
  public EquipmentLineServiceImpl(
      TrackingNumberService trackingNumberService, AppBaseService appBaseService) {
    this.trackingNumberService = trackingNumberService;
    this.appBaseService = appBaseService;
  }

  @Override
  public TrackingNumber createTrackingNumber(EquipmentLine equipmentLine) throws AxelorException {
    Company company = AuthUtils.getUser().getActiveCompany();
    return trackingNumberService.createTrackingNumber(
        equipmentLine.getProduct(), company, appBaseService.getTodayDate(company), null);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void changeEquipment(List<EquipmentLine> equipmentLineList, Equipment equipment) {
    if (CollectionUtils.isEmpty(equipmentLineList) || equipment == null) {
      return;
    }

    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaUpdate<EquipmentLine> cr = cb.createCriteriaUpdate(EquipmentLine.class);
    Root<EquipmentLine> root = cr.from(EquipmentLine.class);
    cr.set("equipment", equipment);

    Predicate inList =
        root.get("id")
            .in(equipmentLineList.stream().map(EquipmentLine::getId).collect(Collectors.toList()));

    cr.where(inList);
    JPA.em().createQuery(cr).executeUpdate();
  }
}
