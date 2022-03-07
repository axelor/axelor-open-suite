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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.exception.AxelorException;
import java.io.IOException;

public interface TemplateMessageAccountService {
  /**
   * Generate message and set second related entity to DebtRecoveryHistory Partner and add message
   * to DebtRecoveryHistory message list.
   *
   * @param debtRecoveryHistory
   * @param template
   * @return
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws InstantiationException
   * @throws AxelorException
   * @throws IllegalAccessException
   */
  Message generateMessage(DebtRecoveryHistory debtRecoveryHistory, Template template)
      throws ClassNotFoundException, IOException, InstantiationException, AxelorException,
          IllegalAccessException;
}
