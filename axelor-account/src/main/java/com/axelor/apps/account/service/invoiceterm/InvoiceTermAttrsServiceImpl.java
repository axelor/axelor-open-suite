package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class InvoiceTermAttrsServiceImpl implements InvoiceTermAttrsService {

  @Inject
  public void InvoiceTermAttrsServiceImpl() {}

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void hideActionAndPfpPanel(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    boolean getHidden = getActionPanelHidden(invoiceTerm);

    this.addAttr("actionPanel", "hidden", getHidden, attrsMap);
    this.addAttr("pfpPanel", "hidden", getHidden, attrsMap);
  }

  protected boolean getActionPanelHidden(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();
      return invoiceTerm.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_NO_PFP
          || !(new ArrayList<>(
                  Arrays.asList(
                      InvoiceRepository.STATUS_VALIDATED,
                      InvoiceRepository.STATUS_VENTILATED,
                      InvoiceRepository.STATUS_CANCELED))
              .contains(invoice.getStatusSelect()))
          || (invoice.getOriginDate() == null
              && new ArrayList<>(
                      Arrays.asList(
                          InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
                          InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND))
                  .contains(invoice.getOperationTypeSelect())
              && (new ArrayList<>(
                          Arrays.asList(
                              InvoiceRepository.STATUS_DRAFT, InvoiceRepository.STATUS_VALIDATED))
                      .contains(invoice.getStatusSelect())
                  || invoice.getOperationSubTypeSelect()
                      == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE));
    }
    Move move =
        Optional.of(invoiceTerm).map(InvoiceTerm::getMoveLine).map(MoveLine::getMove).orElse(null);
    if (move != null) {
      return invoiceTerm.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_NO_PFP
          || !(new ArrayList<>(
                  Arrays.asList(
                      MoveRepository.STATUS_DAYBOOK,
                      MoveRepository.STATUS_ACCOUNTED,
                      MoveRepository.STATUS_CANCELED))
              .contains(move.getStatusSelect()));
    }
    return true;
  }

  @Override
  public void changeAmountsTitle(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    Invoice invoice = invoiceTerm.getInvoice();
    Move move =
        Optional.ofNullable(invoiceTerm)
            .map(InvoiceTerm::getMoveLine)
            .map(MoveLine::getMove)
            .orElse(null);

    if (invoice != null
            && !Objects.equals(invoice.getCurrency(), invoice.getCompany().getCurrency())
        || (invoice == null
            && move != null
            && move.getCompany() != null
            && !Objects.equals(move.getCurrency(), move.getCompany().getCurrency()))) {

      this.addAttr("amount", "title", I18n.get("Amount in currency"), attrsMap);
      this.addAttr("amountRemaining", "title", I18n.get("Amount remaining in currency"), attrsMap);
    }
  }
}
