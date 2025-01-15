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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FileServiceImpl implements FileService {

  protected DMSService dmsService;
  protected FileRepository contractFileRepository;

  @Inject
  public FileServiceImpl(DMSService dmsService, FileRepository contractFileRepository) {
    this.dmsService = dmsService;
    this.contractFileRepository = contractFileRepository;
  }

  @Override
  @Transactional
  public void setDMSFile(File file) {
    MetaFile metaFile = file.getMetaFile();
    dmsService.setDmsFile(metaFile, file);
    contractFileRepository.save(file);
  }

  @Override
  public String getInlineUrl(File file) {
    MetaFile metaFile = file.getMetaFile();
    DMSFile dmsFile = file.getDmsFile();
    if (metaFile == null || dmsFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(dmsFile);
  }
}
