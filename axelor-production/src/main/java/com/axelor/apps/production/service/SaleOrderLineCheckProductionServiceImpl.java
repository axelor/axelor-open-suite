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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCheckProductionServiceImpl
    implements SaleOrderLineCheckProductionService {

  protected final ManufOrderRepository manufOrderRepository;

  @Inject
  public SaleOrderLineCheckProductionServiceImpl(ManufOrderRepository manufOrderRepository) {
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public void checkLinkedMo(SaleOrderLine saleOrderLine) throws AxelorException {
    List<ManufOrder> manufOrderList =
        manufOrderRepository
            .all()
            .filter("self.saleOrderLine = :saleOrderLine")
            .bind("saleOrderLine", saleOrderLine)
            .autoFlush(false)
            .fetch();

    if (CollectionUtils.isNotEmpty(manufOrderList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.SOL_LINKED_TO_MO_DELETE_ERROR),
          saleOrderLine.getProduct().getFullName(),
          StringHtmlListBuilder.formatMessage(
              manufOrderList.stream()
                  .map(ManufOrder::getManufOrderSeq)
                  .collect(Collectors.toList())));
    }
  }
}
