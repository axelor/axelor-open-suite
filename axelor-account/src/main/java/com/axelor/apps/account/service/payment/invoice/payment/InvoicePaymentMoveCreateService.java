package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.base.AxelorException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.datatype.DatatypeConfigurationException;

public interface InvoicePaymentMoveCreateService {

  InvoicePayment createMoveForInvoicePayment(InvoicePayment invoicePayment) throws AxelorException;

  void createInvoicePaymentMove(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException;
}
