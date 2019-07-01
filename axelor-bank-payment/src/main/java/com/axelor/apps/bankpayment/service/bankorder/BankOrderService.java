/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public interface BankOrderService {

  public BigDecimal computeBankOrderTotalAmount(BankOrder bankOrder) throws AxelorException;

  public BigDecimal computeCompanyCurrencyTotalAmount(BankOrder bankOrder) throws AxelorException;

  public void updateTotalAmounts(BankOrder bankOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void confirm(BankOrder bankOrder)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException;

  @Transactional(rollbackOn = {Exception.class})
  public void sign(BankOrder bankOrder);

  @Transactional(rollbackOn = {Exception.class})
  public void validate(BankOrder bankOrder)
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;

  public void realize(BankOrder bankOrder) throws AxelorException;

  public File generateFile(BankOrder bankOrder)
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException;

  @Transactional(rollbackOn = {Exception.class})
  public BankOrder generateSequence(BankOrder bankOrder) throws AxelorException;

  public void setSequenceOnBankOrderLines(BankOrder bankOrder);

  public void checkLines(BankOrder bankOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void validatePayment(BankOrder bankOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void cancelPayment(BankOrder bankOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void cancelBankOrder(BankOrder bankOrder) throws AxelorException;

  @Transactional
  public EbicsUser getDefaultEbicsUserFromBankDetails(BankDetails bankDetails);

  public String createDomainForBankDetails(BankOrder bankOrder);

  public BankDetails getDefaultBankDetails(BankOrder bankOrder);

  public void checkBankDetails(BankDetails bankDetails, BankOrder bankOrder) throws AxelorException;

  public boolean checkBankDetailsTypeCompatible(
      BankDetails bankDetails, BankOrderFileFormat bankOrderFileFormat);

  public boolean checkBankDetailsCurrencyCompatible(BankDetails bankDetails, BankOrder bankOrder);

  public void resetReceivers(BankOrder bankOrder);

  public ActionViewBuilder buildBankOrderLineView(
      String gridViewName, String formViewName, String viewDomain);
}
