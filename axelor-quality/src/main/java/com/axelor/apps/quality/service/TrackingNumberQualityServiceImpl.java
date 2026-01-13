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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.ProductCharacteristic;
import com.axelor.apps.quality.db.TrackingNumberCharacteristic;
import com.axelor.apps.quality.db.repo.ProductCharacteristicRepository;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

public class TrackingNumberQualityServiceImpl implements TrackingNumberQualityService {

  protected TrackingNumberRepository trackingNumberRepo;
  protected ProductCharacteristicRepository productCharacteristicRepo;

  @Inject
  public TrackingNumberQualityServiceImpl(
      TrackingNumberRepository trackingNumberRepo,
      ProductCharacteristicRepository productCharacteristicRepo) {
    this.trackingNumberRepo = trackingNumberRepo;
    this.productCharacteristicRepo = productCharacteristicRepo;
  }

  @Override
  public Integer getConformitySelect(TrackingNumber trackingNumber) {

    if (ObjectUtils.isEmpty(trackingNumber)) {
      return null;
    }

    if (ObjectUtils.isEmpty(trackingNumber.getTrackingNumberCharacteristicList())) {
      return TrackingNumberRepository.CONFORMITY_NOT_ASSESED;
    }

    if (trackingNumber.getTrackingNumberCharacteristicList().stream()
        .anyMatch(tc -> !tc.getConforms())) {
      return TrackingNumberRepository.CONFORMITY_NON_COMPLAINT;
    }

    if (trackingNumber.getTrackingNumberCharacteristicList().stream()
        .allMatch(tc -> tc.getConforms())) {
      return TrackingNumberRepository.CONFORMITY_COMPLAINT;
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<TrackingNumberCharacteristic> addCompleteControl(TrackingNumber trackingNumber) {

    if (ObjectUtils.isEmpty(trackingNumber)
        || ObjectUtils.isEmpty(trackingNumber.getProduct())
        || ObjectUtils.isEmpty(trackingNumber.getProduct().getProductCharacteristicSet())) {
      return trackingNumber.getTrackingNumberCharacteristicList();
    }

    trackingNumber = trackingNumberRepo.find(trackingNumber.getId());
    for (ProductCharacteristic productCharacteristic :
        trackingNumber.getProduct().getProductCharacteristicSet()) {
      createTrackingNumberCharacteristic(trackingNumber, productCharacteristic);
    }

    trackingNumber.setConformitySelect(getConformitySelect(trackingNumber));

    trackingNumberRepo.save(trackingNumber);
    return trackingNumber.getTrackingNumberCharacteristicList();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<TrackingNumberCharacteristic> addCharacteristicControl(
      TrackingNumber trackingNumber, List<Map<String, Object>> productCharacteristics) {

    if (ObjectUtils.isEmpty(trackingNumber) || ObjectUtils.isEmpty(productCharacteristics)) {
      return trackingNumber.getTrackingNumberCharacteristicList();
    }

    trackingNumber = trackingNumberRepo.find(trackingNumber.getId());
    for (Map<String, Object> productCharacteristicMap : productCharacteristics) {
      ProductCharacteristic productCharacteristic =
          productCharacteristicRepo.find(
              Long.parseLong(productCharacteristicMap.get("id").toString()));
      createTrackingNumberCharacteristic(trackingNumber, productCharacteristic);
    }

    trackingNumber.setConformitySelect(getConformitySelect(trackingNumber));

    trackingNumberRepo.save(trackingNumber);
    return trackingNumber.getTrackingNumberCharacteristicList();
  }

  protected TrackingNumberCharacteristic createTrackingNumberCharacteristic(
      TrackingNumber trackingNumber, ProductCharacteristic productCharacteristic) {

    if (ObjectUtils.isEmpty(productCharacteristic)) {
      return null;
    }

    TrackingNumberCharacteristic trackingNumberCharacteristic = new TrackingNumberCharacteristic();
    trackingNumberCharacteristic.setCharacteristic(productCharacteristic.getCharacteristic());
    trackingNumberCharacteristic.setTrackingNumber(trackingNumber);
    trackingNumberCharacteristic.setProduct(productCharacteristic.getProduct());
    trackingNumberCharacteristic.setConforms(Boolean.FALSE);
    trackingNumber.addTrackingNumberCharacteristicListItem(trackingNumberCharacteristic);

    return trackingNumberCharacteristic;
  }
}
