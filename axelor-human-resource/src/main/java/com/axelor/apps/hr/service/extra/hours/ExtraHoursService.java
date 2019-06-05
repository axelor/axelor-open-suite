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
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import javax.mail.MessagingException;

public interface ExtraHoursService {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(ExtraHours extraHours) throws AxelorException;

  public Message sendCancellationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void confirm(ExtraHours extraHours) throws AxelorException;

  public Message sendConfirmationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void validate(ExtraHours extraHours) throws AxelorException;

  public Message sendValidationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void refuse(ExtraHours extraHours) throws AxelorException;

  public Message sendRefusalEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException;
}
