/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class InvoiceLineProjectServiceImpl implements InvoiceLineProjectService {

  @Inject private InvoiceLineRepository invoiceLineRepo;

  @Transactional
  @Override
  public void setProject(List<Long> invoiceLineIds, Project project) {

    if (invoiceLineIds != null) {

      List<InvoiceLine> invoiceLineList =
          invoiceLineRepo.all().filter("self.id in ?1", invoiceLineIds).fetch();

      for (InvoiceLine line : invoiceLineList) {
        line.setProject(project);
        invoiceLineRepo.save(line);
      }
    }
  }
}
