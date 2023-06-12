package com.axelor.apps.businessproject.model;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Optional;

public class AnalyticLineProjectModel extends AnalyticLineContractModel {
  protected Project project;

  public AnalyticLineProjectModel(SaleOrderLine saleOrderLine) {
    super(saleOrderLine);

    this.project = saleOrderLine.getProject();
  }

  public AnalyticLineProjectModel(PurchaseOrderLine purchaseOrderLine) {
    super(purchaseOrderLine);

    this.project = purchaseOrderLine.getProject();
  }

  public AnalyticLineProjectModel(ContractLine contractLine) {
    super(contractLine);

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
