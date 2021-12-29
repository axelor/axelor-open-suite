/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.InputStream;

public class MetaFilesPortal extends MetaFiles {

  @Inject
  public MetaFilesPortal(MetaFileRepository filesRepo) {
    super(filesRepo);
  }

  @Transactional
  @Override
  public DMSFile attach(InputStream stream, String fileName, Model entity) throws IOException {
    DMSFile dmsFile = super.attach(stream, fileName, entity);
    manageSharedPartner(dmsFile.getMetaFile(), entity);
    return dmsFile;
  }

  @Transactional
  @Override
  public MetaAttachment attach(MetaFile file, Model entity) {
    MetaAttachment attchement = super.attach(file, entity);
    manageSharedPartner(file, entity);
    return attchement;
  }

  @Transactional
  protected void manageSharedPartner(MetaFile file, Model entity) {

    if (file == null || !Beans.get(AppService.class).isApp("portal")) {
      return;
    }

    Partner partner = null;
    if (entity.getClass().isAssignableFrom(SaleOrder.class)) {
      SaleOrder order = (SaleOrder) entity;
      partner = order.getClientPartner();

    } else if (entity.getClass().isAssignableFrom(Invoice.class)) {
      Invoice invoice = (Invoice) entity;
      partner = invoice.getPartner();

    } else if (entity.getClass().isAssignableFrom(PortalQuotation.class)) {
      PortalQuotation quotation = (PortalQuotation) entity;
      if (quotation.getSaleOrder() != null) partner = quotation.getSaleOrder().getClientPartner();

    } else if (entity.getClass().isAssignableFrom(ProjectTask.class)) {
      ProjectTask task = (ProjectTask) entity;
      if (task.getProject() != null) partner = task.getProject().getClientPartner();
    }

    file.setSharedWith(partner);
    Beans.get(MetaFileRepository.class).save(file);
  }
}
