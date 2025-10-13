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
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.packaging.PackagingLineService;
import com.axelor.apps.supplychain.service.packaging.PackagingMassService;
import com.axelor.apps.supplychain.service.packaging.PackagingService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.ContextHelper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PackagingController {

  public void viewPackagingLinesFromStockMoveLines(ActionRequest request, ActionResponse response) {
    Packaging packaging = request.getContext().asType(Packaging.class);
    LogisticalForm logisticalForm =
        ContextHelper.getOriginParent(request.getContext(), LogisticalForm.class);
    List<StockMoveLine> stockMoveLineList =
        logisticalForm.getStockMoveList().stream()
            .flatMap(stockMove -> stockMove.getStockMoveLineList().stream())
            .filter(line -> line.getQtyRemainingToPackage().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());

    response.setView(
        ActionView.define(I18n.get("Add packaging lines"))
            .model(Wizard.class.getName())
            .add("form", "packaging-lines-from-stock-move-lines-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .context("_packagingId", packaging.getId())
            .context("_logisticalFormId", logisticalForm.getId())
            .context("_stockMoveLineList", stockMoveLineList)
            .map());
  }

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

      StockMoveLineRepository stockMoveLineRepository = Beans.get(StockMoveLineRepository.class);

      List<StockMoveLine> selectedStockMoveLineList =
          stockMoveLineList.stream()
              .filter(map -> Boolean.TRUE.equals(map.get("selected")))
              .map(map -> stockMoveLineRepository.find(((Integer) map.get("id")).longValue()))
              .collect(Collectors.toList());

      if (CollectionUtils.isEmpty(selectedStockMoveLineList)) {
        response.setInfo(I18n.get(SupplychainExceptionMessage.NO_ANY_STOCK_MOVE_LINE_SELECTED));
        return;
      }
      Packaging packaging = Beans.get(PackagingRepository.class).find(packagingId);
      Beans.get(PackagingLineService.class).addPackagingLines(packaging, selectedStockMoveLineList);
      Beans.get(PackagingMassService.class).updatePackagingMass(packaging);
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

  @SuppressWarnings("unchecked")
  public void removePackagings(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    if (context.get("_ids") == null) {
      return;
    }
    PackagingRepository packagingRepository = Beans.get(PackagingRepository.class);
    List<Integer> packagingIds = (List<Integer>) context.get("_ids");
    List<Packaging> packagingList =
        packagingIds.stream()
            .map(id -> packagingRepository.find(id.longValue()))
            .collect(Collectors.toList());
    Beans.get(PackagingService.class).removePackagings(packagingList);
  }
}
