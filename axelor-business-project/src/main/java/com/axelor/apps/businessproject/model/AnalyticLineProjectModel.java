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
package com.axelor.apps.businessproject.model;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class AnalyticLineProjectModel extends AnalyticLineContractModel {

  protected Project project;

  public AnalyticLineProjectModel(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    super(saleOrderLine, saleOrder);

    this.project = saleOrderLine.getProject();
  }

  public AnalyticLineProjectModel(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    super(purchaseOrderLine, purchaseOrder);

    this.project = purchaseOrderLine.getProject();
  }

  public AnalyticLineProjectModel(
      ContractLine contractLine, ContractVersion contractVersion, Contract contract) {
    super(contractLine, contractVersion, contract);

    this.project =
        Optional.of(contractLine)
            .map(ContractLine::getContractVersion)
            .map(ContractVersion::getContract)
            .map(Contract::getProject)
            .orElse(null);
  }

  public AnalyticLineProjectModel(Project project) {
    this.project = project;

    this.axis1AnalyticAccount = project.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = project.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = project.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = project.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = project.getAxis5AnalyticAccount();
    this.analyticMoveLineList = project.getAnalyticMoveLineList();
    this.analyticDistributionTemplate = project.getAnalyticDistributionTemplate();
  }

  @Override
  public <T extends AnalyticLineModel> T getExtension(Class<T> klass) throws AxelorException {
    try {
      if (project != null) {
        return klass.getDeclaredConstructor(Project.class).newInstance(this.project);
      } else {
        return super.getExtension(klass);
      }
    } catch (IllegalAccessException
        | InstantiationException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  public Project getProject() {
    return this.project;
  }

  @Override
  public Company getCompany() {
    if (this.project != null) {
      this.company = this.project.getCompany();
    } else {
      super.getCompany();
    }

    return this.company;
  }

  @Override
  public Partner getPartner() {
    if (this.project != null) {
      this.partner = this.project.getClientPartner();
    } else {
      super.getPartner();
    }

    return this.partner;
  }

  public void copyToModel() {
    if (this.project != null) {
      this.copyToProject();
    } else {
      super.copyToModel();
    }
  }

  protected void copyToProject() {
    this.project.setAnalyticDistributionTemplate(this.analyticDistributionTemplate);
    this.project.setAxis1AnalyticAccount(this.axis1AnalyticAccount);
    this.project.setAxis2AnalyticAccount(this.axis2AnalyticAccount);
    this.project.setAxis3AnalyticAccount(this.axis3AnalyticAccount);
    this.project.setAxis4AnalyticAccount(this.axis4AnalyticAccount);
    this.project.setAxis5AnalyticAccount(this.axis5AnalyticAccount);
    this.project.setAnalyticMoveLineList(this.analyticMoveLineList);
  }
}
