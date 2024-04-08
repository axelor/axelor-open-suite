package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.datatype.DatatypeConfigurationException;

public interface BankOrderValidationService {

  void validateFromBankOrder(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void realize(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void validatePayment(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void confirm(BankOrder bankOrder)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException;
}
