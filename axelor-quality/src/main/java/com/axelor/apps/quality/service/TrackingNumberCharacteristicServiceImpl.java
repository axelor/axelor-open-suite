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

import com.axelor.apps.quality.db.TrackingNumberCharacteristic;
import com.axelor.apps.quality.db.repo.CharacteristicTypeRepository;
import com.axelor.common.ObjectUtils;

public class TrackingNumberCharacteristicServiceImpl
    implements TrackingNumberCharacteristicService {

  @Override
  public boolean isConforms(TrackingNumberCharacteristic trackingNumberCharacteristic) {

    if (ObjectUtils.isEmpty(trackingNumberCharacteristic)
        || ObjectUtils.isEmpty(trackingNumberCharacteristic.getCharacteristic())) {
      return false;
    }

    int characteristicTypeSelect =
        trackingNumberCharacteristic.getCharacteristic().getCharacteristicTypeSelect();
    if (characteristicTypeSelect == CharacteristicTypeRepository.TYPE_COMMENT
        && ObjectUtils.notEmpty(trackingNumberCharacteristic.getTrackingNumberComment())) {
      return true;
    }

    if (characteristicTypeSelect == CharacteristicTypeRepository.TYPE_MEASURABLE) {
      return trackingNumberCharacteristic.getProduct() != null
          && trackingNumberCharacteristic.getProduct().getProductCharacteristicSet().stream()
              .anyMatch(
                  c ->
                      (c.getCharacteristic()
                                  .equals(trackingNumberCharacteristic.getCharacteristic())
                              || c.getCharacteristic().getCharacteristicTypeSelect()
                                  == CharacteristicTypeRepository.TYPE_MEASURABLE)
                          && (trackingNumberCharacteristic
                                      .getMeasuredValue()
                                      .compareTo(c.getMinValue())
                                  >= 0
                              && trackingNumberCharacteristic
                                      .getMeasuredValue()
                                      .compareTo(c.getMaxValue())
                                  <= 0));
    }

    if (characteristicTypeSelect == CharacteristicTypeRepository.TYPE_LIST) {
      return trackingNumberCharacteristic.getProduct() != null
          && trackingNumberCharacteristic.getProduct().getProductCharacteristicSet().stream()
              .anyMatch(
                  c ->
                      (c.getCharacteristic()
                                  .equals(trackingNumberCharacteristic.getCharacteristic())
                              || c.getCharacteristic().getCharacteristicTypeSelect()
                                  == CharacteristicTypeRepository.TYPE_LIST)
                          && trackingNumberCharacteristic.getObservedValue() != null
                          && trackingNumberCharacteristic
                              .getObservedValue()
                              .equals(c.getExpectedValue()));
    }

    return false;
  }
}
