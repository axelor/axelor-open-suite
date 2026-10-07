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

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Filter;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFilterTargetServiceImpl {

  private MetaModelRepository metaModelRepository;
  private Query<MetaField> metaFieldQuery;
  private FilterTargetService filterTargetService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    metaModelRepository = mock(MetaModelRepository.class);
    MetaFieldRepository metaFieldRepository = mock(MetaFieldRepository.class);
    metaFieldQuery = mock(Query.class, RETURNS_SELF);
    when(metaFieldRepository.all()).thenReturn(metaFieldQuery);
    filterTargetService = new FilterTargetServiceImpl(metaModelRepository, metaFieldRepository);
  }

  @Test
  void testNoTargetField() {
    Filter filter = createFilter(createField("name", "String", null), null);

    Assertions.assertNull(filterTargetService.getTargetType(filter));
  }

  @Test
  void testSimpleFieldReturnsItsTypeName() {
    Filter filter = createFilter(createField("name", "String", null), "name");

    Assertions.assertEquals("String", filterTargetService.getTargetType(filter));
  }

  @Test
  void testRelationalFieldReturnsItsRelationship() {
    Filter filter = createFilter(createField("user", "User", "ManyToOne"), "user");

    Assertions.assertEquals("ManyToOne", filterTargetService.getTargetType(filter));
  }

  @Test
  void testPathIsFollowedToTheTargetedField() {
    when(metaModelRepository.findByName("User")).thenReturn(new MetaModel());
    when(metaFieldQuery.fetchOne()).thenReturn(createField("code", "String", null));
    Filter filter = createFilter(createField("user", "User", "ManyToOne"), "user.code");

    Assertions.assertEquals("String", filterTargetService.getTargetType(filter));
  }

  @Test
  void testUnknownSubFieldReturnsNull() {
    when(metaModelRepository.findByName("User")).thenReturn(new MetaModel());
    when(metaFieldQuery.fetchOne()).thenReturn(null);
    Filter filter = createFilter(createField("user", "User", "ManyToOne"), "user.unknown");

    Assertions.assertNull(filterTargetService.getTargetType(filter));
  }

  private MetaField createField(String name, String typeName, String relationship) {
    MetaField field = new MetaField();
    field.setName(name);
    field.setTypeName(typeName);
    field.setRelationship(relationship);
    return field;
  }

  private Filter createFilter(MetaField metaField, String targetField) {
    Filter filter = new Filter();
    filter.setMetaField(metaField);
    filter.setTargetField(targetField);
    return filter;
  }
}
