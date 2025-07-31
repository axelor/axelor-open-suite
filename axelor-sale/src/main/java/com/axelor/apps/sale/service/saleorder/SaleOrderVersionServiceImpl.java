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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnLineChangeService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class SaleOrderVersionServiceImpl implements SaleOrderVersionService {

  protected SaleOrderRepository saleOrderRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected AppBaseService appBaseService;
  protected SaleOrderOnLineChangeService saleOrderOnLineChangeService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderVersionServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      AppBaseService appBaseService,
      SaleOrderOnLineChangeService saleOrderOnLineChangeService,
      AppSaleService appSaleService) {
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.appBaseService = appBaseService;
    this.saleOrderOnLineChangeService = saleOrderOnLineChangeService;
    this.appBaseService = appSaleService;
    this.appSaleService = appSaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createNewVersion(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    saleOrderLineList.sort(Comparator.comparing(SaleOrderLine::getId));
    saleOrderLineList.forEach(saleOrderLine -> historizeSaleOrderLine(saleOrder, saleOrderLine));
    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    saleOrder.setVersionNumber(saleOrder.getVersionNumber() + 1);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void historizeSaleOrderLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    SaleOrderLine oldVersionSaleOrderLine = saleOrderLineRepository.copy(saleOrderLine, false);
    oldVersionSaleOrderLine.setSaleOrder(null);
    oldVersionSaleOrderLine.setOldVersionSaleOrder(saleOrder);
    oldVersionSaleOrderLine.setVersionNumber(saleOrder.getVersionNumber());
    oldVersionSaleOrderLine.setVersionDateT(
        appBaseService.getTodayDateTime(saleOrder.getCompany()).toLocalDateTime());
    oldVersionSaleOrderLine.setArchived(true);
    historizeMainSaleOrderLine(
        saleOrder, saleOrderLine.getMainSaleOrderLine(), oldVersionSaleOrderLine);
    saleOrderLineRepository.save(oldVersionSaleOrderLine);
  }

  protected void historizeMainSaleOrderLine(
      SaleOrder saleOrder, SaleOrderLine mainSaleOrderLine, SaleOrderLine oldVersionSaleOrderLine) {
    if (mainSaleOrderLine == null
        || appSaleService.getAppSale() == null
        || !appSaleService.getAppSale().getManagePartnerComplementaryProduct()) {
      return;
    }
    SaleOrderLine line =
        saleOrderLineRepository
            .all()
            .filter(
                "self.product = :product AND self.id != :id AND self.oldVersionSaleOrder = :saleOrder AND self.versionNumber = :versionNumber")
            .bind("product", mainSaleOrderLine.getProduct())
            .bind("id", mainSaleOrderLine.getId())
            .bind("saleOrder", saleOrder)
            .bind("versionNumber", saleOrder.getVersionNumber())
            .order("id")
            .fetchOne();
    oldVersionSaleOrderLine.setMainSaleOrderLine(line);
  }

  @Override
  public LocalDateTime getVersionDateTime(SaleOrder saleOrder, Integer versionNumber) {
    List<SaleOrderLine> versionList = getOldVersionSaleOrderLines(saleOrder, versionNumber);
    if (!versionList.isEmpty()) {
      return versionList.get(0).getVersionDateT();
    }
    return null;
  }

  protected List<SaleOrderLine> getOldVersionSaleOrderLines(
      SaleOrder saleOrder, Integer versionNumber) {
    return saleOrderLineRepository
        .all()
        .filter(
            "self.oldVersionSaleOrder.id = :saleOrderId AND self.versionNumber = :versionNumber")
        .bind("saleOrderId", saleOrder.getId())
        .bind("versionNumber", versionNumber)
        .fetch();
  }

  @Override
  public Integer getCorrectedVersionNumber(Integer versionNumber, Integer previousVersionNumber) {
    if (previousVersionNumber < 1) {
      return 1;
    }
    if (previousVersionNumber > versionNumber - 1) {
      return versionNumber - 1;
    }
    return previousVersionNumber;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean recoverVersion(
      SaleOrder saleOrder, Integer versionNumber, boolean saveActualVersion)
      throws AxelorException {
    boolean isNewVersion = !saleOrder.getSaleOrderLineList().isEmpty() && saveActualVersion;
    if (!saveActualVersion) {
      saleOrder.getSaleOrderLineList().clear();
    }
    createNewVersion(saleOrder);
    saleOrder.clearSaleOrderLineList();
    List<SaleOrderLine> saleOrderLines = getOldVersionSaleOrderLines(saleOrder, versionNumber);
    saleOrderLines.sort(Comparator.comparing(SaleOrderLine::getId));
    saleOrderLines.forEach(
        oldVersionSaleOrderLine -> recoverSaleOrderLine(saleOrder, oldVersionSaleOrderLine));
    if (!isNewVersion) {
      saleOrder.setVersionNumber(saleOrder.getVersionNumber() - 1);
    }
    saleOrderOnLineChangeService.onLineChange(saleOrder);
    return isNewVersion;
  }

  protected void recoverSaleOrderLine(SaleOrder saleOrder, SaleOrderLine oldVersionSaleOrderLine) {
    SaleOrderLine saleOrderLine = saleOrderLineRepository.copy(oldVersionSaleOrderLine, false);
    saleOrderLine.setOldVersionSaleOrder(null);
    saleOrderLine.setSaleOrder(saleOrder);
    saleOrder.setArchived(null);
    recoverMainSaleOrderLine(
        saleOrder, oldVersionSaleOrderLine.getMainSaleOrderLine(), saleOrderLine);
    saleOrder.addSaleOrderLineListItem(saleOrderLine);
  }

  protected void recoverMainSaleOrderLine(
      SaleOrder saleOrder, SaleOrderLine mainSaleOrderLine, SaleOrderLine saleOrderLine) {
    if (mainSaleOrderLine == null
        || appSaleService.getAppSale() == null
        || !appSaleService.getAppSale().getManagePartnerComplementaryProduct()) {
      return;
    }
    SaleOrderLine line =
        saleOrderLineRepository
            .all()
            .filter(
                "self.product = :product AND self.id != :id AND self.saleOrder = :saleOrder AND self.versionNumber = :versionNumber")
            .bind("product", mainSaleOrderLine.getProduct())
            .bind("id", mainSaleOrderLine.getId())
            .bind("saleOrder", saleOrder)
            .bind("versionNumber", saleOrderLine.getVersionNumber())
            .order("id")
            .fetchOne();
    saleOrderLine.setMainSaleOrderLine(line);
  }
}
