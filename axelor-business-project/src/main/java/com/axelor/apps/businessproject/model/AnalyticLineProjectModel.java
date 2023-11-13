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
package com.axelor.apps.businessproject.model;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
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

  public Project getProject() {
    return this.project;
  }
}
