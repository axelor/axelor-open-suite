/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.interfaces.PdfViewer;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class DMSServiceImpl implements DMSService {

  protected DMSFileRepository dmsFileRepository;
  protected MetaFiles metaFiles;

  @Inject
  public DMSServiceImpl(DMSFileRepository dmsFileRepository, MetaFiles metaFiles) {
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public DMSFile setDmsFile(MetaFile metaFile, PdfViewer pdfViewer) {
    if (metaFile == null) {
      pdfViewer.setDmsFile(null);

      DMSFile previousDmsFile = pdfViewer.getDmsFile();
      if (previousDmsFile != null) {
        dmsFileRepository.remove(previousDmsFile);
      }
      return null;
    }
    DMSFile dmsFile = metaFiles.attach(metaFile, metaFile.getFileName(), (Model) pdfViewer);
    pdfViewer.setDmsFile(dmsFile);
    return dmsFile;
  }

  @Override
  public String getInlineUrl(DMSFile dmsFile) {
    if (dmsFile == null) {
      return "";
    }
    return String.format("ws/dms/inline/%d", dmsFile.getId());
  }
}
