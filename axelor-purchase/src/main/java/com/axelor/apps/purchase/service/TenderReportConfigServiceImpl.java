/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.TenderReportConfig;
import com.axelor.apps.purchase.db.TenderReportConfigLine;
import com.axelor.apps.purchase.db.repo.TenderReportConfigRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

public class TenderReportConfigServiceImpl implements TenderReportConfigService {

  protected TenderReportConfigRepository tenderReportConfigRepository;
  protected MetaFieldRepository metaFieldRepository;
  protected MetaJsonFieldRepository metaJsonFieldRepository;

  @Inject
  public TenderReportConfigServiceImpl(
      TenderReportConfigRepository tenderReportConfigRepository,
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository) {
    this.tenderReportConfigRepository = tenderReportConfigRepository;
    this.metaFieldRepository = metaFieldRepository;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
  }

  @Override
  @Transactional
  public void addMetaFieldLines(Long configId, List<Long> fieldIds) {

    TenderReportConfig config = tenderReportConfigRepository.find(configId);
    List<MetaField> fields =
        metaFieldRepository
            .all()
            .filter(
                "self.id IN :ids AND self.id NOT IN (SELECT l.metaField.id FROM TenderReportConfigLine l WHERE l.tenderReportConfig = :config AND l.metaField IS NOT NULL)")
            .bind("ids", fieldIds)
            .bind("config", config)
            .fetch();
    int nextSeq =
        config.getTenderReportConfigLineList().stream()
                .mapToInt(l -> l.getSequence() == null ? 0 : l.getSequence())
                .max()
                .orElse(0)
            + 1;

    for (MetaField field : fields) {
      TenderReportConfigLine line = new TenderReportConfigLine();
      line.setMetaField(field);
      line.setSequence(nextSeq++);
      line.setTenderReportConfig(config);
      config.addTenderReportConfigLineListItem(line);
    }

    tenderReportConfigRepository.save(config);
  }

  @Override
  @Transactional
  public void addCustomFieldLines(Long configId, List<Long> fieldIds) {

    TenderReportConfig config = tenderReportConfigRepository.find(configId);
    List<MetaJsonField> customFields =
        metaJsonFieldRepository
            .all()
            .filter(
                "self.id IN :ids AND self.id NOT IN (SELECT l.customField.id FROM TenderReportConfigLine l WHERE l.tenderReportConfig = :config AND l.customField IS NOT NULL)")
            .bind("ids", fieldIds)
            .bind("config", config)
            .fetch();
    int nextSeq =
        config.getTenderReportConfigLineList().stream()
                .mapToInt(l -> l.getSequence() == null ? 0 : l.getSequence())
                .max()
                .orElse(0)
            + 1;

    for (MetaJsonField customField : customFields) {
      TenderReportConfigLine line = new TenderReportConfigLine();
      line.setCustomField(customField);
      line.setSequence(nextSeq++);
      line.setTenderReportConfig(config);
      config.addTenderReportConfigLineListItem(line);
    }

    tenderReportConfigRepository.save(config);
  }
}
