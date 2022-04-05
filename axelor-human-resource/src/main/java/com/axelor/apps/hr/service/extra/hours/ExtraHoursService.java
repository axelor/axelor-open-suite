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
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface ExtraHoursService {

  /**
   * Set extra hours status to canceled.
   *
   * @param extraHours
   * @throws AxelorException if the extra hours were already canceled.
   */
  public void cancel(ExtraHours extraHours) throws AxelorException;

  public Message sendCancellationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set extra hours sent date and change status to confirmed.
   *
   * @param extraHours
   * @throws AxelorException if the extra hours weren't drafted.
   */
  public void confirm(ExtraHours extraHours) throws AxelorException;

  public Message sendConfirmationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set the extra hours validation date, the user who validated the extra hours and change the
   * status to validated.
   *
   * @param extraHours
   * @throws AxelorException if the extra hours were not confirmed.
   */
  public void validate(ExtraHours extraHours) throws AxelorException;

  public Message sendValidationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set the extra hours refusal date, the user who refused the extra hours and change the status to
   * refused.
   *
   * @param extraHours
   * @throws AxelorException if the extra hours were not confirmed.
   */
  public void refuse(ExtraHours extraHours) throws AxelorException;

  /**
   * Set the extra hours status to draft.
   *
   * @param extraHours
   * @throws AxelorException if the extra hours were not refused nor canceled.
   */
  public void draft(ExtraHours extraHours) throws AxelorException;

  public Message sendRefusalEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public void compute(ExtraHours extraHours);
}
