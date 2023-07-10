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
package com.axelor.apps.crm.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Catalog;
import com.axelor.apps.crm.db.repo.CatalogRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.file.PdfTool;
import java.nio.file.Path;

public class CatalogController {

  public void showPdf(ActionRequest request, ActionResponse response) {

    try {
      Catalog catalog =
          Beans.get(CatalogRepository.class)
              .find(request.getContext().asType(Catalog.class).getId());
      MetaFile pdf = catalog.getPdfFile();
      String title = catalog.getName();
      Path path = MetaFiles.getPath(pdf.getFileName());
      String fileLink =
          PdfTool.getFileLinkFromPdfFile(
              PdfTool.printCopiesToFile(path.toFile(), 1), title + ".pdf");
      response.setView(ActionView.define(title).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
