/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.i18n.I18n;
import javax.persistence.PersistenceException;

public class InvoicingProjectManagementRepository extends InvoicingProjectRepository {

  @Override
  public void remove(InvoicingProject entity) {

    if (entity.getInvoice() != null) {
      throw new PersistenceException(
          I18n.get(
              "Since the invoice has already been generated, it's impossible to delete this record"));
    } else {
      super.remove(entity);
    }
  }
}
