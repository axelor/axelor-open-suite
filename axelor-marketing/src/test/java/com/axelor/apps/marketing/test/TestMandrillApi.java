/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillSender;
import com.microtripit.mandrillapp.lutung.view.MandrillUserInfo;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public class TestMandrillApi {

  @Test
  public void testUsersCall() throws MandrillApiError, IOException {
    MandrillApi mandrillApi = new MandrillApi("ogM4Om9GhLWKEy_G1u8t-Q");
    MandrillUserInfo user = mandrillApi.users().info();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    System.out.println(gson.toJson(user));
    System.out.println(mandrillApi.users().ping());
    MandrillSender[] senders = mandrillApi.users().senders();
    System.out.println("Senders :: " + Arrays.asList(senders));
  }
}
