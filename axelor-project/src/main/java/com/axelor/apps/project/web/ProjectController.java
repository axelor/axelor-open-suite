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
package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class ProjectController {

  public void importMembers(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getTeam() != null) {
      project.getTeam().getMembers().forEach(project::addMembersUserSetItem);
      response.setValue("membersUserSet", project.getMembersUserSet());
    }
  }

  public void clearProductSet(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Project project = request.getContext().asType(Project.class);

    Set<Product> productSet = project.getProductSet();
    Company projectCompany = project.getCompany();

    if (productSet != null && projectCompany != null) {

      Set<Product> finalProductSet = new HashSet<>();

      for (Product product : productSet) {
        for (ProductCompany productCompany : product.getProductCompanyList()) {
          if (productCompany.getCompany() == projectCompany) {
            finalProductSet.add(product);
            break;
          }
        }
      }

      response.setValue("productSet", finalProductSet);
    }
  }
}
