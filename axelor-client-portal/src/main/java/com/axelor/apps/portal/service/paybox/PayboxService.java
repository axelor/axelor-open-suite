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
package com.axelor.apps.portal.service.paybox;

import com.axelor.exception.AxelorException;
import javax.ws.rs.core.MultivaluedMap;

public interface PayboxService {

  /**
   * Builds a full paybox access url.
   *
   * @param amountInCents the amount to be paid in cents.
   * @param orderReference the order reference number.
   * @param email user mail
   * @param successURL url to redirect on success * @param failureURL url to redirect on failure
   * @return the full url
   */
  public String buildUrl(
      Long amountInCents,
      String orderReference,
      String email,
      String successURL,
      String failureURL);

  /**
   * Check error codes in paybox response.
   *
   * @param amountInCents the amount to be paid in cents.
   */
  public void checkError(String errorCode) throws AxelorException;

  /**
   * Check signature by taking a full queryString params and analyzing it.
   *
   * @param params query params map
   * @return true if successful, false if not.
   */
  public boolean checkSignature(MultivaluedMap<String, String> params);
}
