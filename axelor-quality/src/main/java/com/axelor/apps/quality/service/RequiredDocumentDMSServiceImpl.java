/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.quality.db.RequiredDocument;
import com.axelor.apps.quality.db.repo.RequiredDocumentRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class RequiredDocumentDMSServiceImpl implements RequiredDocumentDMSService {

  protected final DMSService dmsService;
  protected final RequiredDocumentRepository requiredDocRepo;
  protected final DMSFileRepository dmsFileRepo;
  protected final MetaFileRepository metaFileRepo;

  @Inject
  public RequiredDocumentDMSServiceImpl(
      DMSService dmsService,
      RequiredDocumentRepository requiredDocRepo,
      DMSFileRepository dmsFileRepo,
      MetaFileRepository metaFileRepo) {
    this.dmsService = dmsService;
    this.requiredDocRepo = requiredDocRepo;
    this.dmsFileRepo = dmsFileRepo;
    this.metaFileRepo = metaFileRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setDMSFile(RequiredDocument requiredDocument) {

    DMSFile dmsFile = requiredDocument.getDmsFile();
    MetaFile metaFile = requiredDocument.getMetaFile();

    if (metaFile == null || dmsFile == null) {
      dmsService.setDmsFile(metaFile, requiredDocument);
      requiredDocRepo.save(requiredDocument);
      return;
    }

    dmsFile = dmsFileRepo.find(dmsFile.getId());
    metaFile = metaFileRepo.find(metaFile.getId());

    if (metaFile.equals(dmsFile.getMetaFile())) {
      dmsFile.setFileName(metaFile.getFileName());
    } else {
      requiredDocument.setDmsFile(null);
      dmsFileRepo.remove(dmsFile);
      dmsService.setDmsFile(metaFile, requiredDocument);
    }

    requiredDocRepo.save(requiredDocument);
  }

  @Override
  public String getInlineUrl(RequiredDocument requiredDocument) {
    return dmsService.getInlineUrl(requiredDocument.getDmsFile());
  }
}
