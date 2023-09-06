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

    if (saleOrderLine != null) {
      this.project = saleOrderLine.getProject();
    }
  }

  public AnalyticLineProjectModel(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    super(purchaseOrderLine, purchaseOrder);

    if (purchaseOrderLine != null) {
      this.project = purchaseOrderLine.getProject();
    }
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
