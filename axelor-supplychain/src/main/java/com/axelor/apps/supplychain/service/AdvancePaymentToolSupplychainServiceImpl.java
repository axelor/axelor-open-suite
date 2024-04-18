package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.AdvancePaymentToolServiceImpl;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdvancePaymentToolSupplychainServiceImpl extends AdvancePaymentToolServiceImpl {

  @Inject
  public AdvancePaymentToolSupplychainServiceImpl(
      AppAccountService appAccountService, MoveToolService moveToolService) {
    super(appAccountService, moveToolService);
  }

  @Override
  public List<MoveLine> getMoveLinesFromSOAdvancePayments(Invoice invoice) {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getMoveLinesFromSOAdvancePayments(invoice);
    }

    // search sale order in the invoice
    SaleOrder saleOrder = invoice.getSaleOrder();
    // search sale order in invoice lines
    List<SaleOrder> saleOrderList =
        invoice.getInvoiceLineList().stream()
            .map(invoiceLine -> invoice.getSaleOrder())
            .collect(Collectors.toList());

    saleOrderList.add(saleOrder);

    // remove null value and duplicates
    saleOrderList =
        saleOrderList.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

    if (saleOrderList.isEmpty()) {
      return new ArrayList<>();
    } else {
      // get move lines from sale order
      return saleOrderList.stream()
          .flatMap(saleOrder1 -> saleOrder1.getAdvancePaymentList().stream())
          .filter(Objects::nonNull)
          .distinct()
          .map(AdvancePayment::getMove)
          .filter(Objects::nonNull)
          .distinct()
          .flatMap(move -> moveToolService.getToReconcileCreditMoveLines(move).stream())
          .collect(Collectors.toList());
    }
  }
}
