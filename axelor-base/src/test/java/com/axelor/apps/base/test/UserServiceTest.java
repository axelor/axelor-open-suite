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
package com.axelor.apps.base.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserServiceTest {

  private UserService userService;

  @Before
  public void setUp() {
    userService = mock(UserServiceImpl.class);
    doCallRealMethod().when(userService).matchPasswordPattern(any());
  }

  @Test
  public void testMatchPasswordPatternUpperLowerDigit() {
    Assert.assertTrue(userService.matchPasswordPattern("Axelor123"));
    Assert.assertTrue(userService.matchPasswordPattern("123Axelor"));
    Assert.assertTrue(userService.matchPasswordPattern("axelor123A"));
  }

  @Test
  public void testMatchPasswordPatternUpperLowerSpecial() {
    Assert.assertTrue(userService.matchPasswordPattern("Axelor=["));
    Assert.assertTrue(userService.matchPasswordPattern("]-Axelor"));
    Assert.assertTrue(userService.matchPasswordPattern("axelor\"A"));
  }

  @Test
  public void testMatchPasswordPatternLowerSpecialDigit() {
    Assert.assertTrue(userService.matchPasswordPattern(";axelor12"));
    Assert.assertTrue(userService.matchPasswordPattern("axelor12?"));
    Assert.assertTrue(userService.matchPasswordPattern("axelor123A"));
  }

  @Test
  public void testMatchPasswordPatternUpperSpecialDigit() {
    Assert.assertTrue(userService.matchPasswordPattern("AXELOR12!"));
    Assert.assertTrue(userService.matchPasswordPattern("123!AXELOR"));
    Assert.assertTrue(userService.matchPasswordPattern(";XELOR123"));
  }

  @Test
  public void testMatchPasswordPatternUpperLowerSpecialDigit() {
    Assert.assertTrue(userService.matchPasswordPattern("Axelor!12"));
    Assert.assertTrue(userService.matchPasswordPattern("123Axe+lor"));
    Assert.assertTrue(userService.matchPasswordPattern("ax[elor123A"));
  }

  @Test
  public void testNotMatchPasswordPattern() {
    Assert.assertFalse(userService.matchPasswordPattern("Xlr1!2*"));
    Assert.assertFalse(userService.matchPasswordPattern("AxelorAxelor"));
    Assert.assertFalse(userService.matchPasswordPattern("axelor123456"));
  }
}
