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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.packaging.PackagingLineService;
import com.axelor.apps.supplychain.service.packaging.PackagingService;
import com.axelor.apps.supplychain.service.packaging.PackagingStockMoveLineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PackagingController {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void addPackagingLines(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      List<HashMap<String, Object>> stockMoveLineList =
          (List<HashMap<String, Object>>) request.getContext().get("stockMoveLineList");
      if (context.get("packaging") == null || CollectionUtils.isEmpty(stockMoveLineList)) {
        return;
      }
      Long packagingId =
          Long.parseLong(((LinkedHashMap) context.get("packaging")).get("id").toString());

      List<StockMoveLine> selectedStockMoveLineList =
          stockMoveLineList.stream()
              .filter(map -> Boolean.TRUE.equals(map.get("selected")))
              .map(
                  map ->
                      Beans.get(StockMoveLineRepository.class)
                          .find(((Integer) map.get("id")).longValue()))
              .collect(Collectors.toList());

      if (CollectionUtils.isEmpty(selectedStockMoveLineList)) {
        response.setInfo(I18n.get(SupplychainExceptionMessage.NO_ANY_STOCK_MOVE_LINE_SELECTED));
        return;
      }
      Packaging packaging = Beans.get(PackagingRepository.class).find(packagingId);
      Beans.get(PackagingLineService.class).addPackagingLines(packaging, selectedStockMoveLineList);
      Beans.get(PackagingStockMoveLineService.class).updateQtyRemainingToPackage(packaging);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addChildPackaging(ActionRequest request, ActionResponse response) {
    Packaging packaging = request.getContext().asType(Packaging.class);
    packaging = Beans.get(PackagingRepository.class).find(packaging.getId());
    Beans.get(PackagingService.class).addChildPackaging(packaging);
    response.setReload(true);
  }
}
