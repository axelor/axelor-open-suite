/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplierportal.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.inject.Beans;
import java.util.HashMap;
import java.util.Map;

public class SupplierViewServiceImpl implements SupplierViewService {

  protected static final String SUPPLIER_PORTAL_NO_DATE = /*$$(*/ "None" /*)*/;

  @Override
  public User getSupplierUser() {
    return Beans.get(UserService.class).getUser();
  }

  @Override
  public Map<String, Object> updateSupplierViewIndicators() {
    User user = getSupplierUser();

    Map<String, Object> map = new HashMap<>();

    /* PurchaseOrder */
    map.put("$orders", getCount(PurchaseOrder.class, getPurchaseOrdersOfSupplier(user)));
    map.put(
        "$quotationInProgress",
        getCount(PurchaseOrder.class, getPurchaseQuotationsInProgressOfSupplier(user)));
    PurchaseOrder lastOrder = getData(PurchaseOrder.class, getLastPurchaseOrderOfSupplier(user));
    L10n dateFormat = L10n.getInstance();
    map.put(
        "$lastOrder",
        lastOrder != null
            ? dateFormat.format(lastOrder.getValidationDateTime())
            : I18n.get(SUPPLIER_PORTAL_NO_DATE));

    /* StockMove */
    StockMove stockMoveLastDelivery = getData(StockMove.class, getLastDeliveryOfSupplier(user));
    map.put(
        "$lastDelivery",
        stockMoveLastDelivery != null
            ? dateFormat.format(stockMoveLastDelivery.getRealDate())
            : I18n.get(SUPPLIER_PORTAL_NO_DATE));

    StockMove stockMoveNextDelivery = getData(StockMove.class, getNextDeliveryOfSupplier(user));
    map.put(
        "$nextDelivery",
        stockMoveNextDelivery != null
            ? dateFormat.format(stockMoveNextDelivery.getEstimatedDate())
            : I18n.get(SUPPLIER_PORTAL_NO_DATE));

    map.put(
        "$deliveriesToPrepare", getCount(StockMove.class, getDeliveriesToPrepareOfSupplier(user)));

    /* Invoice */
    map.put("$overdueInvoices", getCount(Invoice.class, getOverdueInvoicesOfSupplier(user)));
    map.put("$awaitingInvoices", getCount(Invoice.class, getAwaitingInvoicesOfSupplier(user)));
    map.put("$totalRemaining", getCount(Invoice.class, getTotalRemainingOfSupplier(user)));

    return map;
  }

  /* PurchaseOrder Query */
  @Override
  public String getPurchaseOrdersOfSupplier(User user) {
    return "self.supplierPartner.id = "
        + user.getPartner().getId()
        + " AND self.statusSelect IN ("
        + PurchaseOrderRepository.STATUS_FINISHED
        + ","
        + PurchaseOrderRepository.STATUS_VALIDATED
        + ")";
  }

  @Override
  public String getPurchaseQuotationsInProgressOfSupplier(User user) {
    return "self.supplierPartner.id = "
        + user.getPartner().getId()
        + " AND self.statusSelect IN ("
        + PurchaseOrderRepository.STATUS_DRAFT
        + ","
        + PurchaseOrderRepository.STATUS_REQUESTED
        + ")";
  }

  @Override
  public String getLastPurchaseOrderOfSupplier(User user) {
    return "self.supplierPartner.id = "
        + user.getPartner().getId()
        + " AND self.statusSelect = "
        + PurchaseOrderRepository.STATUS_FINISHED
        + " GROUP BY self.validationDateTime,self.id"
        + " ORDER BY self.validationDateTime DESC";
  }

  /* StockMove Query */
  @Override
  public String getLastDeliveryOfSupplier(User user) {
    return "self.partner.id = "
        + user.getPartner().getId()
        + " AND self.typeSelect = "
        + StockMoveRepository.TYPE_INCOMING
        + " AND self.statusSelect = "
        + StockMoveRepository.STATUS_REALIZED
        + " ORDER BY self.realDate DESC";
  }

  @Override
  public String getNextDeliveryOfSupplier(User user) {
    return "self.partner.id = "
        + user.getPartner().getId()
        + " AND self.typeSelect = "
        + StockMoveRepository.TYPE_INCOMING
        + " AND self.statusSelect = "
        + StockMoveRepository.STATUS_PLANNED
        + " ORDER BY self.estimatedDate ASC";
  }

  @Override
  public String getDeliveriesToPrepareOfSupplier(User user) {
    return "self.partner.id = "
        + user.getPartner().getId()
        + " AND self.typeSelect = "
        + StockMoveRepository.TYPE_INCOMING
        + " AND self.statusSelect IN ("
        + StockMoveRepository.STATUS_AVAILABLE
        + ", "
        + StockMoveRepository.STATUS_DRAFT
        + ", "
        + StockMoveRepository.STATUS_PARTIALLY_AVAILABLE
        + ")";
  }

  /* Invoice Query */
  @Override
  public String getOverdueInvoicesOfSupplier(User user) {
    return "self.partner.id = " + user.getPartner().getId() + " AND self.dueDate < current_date()";
  }

  @Override
  public String getAwaitingInvoicesOfSupplier(User user) {
    return "self.partner.id = "
        + user.getPartner().getId()
        + " AND self.dueDate < current_date() "
        + " AND self.amountRemaining != 0 AND self.statusSelect != "
        + InvoiceRepository.STATUS_CANCELED;
  }

  @Override
  public String getTotalRemainingOfSupplier(User user) {
    return "self.partner.id = "
        + user.getPartner().getId()
        + " AND self.amountRemaining != 0 AND self.statusSelect != "
        + InvoiceRepository.STATUS_CANCELED;
  }

  protected <T extends Model> long getCount(Class<T> klass, String query) {
    return JPA.all(klass).filter(query).count();
  }

  protected <T extends Model> T getData(Class<T> klass, String query) {
    return JPA.all(klass).filter(query).fetchOne();
  }
}
