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
package com.axelor.apps.account.util;

import com.axelor.apps.account.db.MoveLine;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineUtilsServiceImpl implements MoveLineUtilsService {

  protected InvoiceTermUtilsService invoiceTermUtilsService;

  @Inject
  public MoveLineUtilsServiceImpl(InvoiceTermUtilsService invoiceTermUtilsService) {
    this.invoiceTermUtilsService = invoiceTermUtilsService;
  }

  @Override
  public void updateInvoiceTermsParentFields(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine
          .getInvoiceTermList()
          .forEach(
              it ->
                  invoiceTermUtilsService.setParentFields(
                      it, moveLine.getMove(), moveLine, it.getInvoice()));
    }
  }
}
