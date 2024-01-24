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
package com.axelor.apps.quality.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;

public class QualityConfigService {

  public QualityConfig getQualityConfig(Company company) throws AxelorException {
    QualityConfig qualityConfig = company.getQualityConfig();

    if (qualityConfig == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(QualityExceptionMessage.QUALITY_CONFIG),
          company.getName());
    }
    return qualityConfig;
  }

  public Sequence getQiActionDistributionSequence(QualityConfig qualityConfig)
      throws AxelorException {
    Sequence qiDecisionDistributionSequence = qualityConfig.getQiActionDistributionSequence();

    if (qiDecisionDistributionSequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(QualityExceptionMessage.QI_ACTION_DISTRIBUTION_SEQUENCE_NOT_SET));
    }
    return qiDecisionDistributionSequence;
  }

  public Sequence getQiDecisionDistributionSequence(QualityConfig qualityConfig)
      throws AxelorException {
    Sequence qiDecisionDistributionSequence = qualityConfig.getQiDecisionDistributionSequence();

    if (qiDecisionDistributionSequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(QualityExceptionMessage.QI_DECISION_DISTRIBUTION_SEQUENCE_NOT_FOUND));
    }
    return qiDecisionDistributionSequence;
  }
}
