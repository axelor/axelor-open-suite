/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class InvoiceLineResponseGenerator extends ResponseGenerator {

  @Inject InvoiceLineRepository invoiceLineRepo;

  @Override
  public void init() {
    modelFields.addAll(
        Arrays.asList("id", "exTaxTotal", "price", "productName", "qty", "inTaxTotal", "taxLine"));
    extraFieldMap.put("_discountTotal", this::getDiscount);
    classType = InvoiceLine.class;
  }

  private BigDecimal getDiscount(Object object) {
    InvoiceLine line = (InvoiceLine) object;
    line = invoiceLineRepo.find(line.getId());
    BigDecimal totalWTDiscount = line.getPrice().multiply(line.getQty());
    BigDecimal totalInDiscount = line.getExTaxTotal();
    return totalWTDiscount.subtract(totalInDiscount).setScale(2, RoundingMode.HALF_EVEN);
  }
}
