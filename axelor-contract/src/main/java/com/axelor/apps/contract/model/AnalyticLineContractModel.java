package com.axelor.apps.contract.model;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public class AnalyticLineContractModel extends AnalyticLineModel {
  protected ContractLine contractLine;

  public AnalyticLineContractModel(SaleOrderLine saleOrderLine) {
    super(saleOrderLine);
  }

  public AnalyticLineContractModel(PurchaseOrderLine purchaseOrderLine) {
    super(purchaseOrderLine);
  }

  public AnalyticLineContractModel(ContractLine contractLine) {
    super();

    this.analyticMoveLineList = contractLine.getAnalyticMoveLineList();
    this.axis1AnalyticAccount = contractLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = contractLine.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = contractLine.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = contractLine.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = contractLine.getAxis5AnalyticAccount();

    this.exTaxTotal = contractLine.getExTaxTotal();
  }

  @Override
  public void addAnalyticMoveLineListItem(AnalyticMoveLine analyticMoveLine) {
    super.addAnalyticMoveLineListItem(analyticMoveLine);

    if (this.contractLine != null) {
      analyticMoveLine.setContractLine(this.contractLine);
    }
  }
}
