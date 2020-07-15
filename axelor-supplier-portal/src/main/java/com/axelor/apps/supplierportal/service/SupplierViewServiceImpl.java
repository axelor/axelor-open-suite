package com.axelor.apps.supplierportal.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SupplierViewServiceImpl implements SupplierViewService {
  protected static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @Override
  public User getSupplierUser() {
    return Beans.get(UserService.class).getUser();
  }

  @Override
  public Map<String, Object> updateSupplierViewIndicators() {
    Map<String, Object> map = new HashMap<>();
    /* PurchaseOrder */
    map.put("$orders", 2);
    map.put("$quotationInProgress", 4);
    map.put("$lastOrder", "19/02/2019");

    /* StockMove */
    map.put("$lastDelivery", "15/01/2019");
    map.put("$nextDelivery", "28/02/2019");
    map.put("$deliveriesToPrepare", 4);

    /* Invoice */
    map.put("$overdueInvoices", 1);
    map.put("$awaitingInvoices", 1);
    map.put("$totalRemaining", 157);

    /* Helpdesk */
    map.put("$supplierTickets", 2);
    map.put("$companyTickets", 105);
    map.put("$resolvedTickets", 84);

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
        + " ORDER BY self.validationDate DESC";
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
}
