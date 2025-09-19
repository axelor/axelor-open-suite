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
package com.axelor.apps.sale.service.saleorder.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaleOrderPackagingPlanServiceImpl implements SaleOrderPackagingPlanService {

  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;
  protected SaleOrderPackagingOrientationService saleOrderPackagingOrientationService;

  @Inject
  public SaleOrderPackagingPlanServiceImpl(
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService,
      SaleOrderPackagingOrientationService saleOrderPackagingOrientationService) {
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
    this.saleOrderPackagingOrientationService = saleOrderPackagingOrientationService;
  }

  @Override
  public boolean hasQtyRemaining(Map<Product, BigDecimal> productQtyMap) {
    return productQtyMap.values().stream().anyMatch(qty -> qty.signum() > 0);
  }

  @Override
  public Product chooseBestBox(
      Product product,
      List<Product> boxes,
      List<Product> products,
      Map<Product, BigDecimal> productQtyMap,
      Map<Product, BigDecimal> boxContents)
      throws AxelorException {
    Product bestBox = null;
    BigDecimal maxPlacedQty = BigDecimal.ZERO;
    BigDecimal bestBoxVolume = BigDecimal.ZERO;

    for (Product box : boxes) {
      if (!saleOrderPackagingOrientationService.canFit(product, box)) {
        continue;
      }
      Map<Product, BigDecimal> contents = new HashMap<>();
      Map<Product, BigDecimal> qtyMap = new HashMap<>(productQtyMap);

      BigDecimal totalWeight = fillBox(box, products, qtyMap, contents);

      BigDecimal minWeight = box.getMinWeight();
      if (totalWeight.compareTo(minWeight) < 0) {
        continue;
      }

      BigDecimal placedQty = contents.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal boxVolume = saleOrderPackagingDimensionService.getBoxInnerVolume(box);

      boolean better =
          (placedQty.compareTo(maxPlacedQty) > 0)
              || (placedQty.compareTo(maxPlacedQty) == 0 && boxVolume.compareTo(bestBoxVolume) < 0);

      if (better) {
        bestBox = box;
        maxPlacedQty = placedQty;
        bestBoxVolume = boxVolume;
        boxContents.clear();
        boxContents.putAll(contents);
      }
    }
    return bestBox;
  }

  protected BigDecimal fillBox(
      Product box,
      List<Product> products,
      Map<Product, BigDecimal> productQtyMap,
      Map<Product, BigDecimal> boxContents)
      throws AxelorException {
    List<BigDecimal[]> freeSpaces = new ArrayList<>();
    freeSpaces.add(
        new BigDecimal[] {box.getInnerLength(), box.getInnerWidth(), box.getInnerHeight()});

    BigDecimal totalWeight = box.getGrossMass();
    BigDecimal maxWeight = box.getMaxWeight();

    while (hasQtyRemaining(productQtyMap)) {
      boolean placed = false;

      freeSpaces.sort(
          (s1, s2) ->
              saleOrderPackagingDimensionService
                  .getSpaceVolume(s2)
                  .compareTo(saleOrderPackagingDimensionService.getSpaceVolume(s1)));

      for (int i = 0; i < freeSpaces.size(); i++) {
        BigDecimal[] space = freeSpaces.get(i);

        for (Product product : products) {
          BigDecimal qtyLeft = productQtyMap.getOrDefault(product, BigDecimal.ZERO);
          if (qtyLeft.compareTo(BigDecimal.ZERO) <= 0) {
            continue;
          }
          Object[] plan =
              planPlacement(
                  product,
                  space[0],
                  space[1],
                  space[2],
                  qtyLeft.longValue(),
                  totalWeight,
                  maxWeight);
          long placedQty = (long) plan[0];
          if (placedQty <= 0) {
            continue;
          }
          BigDecimal usedLength = (BigDecimal) plan[1];
          BigDecimal usedWidth = (BigDecimal) plan[2];
          BigDecimal usedHeight = (BigDecimal) plan[3];

          BigDecimal placeQty = BigDecimal.valueOf(placedQty);
          BigDecimal productWeight = product.getGrossMass().multiply(placeQty);
          totalWeight = totalWeight.add(productWeight);

          boxContents.merge(product, placeQty, BigDecimal::add);
          productQtyMap.put(product, qtyLeft.subtract(placeQty));

          List<BigDecimal[]> residualSpaces = splitSpace(space, usedLength, usedWidth, usedHeight);
          freeSpaces.remove(i);
          residualSpaces.forEach(r -> addValidSpace(freeSpaces, r));

          placed = true;
          break;
        }
        if (placed) {
          break;
        }
      }
      if (!placed) {
        break;
      }
    }
    return totalWeight;
  }

  protected Object[] planPlacement(
      Product product,
      BigDecimal spaceLength,
      BigDecimal spaceWidth,
      BigDecimal spaceHeight,
      long qtyLeft,
      BigDecimal currentBoxWeight,
      BigDecimal boxMaxWeight)
      throws AxelorException {
    BigDecimal[] dimensions = saleOrderPackagingDimensionService.getProductDimensions(product);
    BigDecimal[][] orientations = saleOrderPackagingOrientationService.getOrientations(dimensions);

    long bestQty = 0;
    BigDecimal bestVolume = null;
    BigDecimal bestLength = BigDecimal.ZERO;
    BigDecimal bestWidth = BigDecimal.ZERO;
    BigDecimal bestHeight = BigDecimal.ZERO;

    for (BigDecimal[] d : orientations) {
      BigDecimal length = d[0];
      BigDecimal width = d[1];
      BigDecimal height = d[2];

      long maxLength = spaceLength.divide(length, 0, RoundingMode.FLOOR).longValue();
      long maxWidth = spaceWidth.divide(width, 0, RoundingMode.FLOOR).longValue();
      long maxHeight = spaceHeight.divide(height, 0, RoundingMode.FLOOR).longValue();
      if (maxLength <= 0 || maxWidth <= 0 || maxHeight <= 0) {
        continue;
      }
      long capacity = maxLength * maxWidth * maxHeight;
      long qty = Math.min(qtyLeft, capacity);

      BigDecimal totalWeight =
          currentBoxWeight.add(product.getGrossMass().multiply(BigDecimal.valueOf(qty)));
      if (totalWeight.compareTo(boxMaxWeight) > 0) {
        BigDecimal allowedQty =
            boxMaxWeight
                .subtract(currentBoxWeight)
                .divide(product.getGrossMass(), RoundingMode.FLOOR);
        qty = Math.min(qty, allowedQty.longValue());
      }
      if (qty <= 0) {
        continue;
      }
      long[] block = bestBlock(qty, maxLength, maxWidth, maxHeight, length, width, height);

      BigDecimal usedLength = length.multiply(BigDecimal.valueOf(block[0]));
      BigDecimal usedWidth = width.multiply(BigDecimal.valueOf(block[1]));
      BigDecimal usedHeight = height.multiply(BigDecimal.valueOf(block[2]));
      BigDecimal volume = usedLength.multiply(usedWidth).multiply(usedHeight);

      boolean better =
          (qty > bestQty)
              || (qty == bestQty && (bestVolume == null || volume.compareTo(bestVolume) < 0))
              || (qty == bestQty
                  && volume.compareTo(bestVolume) == 0
                  && (usedHeight.compareTo(bestHeight) < 0
                      || (usedHeight.compareTo(bestHeight) == 0
                          && (usedWidth.compareTo(bestWidth) < 0
                              || (usedWidth.compareTo(bestWidth) == 0
                                  && usedLength.compareTo(bestLength) < 0)))));
      if (better) {
        bestQty = qty;
        bestVolume = volume;
        bestLength = usedLength;
        bestWidth = usedWidth;
        bestHeight = usedHeight;
      }
    }
    return bestQty <= 0
        ? new Object[] {0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO}
        : new Object[] {bestQty, bestLength, bestWidth, bestHeight};
  }

  protected long[] bestBlock(
      long qty,
      long maxLength,
      long maxWidth,
      long maxHeight,
      BigDecimal length,
      BigDecimal width,
      BigDecimal height) {
    long bestLength = 1;
    long bestWidth = 1;
    long bestHeight = 1;
    BigDecimal bestVolume =
        length.multiply(width).multiply(height).multiply(BigDecimal.valueOf(qty));
    boolean set = false;

    for (long l = 1; l <= maxLength; l++) {
      for (long w = 1; w <= maxWidth; w++) {
        long h = (qty + (l * w) - 1) / (l * w);
        if (h <= 0 || h > maxHeight) {
          continue;
        }
        BigDecimal usedLength = length.multiply(BigDecimal.valueOf(l));
        BigDecimal usedWidth = width.multiply(BigDecimal.valueOf(w));
        BigDecimal usedHeight = height.multiply(BigDecimal.valueOf(h));
        BigDecimal volume = usedLength.multiply(usedWidth).multiply(usedHeight);

        if (!set
            || volume.compareTo(bestVolume) < 0
            || (volume.compareTo(bestVolume) == 0
                && (usedHeight.compareTo(height.multiply(BigDecimal.valueOf(bestHeight))) < 0
                    || (usedHeight.compareTo(height.multiply(BigDecimal.valueOf(bestHeight))) == 0
                        && (usedWidth.compareTo(width.multiply(BigDecimal.valueOf(bestWidth))) < 0
                            || (usedWidth.compareTo(width.multiply(BigDecimal.valueOf(bestWidth)))
                                    == 0
                                && usedLength.compareTo(
                                        length.multiply(BigDecimal.valueOf(bestLength)))
                                    < 0)))))) {
          bestLength = l;
          bestWidth = w;
          bestHeight = h;
          bestVolume = volume;
          set = true;
        }
      }
    }
    return new long[] {bestLength, bestWidth, bestHeight};
  }

  protected List<BigDecimal[]> splitSpace(
      BigDecimal[] space, BigDecimal usedLength, BigDecimal usedWidth, BigDecimal usedHeight) {
    List<BigDecimal[]> spaces = new ArrayList<>();
    spaces.add(new BigDecimal[] {space[0].subtract(usedLength), space[1], space[2]});
    spaces.add(new BigDecimal[] {usedLength, space[1].subtract(usedWidth), space[2]});
    spaces.add(new BigDecimal[] {usedLength, usedWidth, space[2].subtract(usedHeight)});
    return spaces.stream()
        .filter(s -> s[0].signum() > 0 && s[1].signum() > 0 && s[2].signum() > 0)
        .collect(Collectors.toList());
  }

  protected void addValidSpace(List<BigDecimal[]> freeSpaces, BigDecimal[] space) {
    if (space != null
        && space[0].compareTo(BigDecimal.ZERO) > 0
        && space[1].compareTo(BigDecimal.ZERO) > 0
        && space[2].compareTo(BigDecimal.ZERO) > 0) {
      freeSpaces.add(space);
    }
  }
}
