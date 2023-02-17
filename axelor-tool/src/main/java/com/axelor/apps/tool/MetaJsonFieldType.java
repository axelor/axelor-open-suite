/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

/**
 * List the expected values in the field {@link com.axelor.meta.db.MetaJsonField#type}, i.e. the
 * ones provided by Axelor Open Platform selection <i>json.field.type</i>.
 */
public final class MetaJsonFieldType {

  private MetaJsonFieldType() {
    throw new IllegalStateException("Utility class");
  }

  public static final String STRING = "string";
  public static final String INTEGER = "integer";
  public static final String LONG = "long";
  public static final String DECIMAL = "decimal";
  public static final String BOOLEAN = "boolean";
  public static final String DATETIME = "datetime";
  public static final String DATE = "date";
  public static final String TIME = "time";

  public static final String MANY_TO_MANY = "many-to-many";
  public static final String ONE_TO_MANY = "one-to-many";
  public static final String MANY_TO_ONE = "many-to-one";
  public static final String ONE_TO_ONE = "one-to-one";

  public static final String JSON_MANY_TO_ONE = "json-many-to-one";
  public static final String JSON_MANY_TO_MANY = "json-many-to-many";
  public static final String JSON_ONE_TO_MANY = "json-one-to-many";

  public static final String PANEL = "panel";
  public static final String ENUM = "enum";
  public static final String BUTTON = "button";
  public static final String SEPARATOR = "separator";
}
