package com.axelor.apps.supplychain.model;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
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

  public AnalyticLineModel() {}

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
    return this.tradingName;
  }

  public Company getCompany() {
    return this.company;
  }

  public Partner getPartner() {
    return this.partner;
  }

  public void copyToModel() {}
}
