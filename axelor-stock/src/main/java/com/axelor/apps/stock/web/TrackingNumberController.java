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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.TrackingNumberCompanyService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Set;

@Singleton
public class TrackingNumberController {

  public void calculateVolume(ActionRequest request, ActionResponse response)
      throws AxelorException {

    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);
    Beans.get(TrackingNumberService.class).calculateDimension(trackingNumber);
    response.setValues(Mapper.toMap(trackingNumber));
  }

  public void fillOriginParents(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);
    trackingNumber = Beans.get(TrackingNumberRepository.class).find(trackingNumber.getId());

    Set<TrackingNumber> originParentsSet =
        Beans.get(TrackingNumberService.class).getOriginParents(trackingNumber);

    response.setValue("originParentTrackingNumberSet", originParentsSet);
  }

  public void setProductTrackingNumberConfiguration(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TrackingNumber trackingNumber = request.getContext().asType(TrackingNumber.class);

    response.setValue(
        "productTrackingNumberConfiguration",
        Beans.get(ProductCompanyService.class)
            .get(
                trackingNumber.getProduct(),
                "trackingNumberConfiguration",
                Beans.get(TrackingNumberCompanyService.class)
                    .getCompany(trackingNumber)
                    .orElse(null)));
  }
}
