/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineComputationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class FixedAssetServiceSupplyChainImpl extends FixedAssetServiceImpl {

  @Inject
  public FixedAssetServiceSupplyChainImpl(
      FixedAssetRepository fixedAssetRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      FixedAssetLineComputationService fixedAssetLineComputationService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AccountConfigService accountConfigService) {
    super(
        fixedAssetRepo,
        fixedAssetLineMoveService,
        fixedAssetLineComputationService,
        moveLineComputeAnalyticService,
        accountConfigService);
  }

  @Transactional
  @Override
  public List<FixedAsset> createFixedAssets(Invoice invoice) throws AxelorException {

    List<FixedAsset> fixedAssetList = super.createFixedAssets(invoice);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return fixedAssetList;
    }

    if (fixedAssetList.isEmpty()) {
      return new ArrayList<>();
    }

    StockLocation stockLocation =
        invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getStockLocation() : null;

    for (FixedAsset fixedAsset : fixedAssetList) {

      PurchaseOrderLine pol = fixedAsset.getInvoiceLine().getPurchaseOrderLine();

      fixedAsset.setStockLocation(stockLocation);

      if (fixedAsset.getInvoiceLine().getIncomingStockMove() != null
          && CollectionUtils.isNotEmpty(
              fixedAsset.getInvoiceLine().getIncomingStockMove().getStockMoveLineList())) {
        fixedAsset.setTrackingNumber(
            fixedAsset.getInvoiceLine().getIncomingStockMove().getStockMoveLineList().stream()
                .filter(l -> pol.equals(l.getPurchaseOrderLine()))
                .findFirst()
                .map(StockMoveLine::getTrackingNumber)
                .orElse(null));
        fixedAsset.setStockLocation(
            fixedAsset.getInvoiceLine().getIncomingStockMove().getToStockLocation());
      }
    }

    return fixedAssetList;
  }
}
