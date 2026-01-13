/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.quality.service.TrackingNumberQualityService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackingNumberController {

  public void assignConformitySelect(ActionRequest request, ActionResponse response) {

    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);
    response.setValue(
        "conformitySelect",
        Beans.get(TrackingNumberQualityService.class).getConformitySelect(trackingNumber));
  }

  public void addCompleteControl(ActionRequest request, ActionResponse response) {

    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);
    Beans.get(TrackingNumberQualityService.class).addCompleteControl(trackingNumber);
    response.setReload(true);
  }

  public void addCharacteristicControl(ActionRequest request, ActionResponse response) {

    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> characteristics =
        new ArrayList<Map<String, Object>>(
                (Collection<? extends Map<String, Object>>)
                    (((Map<String, Object>)
                            ((LinkedHashMap<String, Object>) request.getData().get("context"))
                                .get("product"))
                        .get("productCharacteristicSet")))
            .stream()
                .filter(map -> Boolean.TRUE.equals(map.get("selected")))
                .collect(Collectors.toList());

    Beans.get(TrackingNumberQualityService.class)
        .addCharacteristicControl(trackingNumber, characteristics);
    response.setCanClose(true);
  }
}
