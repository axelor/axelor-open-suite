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
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginServiceImpl;
import com.axelor.apps.hr.db.Expense;
import com.axelor.db.Model;
import com.google.inject.Inject;

public class BankOrderLineOriginServiceHRImpl extends BankOrderLineOriginServiceImpl {

  @Inject
  public BankOrderLineOriginServiceHRImpl(
      BankOrderLineOriginRepository bankOrderLineOriginRepository) {
    super(bankOrderLineOriginRepository);
  }

  @Override
  protected String computeRelatedToSelectName(Model model) {

    if (model instanceof Expense) {

      return ((Expense) model).getExpenseSeq();

    } else {

      return super.computeRelatedToSelectName(model);
    }
  }
}
