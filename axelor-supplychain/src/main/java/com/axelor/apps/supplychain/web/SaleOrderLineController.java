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

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineContextHelper;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineCheckSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineOnSaleSupplyChangeService;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineViewSupplychainService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SaleOrderLineController {

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      SaleOrder saleOrder =
          ContextHelper.getContextParent(request.getContext(), SaleOrder.class, 1);

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);

      Beans.get(AnalyticLineModelService.class)
          .createAnalyticDistributionWithTemplate(analyticLineModel);

      response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
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
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      SaleOrder saleOrder =
          ContextHelper.getContextParent(request.getContext(), SaleOrder.class, 1);

      if (saleOrder == null) {
        return;
      }

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(analyticLineModel, saleOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder =
          ContextHelper.getContextParent(request.getContext(), SaleOrder.class, 1);

      if (saleOrder == null) {
        return;
      }

      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);

      if (Beans.get(AnalyticLineModelService.class)
          .analyzeAnalyticLineModel(analyticLineModel, saleOrder.getCompany())) {
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder =
          ContextHelper.getContextParent(request.getContext(), SaleOrder.class, 1);

      if (saleOrder == null || saleOrder.getCompany() == null) {
        return;
      }

      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);

      response.setValues(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAccountValueMap(analyticLineModel, saleOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSaleOrderLineListToInvoice(ActionRequest request, ActionResponse response) {
    try {
      List<Long> selectedLinesIDs =
          (Optional.ofNullable((List<Integer>) request.getContext().get("_ids")))
              .stream()
                  .flatMap(List::stream)
                  .mapToLong(Integer::longValue)
                  .boxed()
                  .collect(Collectors.toList());
      List<SaleOrderLine> selectedSaleOrderLineList =
          Beans.get(SaleOrderLineRepository.class).findByIds(selectedLinesIDs);
      List<Map<String, Object>> selectedSaleOrderLineMapList =
          selectedSaleOrderLineList.stream().map(Mapper::toMap).collect(Collectors.toList());
      response.setView(
          ActionView.define(I18n.get("SOL to invoice"))
              .model(SaleOrderLine.class.getName())
              .add("form", "sale-order-line-multi-invoicing-form")
              .param("popup", "true")
              .param("popup-save", "false")
              .context("_saleOrderLineListToInvoice", selectedSaleOrderLineMapList)
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void saleSupplySelectOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    SaleOrder saleOrder =
        SaleOrderLineContextHelper.getSaleOrder(request.getContext(), saleOrderLine);
    SaleOrderLineOnSaleSupplyChangeService saleOrderLineOnSaleSupplyChangeService =
        Beans.get(SaleOrderLineOnSaleSupplyChangeService.class);
    response.setAttrs(
        saleOrderLineOnSaleSupplyChangeService.onSaleSupplyChangeAttrs(saleOrderLine, saleOrder));
    response.setValues(
        saleOrderLineOnSaleSupplyChangeService.onSaleSupplyChangeValues(saleOrderLine, saleOrder));

    // Check
    Beans.get(SaleOrderLineCheckSupplychainService.class)
        .saleSupplySelectOnChangeCheck(saleOrderLine, saleOrder);
  }

  @ErrorException
  public void getAnalyticDistributionTemplateDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context, saleOrderLine);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                saleOrderLine.getSupplierPartner(),
                saleOrderLine.getProduct(),
                saleOrder.getCompany(),
                saleOrder.getTradingName(),
                null,
                false));
  }

  public void setDistributionLineReadonly(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context, saleOrderLine);
    response.setAttrs(
        Beans.get(SaleOrderLineViewSupplychainService.class)
            .setDistributionLineReadonly(saleOrder));
  }
}
