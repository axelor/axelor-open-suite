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
package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.Message;

public class MessageManagementRepository extends MessageRepository {
  @Override
  public Message copy(Message entity, boolean deep) {
    entity.setStatusSelect(1);
    entity.setSentDateT(null);
    entity.setToEmailAddressSet(null);
    entity.setCcEmailAddressSet(null);
    entity.setBccEmailAddressSet(null);
    entity.setRecipientUser(null);
    return super.copy(entity, deep);
  }
}
