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
package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreateAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.StructuredContentLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStatementLineCreateCAMT53Service extends BankStatementLineCreateAbstractService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  //    protected BankStatementLineCreationAFB120Service bankStatementLineCreationAFB120Service;
  //    protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected CurrencyRepository currencyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected InterbankCodeLineRepository interbankCodeLineRepository;
  //    protected BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service;

  @Inject
  public BankStatementLineCreateCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService,
      //      BankStatementLineCreationAFB120Service bankStatementLineCreationAFB120Service,
      //      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      CurrencyRepository currencyRepository,
      BankDetailsRepository bankDetailsRepository,
      InterbankCodeLineRepository interbankCodeLineRepository)
        //          , BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service)
      {
    super(bankStatementRepository, bankStatementService);
    //    this.bankStatementLineCreationAFB120Service = bankStatementLineCreationAFB120Service;
    //    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.currencyRepository = currencyRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.interbankCodeLineRepository = interbankCodeLineRepository;
    //    this.bankStatementLineMapperAFB120Service = bankStatementLineMapperAFB120Service;
  }

  @Override
  public void process(BankStatement bankStatement) throws IOException, AxelorException {
    setBankStatement(bankStatement);
    // read file, save into List<StructuredContentLine>
  }

  @Override
  protected List<StructuredContentLine> readFile() throws IOException, AxelorException {

    return null;
  }

  @Override
  protected BankStatementLine createBankStatementLine(
      StructuredContentLine structuredContentLine, int sequence) {
    return null;
  }
}
