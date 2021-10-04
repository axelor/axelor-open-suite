package com.axelor.apps.gst.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceGstServiceImpl extends InvoiceServiceImpl {

  @Inject
  public InvoiceGstServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService,
        moveToolService);
  }

  @Inject InvoiceLineGstServiceImpl line;

  @Override
  public Invoice compute(final Invoice invoice) throws AxelorException {
    super.compute(invoice);
    BigDecimal netAmount = BigDecimal.ZERO;
    BigDecimal netIgst = BigDecimal.ZERO;
    BigDecimal netSgst = BigDecimal.ZERO;
    BigDecimal netCgst = BigDecimal.ZERO;
    BigDecimal gross = BigDecimal.ZERO;

    if (ObjectUtils.isEmpty(invoice.getInvoiceLineList())) {
      return invoice;
    }
    if (!ObjectUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine item : invoice.getInvoiceLineList()) {
        item = line.calculateInvoiceLine(item, line.checkIsStateDiff(invoice));

        netAmount = netAmount.add(item.getExTaxTotal());
        netIgst = netIgst.add(item.getIgst());

        netSgst = netSgst.add(item.getSgst());

        netCgst = netCgst.add(item.getCgst());

        gross = gross.add(item.getInTaxTotal());

        invoice.setExTaxTotal(netAmount.setScale(4, RoundingMode.HALF_UP));
        invoice.setNetIgst(netIgst.setScale(4, RoundingMode.HALF_UP));
        invoice.setNetSgst(netSgst.setScale(4, RoundingMode.HALF_UP));
        invoice.setNetCgst(netCgst.setScale(4, RoundingMode.HALF_UP));
        invoice.setInTaxTotal(gross.setScale(4, RoundingMode.HALF_UP));
      }
    }
    invoice.setInvoiceLineList(invoice.getInvoiceLineList());
    return invoice;
  }
}
