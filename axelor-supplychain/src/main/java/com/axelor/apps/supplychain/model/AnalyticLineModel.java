package com.axelor.apps.supplychain.model;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnalyticLineModel implements AnalyticLine {

  protected AnalyticAccount axis1AnalyticAccount;
  protected AnalyticAccount axis2AnalyticAccount;
  protected AnalyticAccount axis3AnalyticAccount;
  protected AnalyticAccount axis4AnalyticAccount;
  protected AnalyticAccount axis5AnalyticAccount;
  protected List<AnalyticMoveLine> analyticMoveLineList;
  protected AnalyticDistributionTemplate analyticDistributionTemplate;

  protected Product product;
  protected Account account;
  protected Company company;
  protected TradingName tradingName;
  protected Partner partner;

  protected boolean isPurchase;
  protected BigDecimal exTaxTotal;
  protected BigDecimal companyExTaxTotal;
  protected BigDecimal lineAmount;

  protected PurchaseOrderLine purchaseOrderLine;

  public AnalyticLineModel() {}

  public AnalyticLineModel(PurchaseOrderLine purchaseOrderLine) {
    this.purchaseOrderLine = purchaseOrderLine;

    this.axis1AnalyticAccount = purchaseOrderLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = purchaseOrderLine.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = purchaseOrderLine.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = purchaseOrderLine.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = purchaseOrderLine.getAxis5AnalyticAccount();
    this.analyticMoveLineList = purchaseOrderLine.getAnalyticMoveLineList();
    this.analyticDistributionTemplate = purchaseOrderLine.getAnalyticDistributionTemplate();

    this.product = purchaseOrderLine.getProduct();

    this.isPurchase = true;
    this.exTaxTotal = purchaseOrderLine.getExTaxTotal();
    this.companyExTaxTotal = purchaseOrderLine.getCompanyExTaxTotal();
  }

  public <T extends AnalyticLineModel> T getExtension(Class<T> klass) throws AxelorException {
    try {
      if (purchaseOrderLine != null) {
        return klass
            .getDeclaredConstructor(PurchaseOrderLine.class)
            .newInstance(this.purchaseOrderLine);
      }

      return null;
    } catch (IllegalAccessException
        | InstantiationException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  @Override
  public BigDecimal getLineAmount() {
    return this.lineAmount;
  }

  @Override
  public Account getAccount() {
    return this.account;
  }

  @Override
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate() {
    return this.analyticDistributionTemplate;
  }

  public void setAnalyticDistributionTemplate(
      AnalyticDistributionTemplate analyticDistributionTemplate) {
    this.analyticDistributionTemplate = analyticDistributionTemplate;
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

    if (this.purchaseOrderLine != null) {
      analyticMoveLine.setPurchaseOrderLine(this.purchaseOrderLine);
    }
  }

  public void clearAnalyticMoveLineList() {
    if (getAnalyticMoveLineList() != null) {
      getAnalyticMoveLineList().clear();
    } else {
      setAnalyticMoveLineList(new ArrayList<>());
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
    return this.axis2AnalyticAccount;
  }

  @Override
  public void setAxis2AnalyticAccount(AnalyticAccount axis2AnalyticAccount) {
    this.axis2AnalyticAccount = axis2AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis3AnalyticAccount() {
    return this.axis3AnalyticAccount;
  }

  @Override
  public void setAxis3AnalyticAccount(AnalyticAccount axis3AnalyticAccount) {
    this.axis3AnalyticAccount = axis3AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis4AnalyticAccount() {
    return this.axis4AnalyticAccount;
  }

  @Override
  public void setAxis4AnalyticAccount(AnalyticAccount axis4AnalyticAccount) {
    this.axis4AnalyticAccount = axis4AnalyticAccount;
  }

  @Override
  public AnalyticAccount getAxis5AnalyticAccount() {
    return this.axis5AnalyticAccount;
  }

  @Override
  public void setAxis5AnalyticAccount(AnalyticAccount axis5AnalyticAccount) {
    this.axis5AnalyticAccount = axis5AnalyticAccount;
  }

  public Product getProduct() {
    return this.product;
  }

  public boolean getIsPurchase() {
    return this.isPurchase;
  }

  public BigDecimal getExTaxTotal() {
    return this.exTaxTotal;
  }

  public BigDecimal getCompanyExTaxTotal() {
    return this.companyExTaxTotal;
  }

  public TradingName getTradingName() {

    if (this.purchaseOrderLine != null && this.purchaseOrderLine.getPurchaseOrder() != null) {
      this.tradingName = this.purchaseOrderLine.getPurchaseOrder().getTradingName();
    }

    return this.tradingName;
  }

  public Company getCompany() {

    if (this.purchaseOrderLine != null && this.purchaseOrderLine.getPurchaseOrder() != null) {
      this.company = this.purchaseOrderLine.getPurchaseOrder().getCompany();
    }

    return this.company;
  }

  public Partner getPartner() {

    if (this.purchaseOrderLine != null && this.purchaseOrderLine.getPurchaseOrder() != null) {
      this.partner = this.purchaseOrderLine.getPurchaseOrder().getSupplierPartner();
    }

    return this.partner;
  }

  public void copyToModel() {
    if (this.purchaseOrderLine != null) {
      this.copyToPurchaseOrder();
    }
  }

  protected void copyToPurchaseOrder() {
    this.purchaseOrderLine.setAnalyticDistributionTemplate(this.analyticDistributionTemplate);
    this.purchaseOrderLine.setAxis1AnalyticAccount(this.axis1AnalyticAccount);
    this.purchaseOrderLine.setAxis2AnalyticAccount(this.axis2AnalyticAccount);
    this.purchaseOrderLine.setAxis3AnalyticAccount(this.axis3AnalyticAccount);
    this.purchaseOrderLine.setAxis4AnalyticAccount(this.axis4AnalyticAccount);
    this.purchaseOrderLine.setAxis5AnalyticAccount(this.axis5AnalyticAccount);
    this.purchaseOrderLine.setAnalyticMoveLineList(this.analyticMoveLineList);
  }
}
