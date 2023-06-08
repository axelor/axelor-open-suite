package com.axelor.apps.businessproject.model;

import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;

public class AnalyticLineProjectModel extends AnalyticLineContractModel {
  protected Project project;

  public AnalyticLineProjectModel(SaleOrderLine saleOrderLine) {
    super(saleOrderLine);
  }

  public AnalyticLineProjectModel(PurchaseOrderLine purchaseOrderLine) {
    super(purchaseOrderLine);
  }

  public AnalyticLineProjectModel(ContractLine contractLine) {
    super(contractLine);

    this.project = contractLine.getContractVersion().getContract().getProject();
  }

  public Project getProject() {
    return this.project;
  }
}
