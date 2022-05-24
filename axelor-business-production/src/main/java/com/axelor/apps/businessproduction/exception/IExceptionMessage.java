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
package com.axelor.apps.businessproduction.exception;

public interface IExceptionMessage {

  String OPERATION_ORDER_TIMESHEET_WAITING_VALIDATION = /*$$(*/
      "There are timesheet still waiting validation on this operation order, do you want to continue anyway ?" /*)*/;
  String MANUF_ORDER_TIMESHEET_WAITING_VALIDATION = /*$$(*/
      "There are timesheet still waiting validation on this manuf order, do you want to continue anyway ?" /*)*/;
}
