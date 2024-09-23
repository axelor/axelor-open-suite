/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.message.db.Message;
import java.io.IOException;
import wslite.json.JSONException;

public interface ExtraHoursService {

  public void cancel(ExtraHours extraHours) throws AxelorException;

  public Message sendCancellationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void confirm(ExtraHours extraHours) throws AxelorException;

  public Message sendConfirmationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void validate(ExtraHours extraHours) throws AxelorException;

  public Message sendValidationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void refuse(ExtraHours extraHours) throws AxelorException;

  public Message sendRefusalEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void compute(ExtraHours extraHours);

  void updateLineEmployee(ExtraHours extraHours);
}
