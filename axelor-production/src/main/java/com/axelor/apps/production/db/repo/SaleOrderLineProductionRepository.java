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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.service.SaleOrderLineBomService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.db.repo.SaleOrderLineSupplychainRepository;
import com.axelor.inject.Beans;
import com.axelor.studio.db.repo.AppSaleRepository;
import java.util.Map;

public class SaleOrderLineProductionRepository extends SaleOrderLineSupplychainRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long saleOrderLineId = (Long) json.get("id");
    if (saleOrderLineId == null) {
      return super.populate(json, context);
    }

    SaleOrderLine saleOrderLine = find(saleOrderLineId);
    if (saleOrderLine == null) {
      return super.populate(json, context);
    }

    String warningMessage = null;
    if (isGenerateLinesAllowed(saleOrderLine)) {
      warningMessage =
          Beans.get(SaleOrderLineBomService.class)
              .getMissingProdProcessWarningMessage(saleOrderLine);
    }
    saleOrderLine.setProductionWarningMessage(warningMessage);
    json.put("productionWarningMessage", warningMessage);

    return super.populate(json, context);
  }

  protected boolean isGenerateLinesAllowed(SaleOrderLine saleOrderLine) {
    return Boolean.TRUE.equals(saleOrderLine.getIsToProduce())
        && Beans.get(AppSaleService.class).getAppSale().getListDisplayTypeSelect()
            == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        && !Beans.get(AppProductionService.class)
            .getAppProduction()
            .getIsBomLineGenerationInSODisabled();
  }
}
