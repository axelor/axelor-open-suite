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
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginServiceImpl;
import com.axelor.apps.hr.db.Expense;
import com.axelor.db.Model;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.google.inject.Inject;
import java.time.LocalDate;

public class BankOrderLineOriginServiceHRImpl extends BankOrderLineOriginServiceImpl {

  @Inject
  public BankOrderLineOriginServiceHRImpl(
      BankOrderLineOriginRepository bankOrderLineOriginRepository,
      InvoiceTermRepository invoiceTermRepository,
      InvoiceRepository invoiceRepository,
      DMSFileRepository dmsFileRepository) {
    super(
        bankOrderLineOriginRepository, invoiceTermRepository, invoiceRepository, dmsFileRepository);
  }

  @Override
  protected String computeRelatedToSelectName(Model model) {

    if (model instanceof Expense) {

      return ((Expense) model).getExpenseSeq();

    } else {

      return super.computeRelatedToSelectName(model);
    }
  }

  @Override
  protected LocalDate computeRelatedToSelectDate(Model model) {

    if (model instanceof Expense) {

      return ((Expense) model).getValidationDateTime().toLocalDate();

    } else {

      return super.computeRelatedToSelectDate(model);
    }
  }

  @Override
  protected LocalDate computeRelatedToSelectDueDate(Model model) {

    if (model instanceof Expense) {

      return ((Expense) model).getValidationDateTime().toLocalDate();

    } else {

      return super.computeRelatedToSelectDueDate(model);
    }
  }
}
