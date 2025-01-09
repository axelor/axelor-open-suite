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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import javax.xml.datatype.DatatypeConfigurationException;

public interface BankOrderService {

  public BigDecimal computeBankOrderTotalAmount(BankOrder bankOrder) throws AxelorException;

  public BigDecimal computeCompanyCurrencyTotalAmount(BankOrder bankOrder) throws AxelorException;

  public void updateTotalAmounts(BankOrder bankOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void sign(BankOrder bankOrder);

  public void validate(BankOrder bankOrder)
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;

  public File generateFile(BankOrder bankOrder)
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;

  public BankOrder generateSequence(BankOrder bankOrder) throws AxelorException;

  public void setSequenceOnBankOrderLines(BankOrder bankOrder);

  public String createDomainForBankDetails(BankOrder bankOrder);

  public void resetReceivers(BankOrder bankOrder);

  public ActionViewBuilder buildBankOrderLineView(
      String gridViewName, String formViewName, String viewDomain);

  public void setStatusToDraft(BankOrder bankOrder);

  void processBankOrderStatus(BankOrder bankOrder, PaymentMode paymentMode) throws AxelorException;

  void setNbOfLines(BankOrder bankOrder);
}
