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
public interface IEvent {

  /** Static event type select */
  static final int CALL = 1;

  static final int MEETING = 2;
  static final int TASK = 3;
  static final int HOLIDAY = 4;

  /** Static event call status select */
  static final int CALL_TYPE_INCOMING = 1;

  static final int CALL_TYPE_OUTGOING = 2;

  /** Static event status select */
  // Status for all kind of event, except tasks
  static final int STATUS_PLANIFIED = 1;

  static final int STATUS_REALIZED = 2;
  static final int STATUS_CANCELED = 3;

  // Status for tasks only
  static final int STATUS_NOT_STARTED = 11;
  static final int STATUS_ON_GOING = 12;
  static final int STATUS_PENDING = 13;
  static final int STATUS_FINISHED = 14;
  static final int STATUS_REPORTED = 15;

  /** Static event related to select */
  static final String RELATED_TO_PARTNER = "com.axelor.apps.base.db.Partner";

  static final String RELATED_TO_LEAD = "com.axelor.apps.crm.db.Lead";
}
