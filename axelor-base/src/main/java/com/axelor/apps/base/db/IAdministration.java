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
package com.axelor.apps.base.db;

/** Interface of Administration package. Enum all static variable of packages. */
@Deprecated
public interface IAdministration {

  /** Static select export type */
  static final String PDF = "pdf";

  static final String XLS = "xls";
  static final String CSV = "csv";

  /** Static select month */
  static final int JAN = 1;

  static final int FEB = 2;
  static final int MAR = 3;
  static final int APR = 4;
  static final int MAY = 5;
  static final int JUN = 6;
  static final int JUL = 7;
  static final int AUG = 8;
  static final int SEP = 9;
  static final int OCT = 10;
  static final int NOV = 11;
  static final int DEC = 12;

  /** Static select yes/no */
  static final int YES = 1;

  static final int NO = 0;
}
