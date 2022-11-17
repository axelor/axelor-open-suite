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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SaleOrderLineController {

  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
      saleOrderLine =
          Beans.get(SaleOrderLineServiceSupplyChain.class)
              .computeAnalyticDistribution(saleOrderLine);
      response.setValue(
          "analyticDistributionTemplate", saleOrderLine.getAnalyticDistributionTemplate());
      response.setValue("analyticMoveLineList", saleOrderLine.getAnalyticMoveLineList());
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    saleOrderLine =
        Beans.get(SaleOrderLineServiceSupplyChain.class)
            .createAnalyticDistributionWithTemplate(saleOrderLine);
    response.setValue("analyticMoveLineList", saleOrderLine.getAnalyticMoveLineList());
  }

  public void checkStocks(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    SaleOrder saleOrder =
        Beans.get(SaleOrderLineServiceSupplyChainImpl.class).getSaleOrder(request.getContext());
    if (saleOrder.getStockLocation() == null) {
      return;
    }
    try {
      if (saleOrderLine.getSaleSupplySelect() != SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK) {
        return;
      }
      // Use the unit to get the right quantity
      Unit unit = null;
      if (saleOrderLine.getProduct() != null) unit = saleOrderLine.getProduct().getUnit();
      BigDecimal qty = saleOrderLine.getQty();
      if (unit != null && !unit.equals(saleOrderLine.getUnit())) {
        qty =
            Beans.get(UnitConversionService.class)
                .convert(
                    saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
      }
      Beans.get(StockLocationLineService.class)
          .checkIfEnoughStock(saleOrder.getStockLocation(), saleOrderLine.getProduct(), qty);
    } catch (Exception e) {
      response.setAlert(e.getLocalizedMessage());
    }
  }

  public void fillAvailableAndAllocatedStock(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    SaleOrderLineServiceSupplyChainImpl saleOrderLineServiceSupplyChainImpl =
        Beans.get(SaleOrderLineServiceSupplyChainImpl.class);
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = saleOrderLineServiceSupplyChainImpl.getSaleOrder(context);

    if (saleOrder != null) {
      if (saleOrderLine.getProduct() != null && saleOrder.getStockLocation() != null) {
        BigDecimal availableStock =
            saleOrderLineServiceSupplyChainImpl.getAvailableStock(saleOrder, saleOrderLine);
        BigDecimal allocatedStock =
            saleOrderLineServiceSupplyChainImpl.getAllocatedStock(saleOrder, saleOrderLine);

        response.setValue("$availableStock", availableStock);
        response.setValue("$allocatedStock", allocatedStock);
        response.setValue("$totalStock", availableStock.add(allocatedStock));
      }
    }
  }

  /**
   * Called from sale order line request quantity wizard view. Call {@link
   * ReservedQtyService#updateReservedQty(SaleOrderLine, BigDecimal)}.
   *
   * @param request
   * @param response
   */
  public void changeReservedQty(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    BigDecimal newReservedQty = saleOrderLine.getReservedQty();
    try {
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Product product = saleOrderLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).updateReservedQty(saleOrderLine, newReservedQty);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeRequestedReservedQty(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    BigDecimal newReservedQty = saleOrderLine.getRequestedReservedQty();
    try {
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Beans.get(ReservedQtyService.class).updateRequestedReservedQty(saleOrderLine, newReservedQty);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order line form view, on request qty click. Call {@link
   * ReservedQtyService#requestQty(SaleOrderLine)}
   *
   * @param request
   * @param response
   */
  public void requestQty(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Product product = saleOrderLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).requestQty(saleOrderLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order line form view, on request qty click. Call {@link
   * ReservedQtyService#cancelReservation(SaleOrderLine)}
   *
   * @param request
   * @param response
   */
  public void cancelReservation(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Product product = saleOrderLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).cancelReservation(saleOrderLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order line form. Set domain for supplier partner.
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDomain(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    String domain = "self.isContact = false AND self.isSupplier = true";
    Product product = saleOrderLine.getProduct();
    if (product != null) {
      List<Long> authorizedPartnerIdsList =
          Beans.get(SaleOrderLineServiceSupplyChain.class).getSupplierPartnerList(saleOrderLine);
      if (authorizedPartnerIdsList.isEmpty()) {
        response.setAttr("supplierPartner", "domain", "self.id IN (0)");
        return;
      } else {
        domain +=
            String.format(
                " AND self.id IN (%s)",
                authorizedPartnerIdsList.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
      }
    }
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    if (saleOrder == null) {
      Context parentContext = request.getContext().getParent();
      if (parentContext == null) {
        response.setAttr("supplierPartner", "domain", domain);
        return;
      }
      saleOrder = parentContext.asType(SaleOrder.class);
      if (saleOrder == null) {
        response.setAttr("supplierPartner", "domain", domain);
        return;
      }
    }
    String blockedPartnerQuery =
        Beans.get(BlockingService.class)
            .listOfBlockedPartner(saleOrder.getCompany(), BlockingRepository.PURCHASE_BLOCKING);

    if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
      domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
    }

    if (saleOrder.getCompany() != null) {
      domain += " AND " + saleOrder.getCompany().getId() + " in (SELECT id FROM self.companySet)";
    }

    response.setAttr("supplierPartner", "domain", domain);
  }

  /**
   * Called from sale order line form, on product change and on sale supply select change
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDefault(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    if (saleOrderLine.getSaleSupplySelect() != SaleOrderLineRepository.SALE_SUPPLY_PURCHASE) {
      return;
    }

    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    if (saleOrder == null) {
      Context parentContext = request.getContext().getParent();
      if (parentContext == null) {
        return;
      }
      saleOrder = parentContext.asType(SaleOrder.class);
    }
    if (saleOrder == null) {
      return;
    }

    Partner supplierPartner = null;
    if (saleOrderLine.getProduct() != null) {
      supplierPartner = saleOrderLine.getProduct().getDefaultSupplierPartner();
    }

    if (supplierPartner != null) {
      Blocking blocking =
          Beans.get(BlockingService.class)
              .getBlocking(
                  supplierPartner, saleOrder.getCompany(), BlockingRepository.PURCHASE_BLOCKING);
      if (blocking != null) {
        supplierPartner = null;
      }
    }

    response.setValue("supplierPartner", supplierPartner);
  }

  /**
   * Called from sale order form view, on clicking allocateAll button on one sale order line. Call
   * {@link ReservedQtyService#allocateAll(SaleOrderLine)}.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Product product = saleOrderLine.getProduct();
      if (product == null || !product.getStockManaged()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
      }
      Beans.get(ReservedQtyService.class).allocateAll(saleOrderLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order form view, on clicking deallocate button on one sale order line. Call
   * {@link ReservedQtyService#updateReservedQty(SaleOrderLine, BigDecimal.ZERO)}.
   *
   * @param request
   * @param response
   */
  public void deallocateAll(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Beans.get(ReservedQtyService.class).updateReservedQty(saleOrderLine, BigDecimal.ZERO);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkInvoicedOrDeliveredOrderQty(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

    SaleOrderLineServiceSupplyChain saleOrderLineService =
        Beans.get(SaleOrderLineServiceSupplyChain.class);

    BigDecimal qty = saleOrderLineService.checkInvoicedOrDeliveredOrderQty(saleOrderLine);

    saleOrderLineService.updateDeliveryState(saleOrderLine);

    response.setValue("qty", qty);
    response.setValue("deliveryState", saleOrderLine.getDeliveryState());
  }

  /**
   * Called from sale order line, on desired delivery date change. Call {@link
   * SaleOrderLineServiceSupplyChain#updateStockMoveReservationDateTime(SaleOrderLine)}.
   *
   * @param request
   * @param response
   */
  public void updateReservationDate(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());
      Beans.get(SaleOrderLineServiceSupplyChain.class)
          .updateStockMoveReservationDateTime(saleOrderLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = null;
      if (request.getContext().getParent() != null
          && (SaleOrder.class).equals(request.getContext().getParent().getContextClass())) {
        saleOrder = request.getContext().getParent().asType(SaleOrder.class);
      }

      AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        List<Long> analyticAccountList = new ArrayList<>();
        if (saleOrder != null
            && analyticToolService.isPositionUnderAnalyticAxisSelect(saleOrder.getCompany(), i)) {

          AnalyticAxis analyticAxis = new AnalyticAxis();

          for (AnalyticAxisByCompany axis :
              Beans.get(AccountConfigService.class)
                  .getAccountConfig(saleOrder.getCompany())
                  .getAnalyticAxisByCompanyList()) {
            if (axis.getSequence() + 1 == i) {
              analyticAxis = axis.getAnalyticAxis();
            }
          }

          for (AnalyticAccount analyticAccount :
              Beans.get(AnalyticAccountRepository.class).findByAnalyticAxis(analyticAxis).fetch()) {
            analyticAccountList.add(analyticAccount.getId());
          }

          if (ObjectUtils.isEmpty(analyticAccountList)) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "domain",
                "self.id IN (0)");
          } else {
            if (saleOrder.getCompany() != null) {
              String idList =
                  analyticAccountList.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","));

              response.setAttr(
                  "axis" + i + "AnalyticAccount",
                  "domain",
                  "self.id IN ("
                      + idList
                      + ") AND self.statusSelect = "
                      + AnalyticAccountRepository.STATUS_ACTIVE
                      + " AND (self.company is null OR self.company.id = "
                      + saleOrder.getCompany().getId()
                      + ")");
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().getParent() != null
          && (SaleOrder.class).equals(request.getContext().getParent().getContextClass())) {

        SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
        SaleOrder saleOrder = request.getContext().getParent().asType(SaleOrder.class);
        if (saleOrder != null
            && Beans.get(MoveLineComputeAnalyticService.class)
                .checkManageAnalytic(saleOrder.getCompany())) {
          saleOrderLine =
              Beans.get(SaleOrderLineServiceSupplyChain.class)
                  .analyzeSaleOrderLine(saleOrderLine, saleOrder, saleOrder.getCompany());
          response.setValue("analyticMoveLineList", saleOrderLine.getAnalyticMoveLineList());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = null;

      if (request.getContext().getParent() != null
          && (SaleOrder.class).equals(request.getContext().getParent().getContextClass())) {
        saleOrder = request.getContext().getParent().asType(SaleOrder.class);
      }

      if (saleOrder != null && saleOrder.getCompany() != null) {
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(saleOrder.getCompany());
        if (Beans.get(MoveLineComputeAnalyticService.class)
            .checkManageAnalytic(saleOrder.getCompany())) {
          AnalyticAxis analyticAxis = null;
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                "hidden",
                !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
            for (AnalyticAxisByCompany analyticAxisByCompany :
                accountConfig.getAnalyticAxisByCompanyList()) {
              if (analyticAxisByCompany.getSequence() + 1 == i) {
                analyticAxis = analyticAxisByCompany.getAnalyticAxis();
              }
            }
            if (analyticAxis != null) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "title",
                  analyticAxis.getName());
              analyticAxis = null;
            }
          }
        } else {
          response.setAttr("analyticDistributionTemplate", "hidden", true);
          response.setAttr("analyticMoveLineList", "hidden", true);
          for (int i = startAxisPosition; i <= endAxisPosition; i++) {
            response.setAttr(
                "axis".concat(Integer.toString(i)).concat("AnalyticAccount"), "hidden", true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = null;
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      if (request.getContext().getParent() != null
          && (SaleOrder.class).equals(request.getContext().getParent().getContextClass())) {
        saleOrder = request.getContext().getParent().asType(SaleOrder.class);
      }
      if (saleOrderLine != null && saleOrder != null) {
        Beans.get(SaleOrderLineServiceSupplyChain.class)
            .printAnalyticAccount(saleOrderLine, saleOrder.getCompany());
        response.setValues(saleOrderLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
