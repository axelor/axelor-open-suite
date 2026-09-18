/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.partner.registrationnumber;

public class TaxNumberHelper {

  private TaxNumberHelper() {}

  public static String getTaxKeyFromSiren(String siren) {
    int taxKey = Math.floorMod(Integer.parseInt(siren), 97);
    taxKey = Math.floorMod(12 + 3 * taxKey, 97);
    return String.format("%02d", taxKey);
  }

  public static String getTaxNbrFromSiren(String countryCode, String siren) {
    return String.format("%s%s%s", countryCode, getTaxKeyFromSiren(siren), siren);
  }
}
