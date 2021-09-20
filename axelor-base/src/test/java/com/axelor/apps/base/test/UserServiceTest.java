/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

  private static UserService userService;

  @BeforeAll
  public static void setUp() {
    userService = mock(UserServiceImpl.class);
    doCallRealMethod().when(userService).matchPasswordPattern(any());
  }

  @Test
  public void testMatchPasswordPatternUpperLowerDigit() {
    Assertions.assertTrue(userService.matchPasswordPattern("Axelor123"));
    Assertions.assertTrue(userService.matchPasswordPattern("123Axelor"));
    Assertions.assertTrue(userService.matchPasswordPattern("axelor123A"));
  }

  @Test
  public void testMatchPasswordPatternUpperLowerSpecial() {
    Assertions.assertTrue(userService.matchPasswordPattern("Axelor=["));
    Assertions.assertTrue(userService.matchPasswordPattern("]-Axelor"));
    Assertions.assertTrue(userService.matchPasswordPattern("axelor\"A"));
  }

  @Test
  public void testMatchPasswordPatternLowerSpecialDigit() {
    Assertions.assertTrue(userService.matchPasswordPattern(";axelor12"));
    Assertions.assertTrue(userService.matchPasswordPattern("axelor12?"));
    Assertions.assertTrue(userService.matchPasswordPattern("axelor123A"));
  }

  @Test
  public void testMatchPasswordPatternUpperSpecialDigit() {
    Assertions.assertTrue(userService.matchPasswordPattern("AXELOR12!"));
    Assertions.assertTrue(userService.matchPasswordPattern("123!AXELOR"));
    Assertions.assertTrue(userService.matchPasswordPattern(";XELOR123"));
  }

  @Test
  public void testMatchPasswordPatternUpperLowerSpecialDigit() {
    Assertions.assertTrue(userService.matchPasswordPattern("Axelor!12"));
    Assertions.assertTrue(userService.matchPasswordPattern("123Axe+lor"));
    Assertions.assertTrue(userService.matchPasswordPattern("ax[elor123A"));
  }

  @Test
  public void testNotMatchPasswordPattern() {
    Assertions.assertFalse(userService.matchPasswordPattern("Xlr1!2*"));
    Assertions.assertFalse(userService.matchPasswordPattern("AxelorAxelor"));
    Assertions.assertFalse(userService.matchPasswordPattern("axelor123456"));
  }
}
