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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderPackagingServiceImpl implements SaleOrderPackagingService {

  protected static final BigDecimal THICKNESS = BigDecimal.valueOf(5); // (in mm)
  protected static final String UNIT_MILLIMETER = "mm";

  protected ProductRepository productRepository;
  protected UnitConversionService unitConversionService;
  protected UnitRepository unitRepository;

  @Inject
  public SaleOrderPackagingServiceImpl(
      ProductRepository productRepository,
      UnitConversionService unitConversionService,
      UnitRepository unitRepository) {
    this.productRepository = productRepository;
    this.unitConversionService = unitConversionService;
    this.unitRepository = unitRepository;
  }

  @Override
  public String estimatePackaging(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    Map<Product, BigDecimal> productQtyMap =
        saleOrderLines.stream()
            .filter(line -> line.getQty().compareTo(BigDecimal.ZERO) > 0)
            .collect(
                Collectors.toMap(
                    SaleOrderLine::getProduct, SaleOrderLine::getQty, BigDecimal::add));

    List<Product> products = getSortedProducts(productQtyMap);
    List<String> messages = new ArrayList<>();
    List<Product> boxes = productRepository.all().filter("self.isPackaging = true").fetch();
    boxes.sort(Comparator.comparing(this::getBoxInnerVolume));

    while (hasRemaining(productQtyMap)) {
      Product nextProduct =
          products.stream()
              .filter(p -> productQtyMap.get(p).compareTo(BigDecimal.ZERO) > 0)
              .findFirst()
              .orElse(null);
      if (nextProduct == null) {
        break;
      }
      Map<Product, BigDecimal> boxContents = new HashMap<>();
      Product selectedBox = chooseBestBox(nextProduct, boxes, products, productQtyMap, boxContents);
      if (selectedBox == null) {
        break;
      }
      for (Map.Entry<Product, BigDecimal> entry : boxContents.entrySet()) {
        productQtyMap.put(
            entry.getKey(), productQtyMap.get(entry.getKey()).subtract(entry.getValue()));
      }

      BigDecimal totalWeight = selectedBox.getNetMass();
      for (Map.Entry<Product, BigDecimal> entry : boxContents.entrySet()) {
        totalWeight = totalWeight.add(entry.getKey().getNetMass().multiply(entry.getValue()));
      }
      String contentDescription =
          boxContents.entrySet().stream()
              .map(
                  entry ->
                      entry.getValue().stripTrailingZeros().toPlainString()
                          + "x"
                          + I18n.get(entry.getKey().getName()))
              .collect(Collectors.joining(" + "));

      messages.add(
          String.format(
              "1 x %s – (%s) – %.2fx%.2fx%.2f mm – %.2f kg",
              I18n.get(selectedBox.getName()),
              contentDescription,
              selectedBox.getOuterLength(),
              selectedBox.getOuterWidth(),
              selectedBox.getOuterHeight(),
              totalWeight));
    }
    if (CollectionUtils.isEmpty(messages)) {
      return "";
    }
    return StringHtmlListBuilder.formatMessage(
        I18n.get("Packaging") + " " + I18n.get(saleOrder.getFullName()), messages);
  }

  protected Product chooseBestBox(
      Product product,
      List<Product> boxes,
      List<Product> products,
      Map<Product, BigDecimal> productQtyMap,
      Map<Product, BigDecimal> boxContents)
      throws AxelorException {
    Product bestBox = null;
    BigDecimal maxPlacedQty = BigDecimal.ZERO;

    for (Product box : boxes) {
      if (!canFit(product, box)) {
        continue;
      }
      Map<Product, BigDecimal> contents = new HashMap<>();
      Map<Product, BigDecimal> qtyMap = new HashMap<>(productQtyMap);

      fillBox(box, products, qtyMap, contents);

      BigDecimal placedQty = contents.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

      if (placedQty.compareTo(maxPlacedQty) > 0
          || (placedQty.compareTo(maxPlacedQty) == 0
              && getBoxInnerVolume(box).compareTo(getBoxInnerVolume(bestBox)) < 0)) {
        bestBox = box;
        maxPlacedQty = placedQty;
        boxContents.clear();
        boxContents.putAll(contents);
      }
    }
    return bestBox;
  }

  protected void fillBox(
      Product box,
      List<Product> products,
      Map<Product, BigDecimal> productQtyMap,
      Map<Product, BigDecimal> boxContents)
      throws AxelorException {
    List<BigDecimal[]> freeSpaces = new ArrayList<>();
    freeSpaces.add(
        new BigDecimal[] {box.getInnerLength(), box.getInnerWidth(), box.getInnerHeight()});

    while (hasRemaining(productQtyMap)) {
      boolean placed = false;

      freeSpaces.sort((s1, s2) -> getVolume(s2).compareTo(getVolume(s1)));

      for (int i = 0; i < freeSpaces.size(); i++) {
        BigDecimal[] space = freeSpaces.get(i);

        for (Product product : products) {
          BigDecimal qtyLeft = productQtyMap.getOrDefault(product, BigDecimal.ZERO);
          if (qtyLeft.compareTo(BigDecimal.ZERO) <= 0) {
            continue;
          }
          Object[] plan = planPlacement(product, space[0], space[1], space[2], qtyLeft.longValue());
          long placedQty = (long) plan[0];
          if (placedQty <= 0) {
            continue;
          }
          BigDecimal usedLength = (BigDecimal) plan[1];
          BigDecimal usedWidth = (BigDecimal) plan[2];
          BigDecimal usedHeight = (BigDecimal) plan[3];

          BigDecimal placeQty = BigDecimal.valueOf(placedQty);
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
  }

  protected Object[] planPlacement(
      Product product,
      BigDecimal spaceLength,
      BigDecimal spaceWidth,
      BigDecimal spaceHeight,
      long qtyLeft)
      throws AxelorException {
    BigDecimal[] dimensions = getProductDimensions(product);
    BigDecimal[][] orientations = getOrientations(dimensions);

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

  protected List<Product> getSortedProducts(Map<Product, BigDecimal> productQtyMap)
      throws AxelorException {
    Map<Product, BigDecimal> map = new HashMap<>();
    for (Product product : productQtyMap.keySet()) {
      BigDecimal volume =
          getConvertedDimension(product.getLength(), product)
              .multiply(getConvertedDimension(product.getWidth(), product))
              .multiply(getConvertedDimension(product.getHeight(), product));
      map.put(product, volume);
    }
    return productQtyMap.keySet().stream()
        .sorted((p1, p2) -> map.get(p2).compareTo(map.get(p1)))
        .collect(Collectors.toList());
  }

  protected boolean hasRemaining(Map<Product, BigDecimal> productQtyMap) {
    return productQtyMap.values().stream().anyMatch(qty -> qty.signum() > 0);
  }

  protected BigDecimal getVolume(BigDecimal[] space) {
    return space[0].multiply(space[1]).multiply(space[2]);
  }

  protected BigDecimal getBoxInnerVolume(Product box) {
    return box.getInnerLength().multiply(box.getInnerWidth()).multiply(box.getInnerHeight());
  }

  protected boolean canFit(Product product, Product box) throws AxelorException {
    BigDecimal[] productDims = getDimensions(product, false);
    BigDecimal[] boxDims = getDimensions(box, true);

    for (BigDecimal[] orientation : getOrientations(productDims)) {
      if (orientation[0].compareTo(boxDims[0]) <= 0
          && orientation[1].compareTo(boxDims[1]) <= 0
          && orientation[2].compareTo(boxDims[2]) <= 0) {
        return true;
      }
    }
    return false;
  }

  protected BigDecimal[][] getOrientations(BigDecimal[] dimensions) {
    return new BigDecimal[][] {
      {dimensions[0], dimensions[1], dimensions[2]},
      {dimensions[0], dimensions[2], dimensions[1]},
      {dimensions[1], dimensions[0], dimensions[2]},
      {dimensions[1], dimensions[2], dimensions[0]},
      {dimensions[2], dimensions[0], dimensions[1]},
      {dimensions[2], dimensions[1], dimensions[0]}
    };
  }

  protected BigDecimal[] getDimensions(Product product, boolean isBox) throws AxelorException {
    if (isBox) {
      return new BigDecimal[] {
        product.getInnerLength(), product.getInnerWidth(), product.getInnerHeight()
      };
    } else {
      return getProductDimensions(product);
    }
  }

  protected BigDecimal[] getProductDimensions(Product product) throws AxelorException {
    return new BigDecimal[] {
      getEffectiveDimension(product.getLength(), product),
      getEffectiveDimension(product.getWidth(), product),
      getEffectiveDimension(product.getHeight(), product)
    };
  }

  protected BigDecimal getEffectiveDimension(BigDecimal value, Product product)
      throws AxelorException {
    return getConvertedDimension(value, product).add(THICKNESS.multiply(BigDecimal.valueOf(2)));
  }

  protected BigDecimal getConvertedDimension(BigDecimal value, Product product)
      throws AxelorException {
    Unit unit = product.getLengthUnit();
    Unit targetUnit =
        unitRepository
            .all()
            .filter("self.labelToPrinting = :labelToPrinting")
            .bind("labelToPrinting", UNIT_MILLIMETER)
            .fetchOne();
    if (unit != null && !unit.equals(targetUnit)) {
      value =
          unitConversionService.convert(
              unit, targetUnit, value, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, product);
    }
    return value;
  }
}
