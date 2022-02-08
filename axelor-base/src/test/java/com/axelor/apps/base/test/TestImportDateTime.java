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

import java.time.LocalDateTime;
import org.junit.Test;

public class TestImportDateTime {
  @Test
  public void testDateTimeImport() {
    LocalDateTime dt = LocalDateTime.now();
    LocalDateTime dt2 = dt.plusDays(2);

    System.out.println(dt);
    System.out.println(dt2);
  }
}
