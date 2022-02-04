/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;

public interface LunchVoucherMgtService {

  @Transactional(rollbackOn = {Exception.class})
  public void calculate(LunchVoucherMgt lunchVoucherMgt) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void validate(LunchVoucherMgt lunchVoucherMgt) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public int updateStock(
      List<LunchVoucherMgtLine> newLunchVoucherMgtLines,
      List<LunchVoucherMgtLine> oldLunchVoucherMgtLines,
      Company company)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void export(LunchVoucherMgt lunchVoucherMgt) throws IOException;

  public int checkStock(Company company, int numberToUse) throws AxelorException;

  public void calculateTotal(LunchVoucherMgt lunchVoucherMgt);
}
