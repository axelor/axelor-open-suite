/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db;

/** Interface of Alarm package. Enum all static variable of packages. */
@Deprecated
public interface IAlarm {

  /** Static select for Alarm & Message */

  // TYPE
  static final String INVOICE = "invoice";

  static final String PAYMENT = "payment";
  static final String REJECT = "reject";

  // TYPE
  static final int INVOICING_MANAGER = 1;
  static final int CONTRACT_MANAGER = 2;
  static final int COMMERCIAL_MANAGER = 3;
  static final int TECHNICAL_MANAGER = 4;
}
