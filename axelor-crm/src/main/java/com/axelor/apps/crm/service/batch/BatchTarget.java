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
package com.axelor.apps.crm.service.batch;

import com.axelor.apps.crm.db.TargetConfiguration;
import com.axelor.apps.crm.db.repo.TargetConfigurationRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.TargetService;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchTarget extends BatchStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private TargetConfigurationRepository targetConfigurationRepo;

  @Inject
  public BatchTarget(TargetService targetService) {

    super(targetService);
  }

  @Override
  protected void process() {

    int i = 0;

    List<TargetConfiguration> targetConfigurationList = new ArrayList<>();
    if (batch.getCrmBatch().getTargetConfigurationSet() != null
        && !batch.getCrmBatch().getTargetConfigurationSet().isEmpty()) {
      targetConfigurationList.addAll(batch.getCrmBatch().getTargetConfigurationSet());
    }

    for (TargetConfiguration targetConfiguration : targetConfigurationList) {

      try {

        targetService.createsTargets(targetConfiguration);
        updateTargetConfiguration(targetConfiguration);
        i++;

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get(IExceptionMessage.BATCH_TARGET_1),
                    targetConfigurationRepo.find(targetConfiguration.getId()).getCode()),
                e),
            IException.CRM,
            batch.getId()); // TODO

        incrementAnomaly();

        LOG.error(
            "Bug(Anomalie) généré(e) pour le rappel de l'évènement {}",
            targetConfigurationRepo.find(targetConfiguration.getId()).getCode());

      } finally {

        if (i % 1 == 0) {
          JPA.clear();
        }
      }
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(IExceptionMessage.BATCH_TARGET_2) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_TARGET_3) + "\n", batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
