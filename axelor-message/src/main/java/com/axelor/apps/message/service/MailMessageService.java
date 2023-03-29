/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import com.axelor.auth.db.User;
import com.axelor.db.Model;

public interface MailMessageService {

  /**
   * Send notification to user via AOP MailMessage from a process.
   *
   * @param user the user which is going to have the notification
   * @param subject the subject of the message
   * @param body the body of the message
   */
  void sendNotification(User user, String subject, String body);

  /**
   * Send notification to user via AOP MailMessage from a process.
   *
   * @param user the user which is going to have the notification
   * @param subject the subject of the message
   * @param body the body of the message
   * @param relatedId related id of the model used in the process
   * @param relatedModel related model used in the process
   */
  void sendNotification(
      User user, String subject, String body, Long relatedId, Class<? extends Model> relatedModel);
}
