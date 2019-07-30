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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class TemplateMessageAccountServiceImpl implements TemplateMessageAccountService {

  protected TemplateMessageService templateMessageService;

  @Inject
  public TemplateMessageAccountServiceImpl(TemplateMessageService templateMessageService) {
    this.templateMessageService = templateMessageService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(DebtRecoveryHistory debtRecoveryHistory, Template template)
      throws ClassNotFoundException, IOException, InstantiationException, AxelorException,
          IllegalAccessException {
    Message message = this.templateMessageService.generateMessage(debtRecoveryHistory, template);
    message.setRelatedTo2Select(Partner.class.getCanonicalName());
    message.setRelatedTo2SelectId(
        debtRecoveryHistory.getDebtRecovery().getAccountingSituation().getPartner().getId());
    return message;
  }
}
