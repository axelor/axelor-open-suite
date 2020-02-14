/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder.file.directdebit;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderFileService;

public abstract class BankOrderFile008Service extends BankOrderFileService {

  public static final String SEPA_TYPE_CORE = "CORE";
  public static final String SEPA_TYPE_SBB = "SBB";
  protected static final String BIC_NOT_PROVIDED = "NOTPROVIDED";
  protected static final String CURRENCY_CODE = "EUR";

  public BankOrderFile008Service(BankOrder bankOrder) {
    super(bankOrder);

    fileExtension = FILE_EXTENSION_XML;
  }
}
