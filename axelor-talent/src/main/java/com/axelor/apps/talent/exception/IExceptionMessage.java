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
package com.axelor.apps.talent.exception;

public interface IExceptionMessage {

  public static final String INVALID_DATE_RANGE = /*$$(*/
      "Invalid dates. From date must be before to date." /*)*/;

  public static final String INVALID_TR_DATE = /*$$(*/
      "Training dates must be under training session date range." /*)*/;

  public static final String NO_EVENT_GENERATED = /*$$(*/
      "No Training register is generated because selected employees don't have any user." /*)*/;

  public static final String JOB_POSITION_DRAFT_WRONG_STATUS = /*$$(*/
      "Can only return to draft from canceled job position." /*)*/;

  public static final String JOB_POSITION_OPEN_WRONG_STATUS = /*$$(*/
      "Can only open drafted job position." /*)*/;

  public static final String JOB_POSITION_PAUSE_WRONG_STATUS = /*$$(*/
      "Can only pause opened job position." /*)*/;

  public static final String JOB_POSITION_CLOSE_WRONG_STATUS = /*$$(*/
      "Can only close opened or on hold job position." /*)*/;

  public static final String JOB_POSITION_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel closed job position." /*)*/;

  public static final String APPRAISAL_SEND_WRONG_STATUS = /*$$(*/
      "Can only send drafted appraisal." /*)*/;

  public static final String APPRAISAL_REALIZE_WRONG_STATUS = /*$$(*/
      "Can only realize sent appraisal." /*)*/;

  public static final String APPRAISAL_CANCEL_WRONG_STATUS = /*$$(*/
      "Can only cancel drafted or sent appraisal.." /*)*/;

  public static final String APPRAISAL_DRAFT_WRONG_STATUS = /*$$(*/
      "Can only return to draft from canceled appraisal." /*)*/;
}
