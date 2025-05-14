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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.TrackingNumberConfigurationProfile;
import com.axelor.apps.stock.db.repo.TrackingNumberConfigurationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TrackingNumberServiceImpl implements TrackingNumberService {

  protected static final int MAX_ITERATION = 1000;
  protected final UserService userService;

  protected TrackingNumberConfigurationProfileService trackingNumberConfigurationProfileService;

  @Inject
  public TrackingNumberServiceImpl(
      UserService userService,
      TrackingNumberConfigurationProfileService trackingNumberConfigurationProfileService) {
    this.userService = userService;
    this.trackingNumberConfigurationProfileService = trackingNumberConfigurationProfileService;
  }

  @Override
  public String getOrderMethod(TrackingNumberConfiguration trackingNumberConfiguration) {
    int autoTrackingNbrOrderSelect = -1;
    if (trackingNumberConfiguration.getIsSaleTrackingManaged()) {
      autoTrackingNbrOrderSelect = trackingNumberConfiguration.getSaleAutoTrackingNbrOrderSelect();
    } else if (trackingNumberConfiguration.getIsProductionTrackingManaged()) {
      autoTrackingNbrOrderSelect =
          trackingNumberConfiguration.getProductAutoTrackingNbrOrderSelect();
    }
    switch (autoTrackingNbrOrderSelect) {
      case TrackingNumberConfigurationRepository.TRACKING_NUMBER_ORDER_FIFO:
        return " ORDER BY self.trackingNumber ASC";

      case TrackingNumberConfigurationRepository.TRACKING_NUMBER_ORDER_LIFO:
        return " ORDER BY self.trackingNumber DESC";

      default:
        return "";
    }
  }

  @Override
  public void calculateDimension(TrackingNumber trackingNumber) throws AxelorException {
    Objects.requireNonNull(trackingNumber);

    Optional<TrackingNumberConfigurationProfile> optTrackingNumberConfigurationProfile =
        Optional.ofNullable(trackingNumber.getProduct())
            .map(Product::getTrackingNumberConfiguration)
            .map((TrackingNumberConfiguration::getTrackingNumberConfigurationProfile));

    if (optTrackingNumberConfigurationProfile.isPresent()) {
      trackingNumberConfigurationProfileService.calculateDimension(
          trackingNumber, optTrackingNumberConfigurationProfile.get());
    }
  }

  @Override
  public Set<TrackingNumber> getOriginParents(TrackingNumber trackingNumber)
      throws AxelorException {
    Objects.requireNonNull(trackingNumber);

    if (trackingNumber.getParentTrackingNumberSet() != null
        && !trackingNumber.getParentTrackingNumberSet().isEmpty()) {
      return getOriginParentsRecursive(trackingNumber, 0);
    }
    return Set.of();
  }

  protected Set<TrackingNumber> getOriginParentsRecursive(
      TrackingNumber trackingNumber, int loopNbr) throws AxelorException {

    if (loopNbr >= MAX_ITERATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_TRACKING_NUMBER_PARENT_MAXIMUM_ITERATION_REACHED));
    }

    if (trackingNumber.getParentTrackingNumberSet() != null
        && !trackingNumber.getParentTrackingNumberSet().isEmpty()) {
      HashSet<TrackingNumber> trackingNumbers = new HashSet<>();
      for (TrackingNumber parentTrackingNumber : trackingNumber.getParentTrackingNumberSet()) {
        trackingNumbers.addAll(this.getOriginParentsRecursive(parentTrackingNumber, loopNbr + 1));
      }
      return trackingNumbers;
    }

    return Set.of(trackingNumber);
  }
}
