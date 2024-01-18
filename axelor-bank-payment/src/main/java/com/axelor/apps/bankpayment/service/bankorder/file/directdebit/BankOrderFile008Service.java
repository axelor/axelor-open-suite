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
