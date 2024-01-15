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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.TimetableTemplate;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TimetableService {

  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(Timetable timetable) throws AxelorException;

  public Invoice createInvoice(Timetable timetable) throws AxelorException;

  public List<Timetable> applyTemplate(
      TimetableTemplate template, BigDecimal exTaxTotal, LocalDate computationDate)
      throws AxelorException;
}
