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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ContextHelper;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductMultipleQtyServiceImpl implements ProductMultipleQtyService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public boolean checkMultipleQty(BigDecimal qty, List<ProductMultipleQty> productMultipleQties) {

    if (productMultipleQties.size() == 0) {
      return true;
    }

    for (ProductMultipleQty productMultipleQty : productMultipleQties) {

      if (productMultipleQty.getMultipleQty().compareTo(BigDecimal.ZERO) != 0) {

        LOG.debug(
            "Check on multiple qty : {}, Modulo : {}",
            qty,
            qty.remainder(productMultipleQty.getMultipleQty()));

        if (qty.remainder(productMultipleQty.getMultipleQty()).compareTo(BigDecimal.ZERO) == 0) {
          return true;
        }
      }
    }

    return false;
  }

  public String toStringMultipleQty(List<ProductMultipleQty> productMultipleQties) {

    String message = "";

    for (ProductMultipleQty productMultipleQty : productMultipleQties) {
      if (message.length() > 0) {
        message += " " + I18n.get("or") + " ";
      }
      message += productMultipleQty.getMultipleQty();
      if (!Strings.isNullOrEmpty(productMultipleQty.getName())) {
        message += " (" + productMultipleQty.getName() + ")";
      }
    }

    return message;
  }

  public void checkMultipleQty(
      BigDecimal qty,
      List<ProductMultipleQty> productMultipleQties,
      boolean allowToForce,
      ActionResponse response) {

    boolean isMultiple = this.checkMultipleQty(qty, productMultipleQties);

    if (isMultiple) {
      response.setAttr("multipleQtyNotRespectedLabel", "hidden", true);
      response.setValue("$qtyValid", true);
    } else {
      String spanClass =
          allowToForce ? ContextHelper.SPAN_CLASS_WARNING : ContextHelper.SPAN_CLASS_IMPORTANT;

      String message =
          String.format(
              I18n.get("Quantity should be a multiple of %s"),
              this.toStringMultipleQty(productMultipleQties));
      String title = ContextHelper.formatLabel(message, spanClass, 75);

      response.setAttr("multipleQtyNotRespectedLabel", "title", title);
      response.setAttr("multipleQtyNotRespectedLabel", "hidden", false);
      response.setValue("$qtyValid", allowToForce);
    }
  }
}
