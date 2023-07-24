/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;

public interface TemplateMessageAccountService {
  /**
   * Generate message and set second related entity to DebtRecoveryHistory Partner and add message
   * to DebtRecoveryHistory message list.
   *
   * @param debtRecoveryHistory
   * @param template
   * @return
   * @throws ClassNotFoundException
   */
  Message generateMessage(DebtRecoveryHistory debtRecoveryHistory, Template template)
      throws ClassNotFoundException;
}
