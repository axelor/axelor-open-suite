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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.PdfViewer;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface DMSService {
  DMSFile setDmsFile(MetaFile metaFile, PdfViewer pdfViewer);

  String getInlineUrl(DMSFile dmsFile);

  void addLinkedDMSFiles(List<? extends Model> entityList, Model entityMerged);

  void unzip(String zipFilePath, Model model) throws AxelorException;

  DMSFile getDMSRoot(Model related);

  DMSFile getDMSHome(Model related, DMSFile dmsRoot);

  DMSFile getDMSFolder(Model model, String fileName, DMSFile dmsRoot);

  DMSFile addDMSFileToParentFolder(
      Model model, String fileName, DMSFile dmsFolder, MetaFile metaFile);
}
