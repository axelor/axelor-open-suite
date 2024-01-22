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
package com.axelor.apps.bankpayment.test;

import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.base.AxelorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestEbicsOrderNumber {

  private static EbicsUserService ebicsUserService;

  @BeforeAll
  static void prepare() {
    ebicsUserService = new EbicsUserService();
  }

  @Test
  void validateOrderId() {
    try {
      ebicsUserService.getNextOrderNumber("01.AC");
    } catch (AxelorException e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  void testPossibleOrderNumbers() {
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
      Assertions.assertEquals((36 * 36 * 36), i);
    }
  }
}
