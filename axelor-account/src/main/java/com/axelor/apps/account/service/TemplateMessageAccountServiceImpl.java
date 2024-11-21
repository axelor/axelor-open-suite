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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.base.db.Partner;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TemplateMessageAccountServiceImpl implements TemplateMessageAccountService {

  protected TemplateMessageService templateMessageService;

  @Inject
  public TemplateMessageAccountServiceImpl(TemplateMessageService templateMessageService) {
    this.templateMessageService = templateMessageService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(DebtRecoveryHistory debtRecoveryHistory, Template template)
      throws ClassNotFoundException {
    Message message = this.templateMessageService.generateMessage(debtRecoveryHistory, template);
    Long id =
        debtRecoveryHistory.getDebtRecovery().getTradingName() == null
            ? debtRecoveryHistory.getDebtRecovery().getAccountingSituation().getPartner().getId()
            : debtRecoveryHistory
                .getDebtRecovery()
                .getTradingNameAccountingSituation()
                .getPartner()
                .getId();

    Beans.get(MessageService.class)
        .addMessageRelatedTo(message, Partner.class.getCanonicalName(), id);

    return message;
  }
}
