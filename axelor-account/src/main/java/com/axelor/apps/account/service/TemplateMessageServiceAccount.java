package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;

import java.io.IOException;

public interface TemplateMessageServiceAccount extends TemplateMessageService {
    /**
     * Generate message and set second related entity to DebtRecoveryHistory
     * Partner and add message to DebtRecoveryHistory message list.
     * @param debtRecoveryHistory
     * @param template
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InstantiationException
     * @throws AxelorException
     * @throws IllegalAccessException
     */
    Message generateMessage(DebtRecoveryHistory debtRecoveryHistory, Template template) throws ClassNotFoundException, IOException, InstantiationException, AxelorException, IllegalAccessException;
}
