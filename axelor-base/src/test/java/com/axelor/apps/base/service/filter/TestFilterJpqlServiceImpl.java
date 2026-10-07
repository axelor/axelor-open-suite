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
package com.axelor.apps.base.service.filter;

import com.axelor.apps.base.db.Filter;
import com.axelor.meta.db.MetaField;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestFilterJpqlServiceImpl {

  private final FilterJpqlService filterJpqlService = new FilterJpqlServiceImpl();

  @Test
  void testNoFilter() {
    Assertions.assertNull(filterJpqlService.getJpqlFilters(null));
    Assertions.assertNull(filterJpqlService.getJpqlFilters(List.of()));
  }

  @Test
  void testStringFilterIsCaseInsensitiveLike() {
    Filter filter = createFilter("name", "String", "like", "Axelor");

    Assertions.assertEquals(
        "LOWER(self.name) LIKE LOWER('%Axelor%')",
        filterJpqlService.getJpqlFilters(List.of(filter)));
  }

  @Test
  void testFiltersAreCombinedWithTheirLogicOperator() {
    Filter first = createFilter("isCustomer", "Boolean", "isTrue", null);
    Filter second = createFilter("isProspect", "Boolean", "isTrue", null);
    second.setLogicOp(1);
    Filter third = createFilter("name", "String", "=", "x");
    third.setLogicOp(0);

    Assertions.assertEquals(
        "self.isCustomer IS TRUE  OR self.isProspect IS TRUE  AND LOWER(self.name) = LOWER('x')",
        filterJpqlService.getJpqlFilters(List.of(first, second, third)));
  }

  @Test
  void testRelationalFilterUsesTargetField() {
    Filter filter = createFilter("partnerCategory", "Long", "=", "3");
    filter.getMetaField().setRelationship("ManyToOne");
    filter.setTargetField("partnerCategory.id");

    Assertions.assertEquals(
        "self.partnerCategory.id = '3'", filterJpqlService.getJpqlFilters(List.of(filter)));
  }

  private Filter createFilter(String fieldName, String targetType, String operator, String value) {
    MetaField metaField = new MetaField();
    metaField.setName(fieldName);
    Filter filter = new Filter();
    filter.setMetaField(metaField);
    filter.setTargetType(targetType);
    filter.setOperator(operator);
    filter.setValue(value);
    return filter;
  }
}
