package com.axelor.apps.supplychain.model;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.List;

public class AnalyticLineModel implements AnalyticLine {
  protected SaleOrderLine saleOrderLine;
  protected PurchaseOrderLine purchaseOrderLine;

  protected AnalyticAccount axis1AnalyticAccount;
  protected AnalyticAccount axis2AnalyticAccount;
  protected AnalyticAccount axis3AnalyticAccount;
  protected AnalyticAccount axis4AnalyticAccount;
  protected AnalyticAccount axis5AnalyticAccount;
  protected List<AnalyticMoveLine> analyticMoveLineList;

  protected BigDecimal exTaxTotal;

  public AnalyticLineModel() {}

  public AnalyticLineModel(SaleOrderLine saleOrderLine) {
    this.saleOrderLine = saleOrderLine;

    this.analyticMoveLineList = saleOrderLine.getAnalyticMoveLineList();
    this.axis1AnalyticAccount = saleOrderLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = saleOrderLine.getAxis1AnalyticAccount();
    this.axis3AnalyticAccount = saleOrderLine.getAxis1AnalyticAccount();
    this.axis4AnalyticAccount = saleOrderLine.getAxis1AnalyticAccount();
    this.axis5AnalyticAccount = saleOrderLine.getAxis1AnalyticAccount();

    this.exTaxTotal = saleOrderLine.getExTaxTotal();
  }

  public AnalyticLineModel(PurchaseOrderLine purchaseOrderLine) {
    this.purchaseOrderLine = purchaseOrderLine;

    this.analyticMoveLineList = purchaseOrderLine.getAnalyticMoveLineList();
    this.axis1AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();
    this.axis3AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();
    this.axis4AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();
    this.axis5AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();

    this.exTaxTotal = purchaseOrderLine.getExTaxTotal();
  }

  @Override
  public BigDecimal getLineAmount() {
    return null;
  }

  @Override
  public Account getAccount() {
    return null;
  }

  @Override
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate() {
    return null;
  }

  @Override
  public List<AnalyticMoveLine> getAnalyticMoveLineList() {
    return this.analyticMoveLineList;
  }

  public void setAnalyticMoveLineList(List<AnalyticMoveLine> analyticMoveLineList) {
    this.analyticMoveLineList = analyticMoveLineList;
  }

  public void addAnalyticMoveLineListItem(AnalyticMoveLine analyticMoveLine) {
    this.analyticMoveLineList.add(analyticMoveLine);

    if (this.saleOrderLine != null) {
      analyticMoveLine.setSaleOrderLine(this.saleOrderLine);
    }

    if (this.purchaseOrderLine != null) {
      analyticMoveLine.setPurchaseOrderLine(this.purchaseOrderLine);
    }
  }

  public void clearAnalyticMoveLineList() {
    if (getAnalyticMoveLineList() != null) {
      getAnalyticMoveLineList().clear();
    }
  }

  @Override
  public AnalyticAccount getAxis1AnalyticAccount() {
    return axis1AnalyticAccount;
  }

  @Override
  public void setAxis1AnalyticAccount(AnalyticAccount axis1AnalyticAccount) {
    this.axis1AnalyticAccount = axis1AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis2AnalyticAccount() {
    return axis2AnalyticAccount;
  }

  @Override
  public void setAxis2AnalyticAccount(AnalyticAccount axis2AnalyticAccount) {
    this.axis2AnalyticAccount = axis2AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis3AnalyticAccount() {
    return axis3AnalyticAccount;
  }

  @Override
  public void setAxis3AnalyticAccount(AnalyticAccount axis3AnalyticAccount) {
    this.axis3AnalyticAccount = axis3AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis4AnalyticAccount() {
    return axis4AnalyticAccount;
  }

  @Override
  public void setAxis4AnalyticAccount(AnalyticAccount axis4AnalyticAccount) {
    this.axis4AnalyticAccount = axis4AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis5AnalyticAccount() {
    return axis5AnalyticAccount;
  }

  @Override
  public void setAxis5AnalyticAccount(AnalyticAccount axis5AnalyticAccount) {
    this.axis5AnalyticAccount = axis5AnalyticAccount;
  }

  public BigDecimal getExTaxTotal() {
    return this.exTaxTotal;
  }

  public Company getCompany() {
    if (this.saleOrderLine != null && this.saleOrderLine.getSaleOrder() != null) {
      return this.saleOrderLine.getSaleOrder().getCompany();
    }

    if (this.purchaseOrderLine != null && this.purchaseOrderLine.getPurchaseOrder() != null) {
      return this.purchaseOrderLine.getPurchaseOrder().getCompany();
    }

    return null;
  }
}
