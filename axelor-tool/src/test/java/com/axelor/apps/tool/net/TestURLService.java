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
package com.axelor.apps.tool.net;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import org.junit.Assert;
import org.junit.Test;

public class TestURLService {

  @Test
  public void testNotExist() {

    Assert.assertNull(URLService.notExist("http://www.google.com"));

    String url = "www.google.com";
    Assert.assertEquals(
        String.format(I18n.get(IExceptionMessage.URL_SERVICE_2), url), URLService.notExist(url));

    url = "http://www.testtrgfgfdg.com/";
    Assert.assertEquals(
        String.format(I18n.get(IExceptionMessage.URL_SERVICE_3), url), URLService.notExist(url));
  }
}
