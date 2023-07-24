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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.TrackingNumberConfigurationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppStock;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TrackingNumberService {

  @Inject private SequenceService sequenceService;

  @Inject private TrackingNumberRepository trackingNumberRepo;

  @Inject private AppStockService appStockService;

  @Transactional(rollbackOn = {Exception.class})
  public TrackingNumber getTrackingNumber(
      Product product, BigDecimal sizeOfLot, Company company, LocalDate date, String origin)
      throws AxelorException {

    TrackingNumber trackingNumber =
        trackingNumberRepo
            .all()
            .filter("self.product = ?1 AND self.counter < ?2", product, sizeOfLot)
            .fetchOne();

    if (trackingNumber == null) {
      trackingNumber =
          trackingNumberRepo.save(this.createTrackingNumber(product, company, date, origin));
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

  public TrackingNumber createTrackingNumber(
      Product product, Company company, LocalDate date, String origin) throws AxelorException {
    Preconditions.checkNotNull(product, I18n.get("Product cannot be null."));
    Preconditions.checkNotNull(company, I18n.get("Company cannot be null."));
    if (date == null) {
      throw new AxelorException(
          product,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.TRACK_NUMBER_DATE_MISSING),
          product.getFullName(),
          origin);
    }

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
          I18n.get(StockExceptionMessage.TRACKING_NUMBER_1),
          company.getName(),
          product.getCode());
    }

    Sequence sequence = trackingNumberConfiguration.getSequence();
    String seq;
    while (true) {
      seq = sequenceService.getSequenceNumber(sequence, TrackingNumber.class, "trackingNumberSeq");
      if (trackingNumberRepo
              .all()
              .filter("self.product = ?1 AND self.trackingNumberSeq = ?2", product, seq)
              .count()
          == 0) {
        break;
      }
    }
    trackingNumber.setTrackingNumberSeq(seq);

    // In case of barcode generation, retrieve the one set on tracking number configuration
    AppStock appStock = appStockService.getAppStock();
    if (appStock != null && appStock.getActivateTrackingNumberBarCodeGeneration()) {
      if (appStock.getEditTrackingNumberBarcodeType()) {
        trackingNumber.setBarcodeTypeConfig(trackingNumberConfiguration.getBarcodeTypeConfig());
      } else {
        trackingNumber.setBarcodeTypeConfig(appStock.getTrackingNumberBarcodeTypeConfig());
      }
      if (trackingNumberConfiguration.getUseTrackingNumberSeqAsSerialNbr()) {
        trackingNumber.setSerialNumber(seq);
      }
    }

    return trackingNumber;
  }

  @Transactional(rollbackOn = {Exception.class})
  public TrackingNumber generateTrackingNumber(
      Product product, Company company, LocalDate date, String origin, String notes)
      throws AxelorException {

    TrackingNumber trackingNumber = this.createTrackingNumber(product, company, date, origin);
    trackingNumber.setOrigin(origin);
    trackingNumber.setNote(notes);
    trackingNumberRepo.save(trackingNumber);
    return trackingNumber;
  }
}
