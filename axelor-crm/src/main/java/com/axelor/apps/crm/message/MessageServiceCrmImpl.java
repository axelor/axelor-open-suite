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
package com.axelor.apps.crm.message;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.AppSettingsMessageService;
import com.axelor.message.service.SendMailQueueService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class MessageServiceCrmImpl extends MessageServiceBaseImpl {

  @Inject
  public MessageServiceCrmImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      AppSettingsMessageService appSettingsMessageService,
      UserService userService,
      AppBaseService appBaseService) {
    super(
        metaAttachmentRepository,
        messageRepository,
        sendMailQueueService,
        appSettingsMessageService,
        userService,
        appBaseService);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Message createMessage(Event event)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, IOException {

    // Get template depending on event type
    Template template = null;
    Company company = event.getUser().getActiveCompany();

    if (company != null) {
      switch (event.getTypeSelect()) {
        case ICalendarEventRepository.TYPE_EVENT:
          template = Beans.get(CrmConfigService.class).getCrmConfig(company).getEventTemplate();
          break;

        case ICalendarEventRepository.TYPE_CALL:
          template = Beans.get(CrmConfigService.class).getCrmConfig(company).getCallTemplate();
          break;

        case ICalendarEventRepository.TYPE_MEETING:
          template = Beans.get(CrmConfigService.class).getCrmConfig(company).getMeetingTemplate();
          break;

        case ICalendarEventRepository.TYPE_TASK:
          template = Beans.get(CrmConfigService.class).getCrmConfig(company).getTaskTemplate();
          break;

        default:
          break;
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.EVENT_USER_NO_ACTIVE_COMPANY),
          event.getUser().getName());
    }

    Message message = Beans.get(TemplateMessageService.class).generateMessage(event, template);

    return messageRepository.save(message);
  }
}
