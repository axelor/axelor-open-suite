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
package com.axelor.apps.crm.db;

/**
 * Interface of Event object. Enum all static variable of object.
 *
 * @author dubaux
 */
@Deprecated
public interface IEventReminder {

  /** Static duration type select */
  static final int DURATION_MINUTES = 1;

  static final int DURATION_HOURS = 2;
  static final int DURATION_DAYS = 3;
  static final int DURATION_WEEKS = 4;
}
