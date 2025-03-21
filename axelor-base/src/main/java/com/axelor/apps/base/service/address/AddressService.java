/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import wslite.json.JSONException;

public interface AddressService {

  public boolean check(String wsdlUrl);

  public Map<String, Object> validate(String wsdlUrl, String search);

  public com.qas.web_2005_02.Address select(String wsdlUrl, String moniker);

  @CallMethod
  public boolean checkAddressUsed(Long addressId);

  /**
   * Get or update latitude and longitude.
   *
   * @param address
   * @return
   * @throws JSONException
   * @throws AxelorException
   */
  Optional<Pair<BigDecimal, BigDecimal>> getOrUpdateLatLong(Address address)
      throws AxelorException, JSONException;

  /**
   * Update latitude and longitude.
   *
   * @param address
   * @throws AxelorException
   * @throws JSONException
   */
  Optional<Pair<BigDecimal, BigDecimal>> updateLatLong(Address address)
      throws AxelorException, JSONException;

  /**
   * Reset latitude and longitude.
   *
   * @param address
   */
  void resetLatLong(Address address);

  public String computeFullName(Address address);

  /**
   * Used to fill the string field in invoice, sale/purchase order and stock move
   *
   * @param address
   * @return the string field corresponding to the given address.
   */
  @CallMethod
  String computeAddressStr(Address address);
}
