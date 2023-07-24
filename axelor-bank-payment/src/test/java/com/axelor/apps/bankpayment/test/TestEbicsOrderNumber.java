/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.test;

import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.base.AxelorException;
import org.junit.Assert;
import org.junit.Test;

public class TestEbicsOrderNumber {

  @Test
  public void validateOrderId() {
    EbicsUserService ebicsUserService = new EbicsUserService();
    try {
      ebicsUserService.getNextOrderNumber("01.AC");
    } catch (AxelorException e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void testPossibleOrderNumbers() {
    EbicsUserService ebicsUserService = new EbicsUserService();
    int i = 0;
    String orderNo = "A000";
    try {
      while (true) {
        i++;
        orderNo = ebicsUserService.getNextOrderNumber(orderNo);
      }
    } catch (AxelorException e) {
      System.out.println(e.getMessage());
      System.out.println("Maximum possible order numbers: " + i);
      Assert.assertTrue(i == (36 * 36 * 36));
    }
  }
}
