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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.TrackingNumberConfigurationProfile;
import com.axelor.apps.stock.db.repo.TrackingNumberConfigurationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppStock;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TrackingNumberServiceImpl implements TrackingNumberService {

  protected SequenceService sequenceService;
  protected TrackingNumberRepository trackingNumberRepo;
  protected AppStockService appStockService;

  protected ProductCompanyService productCompanyService;
  protected TrackingNumberConfigurationProfileService trackingNumberConfigurationProfileService;
  protected static final int MAX_ITERATION = 1000;

  @Inject
  public TrackingNumberServiceImpl(
      SequenceService sequenceService,
      TrackingNumberRepository trackingNumberRepo,
      AppStockService appStockService,
      ProductCompanyService productCompanyService,
      TrackingNumberConfigurationProfileService trackingNumberConfigurationProfileService) {
    this.sequenceService = sequenceService;
    this.trackingNumberRepo = trackingNumberRepo;
    this.appStockService = appStockService;
    this.productCompanyService = productCompanyService;
    this.trackingNumberConfigurationProfileService = trackingNumberConfigurationProfileService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public TrackingNumber getTrackingNumber(
      Product product, Company company, LocalDate date, String origin, Partner supplier)
      throws AxelorException {

    return trackingNumberRepo.save(
        this.createTrackingNumber(product, company, date, origin, supplier));
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
  public TrackingNumber createTrackingNumber(
      Product product, Company company, LocalDate date, String origin, Partner supplier)
      throws AxelorException {
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

    trackingNumber.setProduct(product);

    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(product, "trackingNumberConfiguration", company);
    boolean isPerishable = trackingNumberConfiguration.getIsPerishable();
    trackingNumber.setIsPerishable(isPerishable);
    if (isPerishable) {
      trackingNumber.setPerishableExpirationDate(
          date.plusDays(trackingNumberConfiguration.getPerishableNbrOfDays()));
    }
    boolean hasWarranty = trackingNumberConfiguration.getHasWarranty();
    trackingNumber.setHasWarranty(hasWarranty);
    if (hasWarranty) {
      trackingNumber.setWarrantyExpirationDate(
          date.plusMonths(trackingNumberConfiguration.getWarrantyNbrOfMonths()));
    }
    trackingNumber.setCheckExpirationDateAtStockMoveRealization(
        trackingNumberConfiguration.getCheckExpirationDateAtStockMoveRealization());

    Sequence sequence = trackingNumberConfiguration.getSequence();
    if (sequence == null) {
      throw new AxelorException(
          product,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.TRACKING_NUMBER_1),
          company.getName(),
          product.getCode());
    }

    String seq;
    while (true) {
      seq =
          sequenceService.getSequenceNumber(
              sequence, TrackingNumber.class, "trackingNumberSeq", trackingNumber);
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
    trackingNumber.setSupplier(supplier);

    if (product.getTrackingNumberConfiguration() != null
        && product.getTrackingNumberConfiguration().getIsDimensional()) {
      trackingNumber.setUnitMass(product.getNetMass());
      trackingNumber.setMetricMass(product.getMetricMass());
    }

    return trackingNumber;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public TrackingNumber generateTrackingNumber(
      Product product,
      Company company,
      LocalDate date,
      String origin,
      Partner supplier,
      String notes)
      throws AxelorException {

    TrackingNumber trackingNumber =
        this.createTrackingNumber(product, company, date, origin, supplier);
    trackingNumber.setOrigin(origin);
    trackingNumber.setNote(notes);
    trackingNumberRepo.save(trackingNumber);
    return trackingNumber;
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
