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
package com.axelor.apps.base.service.administration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class IncrementResultTest {

  @Test
  void testConstructorAndGetters() {
    Long versionId = 123L;
    Long nextNum = 456L;

    IncrementResult result = new IncrementResult(versionId, nextNum);

    assertEquals(versionId, result.getSequenceVersionId());
    assertEquals(nextNum, result.getNextNum());
  }

  @Test
  void testEquality() {
    IncrementResult result1 = new IncrementResult(1L, 100L);
    IncrementResult result2 = new IncrementResult(1L, 100L);
    IncrementResult result3 = new IncrementResult(2L, 100L);
    IncrementResult result4 = new IncrementResult(1L, 200L);

    assertEquals(result1, result2);
    assertNotEquals(result1, result3);
    assertNotEquals(result1, result4);
  }

  @Test
  void testHashCode() {
    IncrementResult result1 = new IncrementResult(1L, 100L);
    IncrementResult result2 = new IncrementResult(1L, 100L);

    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void testToString() {
    IncrementResult result = new IncrementResult(123L, 456L);

    String str = result.toString();

    // Verify toString contains the important values
    assertEquals("IncrementResult{sequenceVersionId=123, nextNum=456}", str);
  }

  @Test
  void testNullValues() {
    IncrementResult result = new IncrementResult(null, null);

    assertEquals(null, result.getSequenceVersionId());
    assertEquals(null, result.getNextNum());
  }
}
