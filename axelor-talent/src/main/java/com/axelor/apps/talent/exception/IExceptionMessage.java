/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.talent.exception;

public interface IExceptionMessage {

  public static final String INVALID_DATE_RANGE = /*$$(*/
      "Invalid dates. From date must be before to date." /*)*/;

  public static final String INVALID_TR_DATE = /*$$(*/
      "Training dates must be under training session date range." /*)*/;

  public static final String NO_EVENT_GENERATED = /*$$(*/
      "No Training register is generated because selected employees don't have any user." /*)*/;
}
