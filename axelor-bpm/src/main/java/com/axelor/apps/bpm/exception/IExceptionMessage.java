/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.exception;

public interface IExceptionMessage {

  public static final String MISSING_INPUT_LABEL = /*$$(*/ "Missing input label" /*)*/;

  public static final String MISSING_OUTPUT_LABEL = /*$$(*/ "Missing output label" /*)*/;

  public static final String INVALID_IMPORT_FILE = /*$$(*/ "Data file must be excel file" /*)*/;

  public static final String INVALID_HEADER = /*$$(*/ "Header is invalid in import file" /*)*/;

  public static final String EMPTY_OUTPUT_COLUMN = /*$$(*/
      "Output columns can't be empty in import file" /*)*/;
}
