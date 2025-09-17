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

import com.axelor.apps.base.db.Product;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderPackagingMessageServiceImpl implements SaleOrderPackagingMessageService {

  @Override
  public String formatPackagingMessage(String title, List<String> messages) {
    if (CollectionUtils.isEmpty(messages)) {
      return "";
    }
    return StringHtmlListBuilder.formatMessage(I18n.get(title), messages);
  }

  @Override
  public void updatePackagingMessage(
      Product selectedBox,
      Map<Product, BigDecimal> boxContents,
      Map<Product, BigDecimal> productQtyMap,
      List<String> messages,
      Map<Product, String> descMap,
      Map<Product, BigDecimal[]> weightMap) {

    for (Map.Entry<Product, BigDecimal> entry : boxContents.entrySet()) {
      productQtyMap.put(
          entry.getKey(), productQtyMap.get(entry.getKey()).subtract(entry.getValue()));
    }
    BigDecimal totalWeight = selectedBox.getGrossMass();
    BigDecimal totalNetMass = BigDecimal.ZERO;

    for (Map.Entry<Product, BigDecimal> entry : boxContents.entrySet()) {
      Product subProduct = entry.getKey();
      BigDecimal qty = entry.getValue();

      if (subProduct.getIsPackaging()) {
        BigDecimal[] subWeights =
            weightMap.getOrDefault(
                subProduct, new BigDecimal[] {subProduct.getNetMass(), subProduct.getGrossMass()});
        totalNetMass = totalNetMass.add(subWeights[0].multiply(qty));
        totalWeight = totalWeight.add(subWeights[1].multiply(qty));
      } else {
        totalNetMass = totalNetMass.add(subProduct.getNetMass().multiply(qty));
        totalWeight = totalWeight.add(subProduct.getGrossMass().multiply(qty));
      }
    }

    String contentDescription =
        boxContents.entrySet().stream()
            .map(
                e ->
                    e.getValue().stripTrailingZeros().toPlainString()
                        + "x"
                        + I18n.get(e.getKey().getName()))
            .collect(Collectors.joining(" + "));

    messages.add(
        String.format(
            "1 x %s – (%s) – %.2fx%.2fx%.2f mm – Net: %.2f kg, Gross: %.2f kg",
            I18n.get(selectedBox.getName()),
            contentDescription,
            selectedBox.getOuterLength(),
            selectedBox.getOuterWidth(),
            selectedBox.getOuterHeight(),
            totalNetMass,
            totalWeight));

    descMap.put(selectedBox, contentDescription);
    weightMap.put(selectedBox, new BigDecimal[] {totalNetMass, totalWeight});
  }
}
