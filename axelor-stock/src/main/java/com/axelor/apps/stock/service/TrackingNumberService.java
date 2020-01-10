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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.TrackingNumberConfigurationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TrackingNumberService {

  @Inject private SequenceService sequenceService;

  @Inject private TrackingNumberRepository trackingNumberRepo;

  @Transactional(rollbackOn = {Exception.class})
  public TrackingNumber getTrackingNumber(
      Product product, BigDecimal sizeOfLot, Company company, LocalDate date)
      throws AxelorException {

    TrackingNumber trackingNumber =
        trackingNumberRepo
            .all()
            .filter("self.product = ?1 AND self.counter < ?2", product, sizeOfLot)
            .fetchOne();

    if (trackingNumber == null) {
      trackingNumber = trackingNumberRepo.save(this.createTrackingNumber(product, company, date));
    }

    trackingNumber.setCounter(trackingNumber.getCounter().add(sizeOfLot));

    return trackingNumber;
  }

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

  public TrackingNumber createTrackingNumber(Product product, Company company, LocalDate date)
      throws AxelorException {
    Preconditions.checkNotNull(product, I18n.get("Product cannot be null."));
    Preconditions.checkNotNull(company, I18n.get("Company cannot be null."));
    Preconditions.checkNotNull(date, I18n.get(IExceptionMessage.TRACK_NUMBER_DATE_MISSING));

    TrackingNumber trackingNumber = new TrackingNumber();

    if (product.getIsPerishable()) {
      trackingNumber.setPerishableExpirationDate(
          date.plusMonths(product.getPerishableNbrOfMonths()));
    }
    if (product.getHasWarranty()) {
      trackingNumber.setWarrantyExpirationDate(date.plusMonths(product.getWarrantyNbrOfMonths()));
    }

    trackingNumber.setProduct(product);
    trackingNumber.setCounter(BigDecimal.ZERO);

    TrackingNumberConfiguration trackingNumberConfiguration =
        product.getTrackingNumberConfiguration();

    if (trackingNumberConfiguration.getSequence() == null) {
      throw new AxelorException(
          product,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.TRACKING_NUMBER_1),
          company.getName(),
          product.getCode());
    }

    Sequence sequence = trackingNumberConfiguration.getSequence();
    String seq;
    while (true) {
      seq = sequenceService.getSequenceNumber(sequence);
      if (trackingNumberRepo
              .all()
              .filter("self.product = ?1 AND self.trackingNumberSeq = ?2", product, seq)
              .count()
          == 0) {
        break;
      }
    }
    trackingNumber.setTrackingNumberSeq(seq);

    return trackingNumber;
  }
}
