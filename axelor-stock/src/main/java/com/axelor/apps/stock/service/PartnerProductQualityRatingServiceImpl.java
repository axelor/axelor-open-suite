/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.stock.db.PartnerProductQualityRating;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.PartnerProductQualityRatingRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.utils.BatchProcessorHelper;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PartnerProductQualityRatingServiceImpl implements PartnerProductQualityRatingService {

  public static final BigDecimal MAX_QUALITY_RATING = new BigDecimal(5);

  private final PartnerProductQualityRatingRepository partnerProductQualityRatingRepo;
  private final StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public PartnerProductQualityRatingServiceImpl(
      PartnerProductQualityRatingRepository partnerProductQualityRatingRepo,
      StockMoveLineRepository stockMoveLineRepository) {
    this.partnerProductQualityRatingRepo = partnerProductQualityRatingRepo;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void calculate(StockMove stockMove) throws AxelorException {
    Partner partner = stockMove.getPartner();

    if (partner == null || !partner.getIsSupplier()) {
      return;
    }

    final long partnerId = partner.getId();
    final PartnerRepository partnerRepo = Beans.get(PartnerRepository.class);
    final Partner[] partnerHolder = {partnerRepo.find(partnerId)};

    forEachStockMoveLine(
        stockMove,
        stockMoveLine -> {
          if (Optional.ofNullable(stockMoveLine.getConformitySelect()).orElse(0) != 0) {
            createAndUpdatePartnerProducQualityRating(stockMoveLine, partner);
          }
        },
        () -> partnerHolder[0] = partnerRepo.find(partnerId));

    updateSupplier(partnerHolder[0]);
  }

  protected void createAndUpdatePartnerProducQualityRating(
      StockMoveLine stockMoveLine, Partner partner) {
    Product product = stockMoveLine.getProduct();
    PartnerProductQualityRating partnerProductQualityRating =
        searchPartnerProductQualityRating(partner, product)
            .orElseGet(() -> createPartnerProductQualityRating(partner, product));
    updatePartnerProductQualityRating(partnerProductQualityRating, stockMoveLine);
  }

  @Override
  @Transactional
  public void undoCalculation(StockMove stockMove) {
    Partner partner = stockMove.getPartner();

    if (partner == null || !partner.getIsSupplier()) {
      return;
    }

    final long partnerId = partner.getId();
    final PartnerRepository partnerRepo = Beans.get(PartnerRepository.class);
    final Partner[] partnerHolder = {partnerRepo.find(partnerId)};

    forEachStockMoveLine(
        stockMove,
        stockMoveLine -> {
          Product product = stockMoveLine.getProduct();
          Optional<PartnerProductQualityRating> optional =
              searchPartnerProductQualityRating(partner, product);

          if (optional.isPresent()) {
            PartnerProductQualityRating partnerProductQualityRating = optional.get();
            updatePartnerProductQualityRating(partnerProductQualityRating, stockMoveLine, true);
          }
        },
        () -> partnerHolder[0] = partnerRepo.find(partnerId));

    updateSupplier(partnerHolder[0]);
  }

  /**
   * Search for partner product quality rating.
   *
   * @param partner
   * @param product
   * @return
   */
  private Optional<PartnerProductQualityRating> searchPartnerProductQualityRating(
      Partner partner, Product product) {
    List<PartnerProductQualityRating> partnerProductQualityRatingList =
        partner.getPartnerProductQualityRatingList();

    if (partnerProductQualityRatingList == null) {
      return Optional.empty();
    }

    Optional<PartnerProductQualityRating> productQualityRating =
        partnerProductQualityRatingList.stream()
            .filter(
                PartnerProductQualityRating ->
                    PartnerProductQualityRating.getProduct() != null
                        && PartnerProductQualityRating.getProduct().equals(product))
            .findFirst();

    if (productQualityRating == null) {
      productQualityRating = Optional.empty();
    }

    return productQualityRating;
  }

  /**
   * Create partner product quality rating.
   *
   * @param partner
   * @param product
   * @return
   */
  @Transactional
  protected PartnerProductQualityRating createPartnerProductQualityRating(
      Partner partner, Product product) {
    PartnerProductQualityRating partnerProductQualityRating =
        new PartnerProductQualityRating(product);
    partner.addPartnerProductQualityRatingListItem(partnerProductQualityRating);
    partnerProductQualityRatingRepo.persist(partnerProductQualityRating);

    return partnerProductQualityRating;
  }

  /**
   * Update partner product quality rating.
   *
   * @param partnerProductQualityRating
   * @param stockMoveLine
   */
  protected void updatePartnerProductQualityRating(
      PartnerProductQualityRating partnerProductQualityRating, StockMoveLine stockMoveLine) {
    updatePartnerProductQualityRating(partnerProductQualityRating, stockMoveLine, false);
  }

  /**
   * Update partner product quality rating.
   *
   * @param partnerProductQualityRating
   * @param stockMoveLine
   * @param undo
   */
  protected void updatePartnerProductQualityRating(
      PartnerProductQualityRating partnerProductQualityRating,
      StockMoveLine stockMoveLine,
      boolean undo) {

    BigDecimal qty = !undo ? stockMoveLine.getRealQty() : stockMoveLine.getRealQty().negate();
    BigDecimal compliantArrivalProductQty =
        partnerProductQualityRating.getCompliantArrivalProductQty();

    if (stockMoveLine.getConformitySelect() == StockMoveLineRepository.CONFORMITY_COMPLIANT) {
      compliantArrivalProductQty = compliantArrivalProductQty.add(qty);
      partnerProductQualityRating.setCompliantArrivalProductQty(compliantArrivalProductQty);
    }

    BigDecimal arrivalProductQty = partnerProductQualityRating.getArrivalProductQty().add(qty);
    partnerProductQualityRating.setArrivalProductQty(arrivalProductQty);

    if (arrivalProductQty.signum() > 0) {
      BigDecimal qualityRating =
          computeQualityRating(compliantArrivalProductQty, arrivalProductQty);
      partnerProductQualityRating.setQualityRating(qualityRating);
      partnerProductQualityRating.setQualityRatingSelect(computeQualityRatingSelect(qualityRating));
    } else {
      partnerProductQualityRating
          .getPartner()
          .removePartnerProductQualityRatingListItem(partnerProductQualityRating);
    }
  }

  /**
   * Update supplier's quality rating and arrival product quantity.
   *
   * @param partner
   */
  protected void updateSupplier(Partner partner) {
    BigDecimal supplierQualityRating = BigDecimal.ZERO;
    BigDecimal supplierArrivalProductQty = BigDecimal.ZERO;
    List<PartnerProductQualityRating> partnerProductQualityRatingList =
        partner.getPartnerProductQualityRatingList();

    if (partnerProductQualityRatingList != null) {
      for (PartnerProductQualityRating partnerProductQualityRating :
          partnerProductQualityRatingList) {
        BigDecimal qualityRating = partnerProductQualityRating.getQualityRating();
        BigDecimal arrivalProductQty = partnerProductQualityRating.getArrivalProductQty();
        supplierQualityRating =
            supplierQualityRating.add(qualityRating.multiply(arrivalProductQty));
        supplierArrivalProductQty = supplierArrivalProductQty.add(arrivalProductQty);
      }

      if (supplierArrivalProductQty.signum() > 0) {
        supplierQualityRating =
            supplierQualityRating.divide(supplierArrivalProductQty, 2, RoundingMode.HALF_UP);
      } else {
        supplierQualityRating = BigDecimal.ZERO;
      }
    }

    partner.setSupplierQualityRating(supplierQualityRating);
    partner.setSupplierQualityRatingSelect(computeQualityRatingSelect(supplierQualityRating));
    partner.setSupplierArrivalProductQty(supplierArrivalProductQty);
  }

  protected void forEachStockMoveLine(
      StockMove stockMove, Consumer<StockMoveLine> consumer, Runnable postBatchAction) {
    if (stockMove == null || consumer == null || stockMove.getId() == null) {
      return;
    }

    Query<StockMoveLine> query =
        stockMoveLineRepository
            .all()
            .filter("self.stockMove.id = :stockMoveId AND self.id > :lastSeenId")
            .bind("stockMoveId", stockMove.getId())
            .order("id");

    BatchProcessorHelper.builder()
        .clearEveryNBatch(0)
        .build()
        .<StockMoveLine>forEachByQuery(query, consumer, postBatchAction);
  }

  /**
   * Compute quality rating.
   *
   * @param compliantArrivalProductQty
   * @param arrivalProductQty
   * @return
   */
  protected BigDecimal computeQualityRating(
      BigDecimal compliantArrivalProductQty, BigDecimal arrivalProductQty) {
    return compliantArrivalProductQty
        .multiply(MAX_QUALITY_RATING)
        .divide(arrivalProductQty, 2, RoundingMode.HALF_UP);
  }

  /**
   * Compute quality rating selection value (rounding to the nearest half).
   *
   * @param qualityRating
   * @return
   */
  protected BigDecimal computeQualityRatingSelect(BigDecimal qualityRating) {
    final BigDecimal two = new BigDecimal(2);
    return qualityRating
        .multiply(two)
        .setScale(0, RoundingMode.HALF_UP)
        .divide(two, 2, RoundingMode.HALF_UP);
  }
}
