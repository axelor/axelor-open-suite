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
package com.axelor.apps.base.recording;

import com.axelor.apps.base.db.Recording;
import com.axelor.apps.base.db.repo.RecordingRepository;
import com.axelor.apps.base.service.RecordingService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.event.Observes;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.inject.Beans;
import java.util.HashSet;
import java.util.Set;

public class RecordingListener {

  public void onBeforeTransactionComplete(@Observes BeforeTransactionComplete event) {

    Set<? extends Model> updated = new HashSet<>(event.getUpdated());
    if (ObjectUtils.isEmpty(updated)) {
      return;
    }

    Recording recording =
        Beans.get(RecordingRepository.class).findByStatus(RecordingRepository.STATUS_START);
    if (recording == null) {
      return;
    }

    for (Model model : updated) {
      String modelName = EntityHelper.getEntityClass(model).getName();
      Beans.get(RecordingService.class).addModelIdLog(recording, modelName, model.getId());
    }
  }
}
