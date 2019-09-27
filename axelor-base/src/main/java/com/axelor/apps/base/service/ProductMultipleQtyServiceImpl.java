/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.tool.ContextTool;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionResponse;
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

      LOG.debug(
          "Check on multiple qty : {}, Modulo : {}",
          qty,
          qty.remainder(productMultipleQty.getMultipleQty()));

      if (qty.remainder(productMultipleQty.getMultipleQty()).compareTo(BigDecimal.ZERO) == 0) {
        return true;
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
          allowToForce ? ContextTool.SPAN_CLASS_WARNING : ContextTool.SPAN_CLASS_IMPORTANT;

      String message =
          String.format(
              I18n.get("Quantity should be a multiple of %s"),
              this.toStringMultipleQty(productMultipleQties));
      String title = ContextTool.formatLabel(message, spanClass, 75);

      response.setAttr("multipleQtyNotRespectedLabel", "title", title);
      response.setAttr("multipleQtyNotRespectedLabel", "hidden", false);
      response.setValue("$qtyValid", allowToForce);
    }
  }
}
