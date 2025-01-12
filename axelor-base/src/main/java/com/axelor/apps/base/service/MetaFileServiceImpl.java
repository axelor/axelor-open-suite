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

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;

public class MetaFileServiceImpl implements MetaFileService {
  protected final MetaFiles metaFiles;

  @Inject
  public MetaFileServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile copyMetaFile(MetaFile metaFile) throws IOException {
    String copiedFileName = Files.getNameWithoutExtension(metaFile.getFileName()) + "_copy";
    File copiedFile =
        File.createTempFile(copiedFileName, "." + Files.getFileExtension(metaFile.getFilePath()));
    Files.copy(new File(MetaFiles.getPath(metaFile).toString()), copiedFile);
    return metaFiles.upload(copiedFile);
  }
}
