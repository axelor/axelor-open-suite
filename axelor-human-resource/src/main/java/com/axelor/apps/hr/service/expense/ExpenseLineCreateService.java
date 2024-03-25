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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.project.db.Project;
import com.axelor.meta.db.MetaFile;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseLineCreateService {

  ExpenseLine createGeneralExpenseLine(
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException;

  ExpenseLine createKilometricExpenseLine(
      Project project,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      String comments,
      Employee employee,
      Company company,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException;
}
