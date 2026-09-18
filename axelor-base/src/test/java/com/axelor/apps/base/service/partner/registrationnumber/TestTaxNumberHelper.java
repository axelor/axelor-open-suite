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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TestTaxNumberHelper {

  @ParameterizedTest
  @CsvSource({
    "479921561, 00",
    "317145894, 02",
    "420244931, 03",
    "789472586, 04",
    "435089263, 08",
    "063201404, 70",
    "025780396, 93",
    "552100554, 96"
  })
  void testGetTaxKeyFromSiren(String siren, String expectedKey) {
    assertEquals(expectedKey, TaxNumberHelper.getTaxKeyFromSiren(siren));
  }

  @ParameterizedTest
  @CsvSource({
    "479921561, FR00479921561",
    "317145894, FR02317145894",
    "420244931, FR03420244931",
    "789472586, FR04789472586",
    "435089263, FR08435089263",
    "063201404, FR70063201404",
    "025780396, FR93025780396",
    "552100554, FR96552100554"
  })
  void testGetTaxNbrFromSiren(String siren, String expectedTaxNbr) {
    String taxNbr = TaxNumberHelper.getTaxNbrFromSiren("FR", siren);
    assertEquals(expectedTaxNbr, taxNbr);
    assertEquals(13, taxNbr.length());
  }
}
