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
package com.axelor.apps.crm.db;

@Deprecated
public interface IOpportunity {

  /** Static opportunity sales stage select */
  static final int STAGE_NEW = 1;

  static final int STAGE_QUALIFICATION = 2;
  static final int STAGE_PROPOSITION = 3;
  static final int STAGE_NEGOTIATION = 4;
  static final int STAGE_CLOSED_WON = 5;
  static final int STAGE_CLOSED_LOST = 6;
}
