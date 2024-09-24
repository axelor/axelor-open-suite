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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.helpdesk.service.MailServiceHelpDeskImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.message.service.MailAccountService;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class MailServiceProjectImpl extends MailServiceHelpDeskImpl {

  @Inject
  public MailServiceProjectImpl(
      MailAccountService mailAccountService, AppBaseService appBaseService) {
    super(mailAccountService, appBaseService);
  }

  @Override
  protected Set<String> recipients(MailMessage message, Model entity) {

    String entityName =
        Optional.ofNullable(entity).map(Model::getClass).map(Class::getName).orElse(null);

    if (!ObjectUtils.isEmpty(entityName) && entityName.equals(ProjectTask.class.getName())) {
      ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(message.getRelatedId());
      boolean isPrivateNote = projectTask.getIsPrivateNote();

      Set<String> recipients = new LinkedHashSet<>();

      if (isPrivateNote) {
        recipients = getRecipients(projectTask.getInternalWatchers(), entityName, recipients);
      } else {
        recipients = getRecipients(projectTask.getInternalWatchers(), entityName, recipients);
        recipients = getRecipients(projectTask.getExternalWatchers(), entityName, recipients);
      }

      return recipients;
    }

    return super.recipients(message, entity);
  }

  public Set<String> getRecipients(
      Set<User> watcherSet, String entityName, Set<String> recipients) {

    if (!CollectionUtils.isEmpty(watcherSet)) {

      for (User watcher : watcherSet) {

        if (watcher.getReceiveEmails()
            && watcher.getFollowedMetaModelSet().stream()
                .map(MetaModel::getFullName)
                .anyMatch(entityName::equals)) {

          Partner partner = watcher.getPartner();

          if (partner != null && partner.getEmailAddress() != null) {
            recipients.add(partner.getEmailAddress().getAddress());
          } else if (watcher.getEmail() != null) {
            recipients.add(watcher.getEmail());
          }
        }
      }
    }

    return recipients;
  }
}
