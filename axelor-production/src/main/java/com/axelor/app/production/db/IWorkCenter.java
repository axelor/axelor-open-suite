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
package com.axelor.app.production.db;

/**
 * Interface of Event package. Enum all static variable of packages.
 *
 * @author dubaux
 */
@Deprecated
public interface IWorkCenter {

  /** Static work center type select */
  static final int WORK_CENTER_HUMAN = 1;

  static final int WORK_CENTER_MACHINE = 2;
  static final int WORK_CENTER_BOTH = 3;

  /** Static cost type select */
  static final int COST_PER_HOUR = 1;

  static final int COST_PER_CYCLE = 2;
  static final int COST_PER_PIECE = 3;
}
