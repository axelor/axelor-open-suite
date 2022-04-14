package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Product;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface CommonInvoiceService {

  /**
   * This method return amount% of total if isPercent and amount if not.
   *
   * @param model : model in process
   * @param amount
   * @param isPercent
   * @param total
   * @throws AxelorException if there is a inconsistency with amount and total. (For example if
   *     total is 0 and amount is not)
   * @return amount% of total if isPercent and amount if not
   */
  BigDecimal computeAmountToInvoicePercent(
      Model model, BigDecimal amount, boolean isPercent, BigDecimal total) throws AxelorException;

  /**
   * This method will create ONLY ONE line of invoiceLine (using invoicingProduct). It is used when
   * creating a advance payment or supplier advance payment where there is only one line generated.
   *
   * @param invoice
   * @param inTaxTotal of the Order
   * @param invoicingProduct
   * @param percentToInvoice
   * @return
   * @throws AxelorException
   */
  List<InvoiceLine> createInvoiceLinesFromOrder(
      Invoice invoice, BigDecimal inTaxTotal, Product invoicingProduct, BigDecimal percentToInvoice)
      throws AxelorException;

  BigDecimal computeSumInvoices(List<Invoice> invoices);
}
