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
package com.axelor.apps.quality.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.quality.db.QIActionDistribution;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QITask;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;

public class QIActionDistributionServiceImpl implements QIActionDistributionService {

  protected QualityConfigService qualityConfigService;
  protected SequenceService sequenceService;

  @Inject
  public QIActionDistributionServiceImpl(
      QualityConfigService qualityConfigService, SequenceService sequenceService) {
    this.qualityConfigService = qualityConfigService;
    this.sequenceService = sequenceService;
  }

  @Override
  public QIActionDistribution createQIActionDistribution(
      QIAnalysis qiAnalysis, Company company, Partner responsiblePartner, List<QITask> qiTasks)
      throws AxelorException {
    QIActionDistribution qiActionDistribution =
        this.createQIActionDistribution(
            company, qiTasks.get(0).getResponsible(), responsiblePartner);
    qiActionDistribution.setQiDecision(qiTasks.get(0).getQiDecision());
    qiActionDistribution.setQiTaskSet(new HashSet<>(qiTasks));
    return qiActionDistribution;
  }

  @Override
  public QIActionDistribution createQIActionDistribution(
      Company company, Integer recipient, Partner recipientPartner) throws AxelorException {
    QIActionDistribution qiActionDistribution = new QIActionDistribution();
    Sequence qiActionDistributionSequence =
        qualityConfigService.getQiActionDistributionSequence(
            qualityConfigService.getQualityConfig(company));
    qiActionDistribution.setSequence(
        sequenceService.getSequenceNumber(
            qiActionDistributionSequence,
            QIActionDistribution.class,
            "sequence",
            qiActionDistribution));
    qiActionDistribution.setRecipient(recipient);
    qiActionDistribution.setRecipientPartner(recipientPartner);
    return qiActionDistribution;
  }
}
