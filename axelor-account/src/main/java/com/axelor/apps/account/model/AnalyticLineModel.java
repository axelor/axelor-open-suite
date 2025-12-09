/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.model;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AnalyticLineModel {

  protected AnalyticLine analyticLine;

  protected Product product;
  protected Account account;
  protected Company company;
  protected TradingName tradingName;
  protected Partner partner;

  protected boolean isPurchase;
  protected BigDecimal lineAmount;
  protected FiscalPosition fiscalPosition;

  public AnalyticLineModel(
      AnalyticLine analyticLine,
      Product product,
      Account account,
      Company company,
      TradingName tradingName,
      Partner partner,
      boolean isPurchase,
      BigDecimal lineAmount,
      FiscalPosition fiscalPosition) {
    Preconditions.checkNotNull(analyticLine);
    this.analyticLine = analyticLine;
    this.product = product;
    this.account = account;
    this.company = company;
    this.tradingName = tradingName;
    this.partner = partner;
    this.isPurchase = isPurchase;
    this.lineAmount = lineAmount;
    this.fiscalPosition = fiscalPosition;
  }

  public AnalyticLine getAnalyticLine() {
    return this.analyticLine;
  }

  public Product getProduct() {
    return this.product;
  }

  public Account getAccount() {
    return this.account;
  }

  public Company getCompany() {
    return this.company;
  }

  public TradingName getTradingName() {
    return this.tradingName;
  }

  public Partner getPartner() {
    return this.partner;
  }

  public boolean getIsPurchase() {
    return this.isPurchase;
  }

  public BigDecimal getLineAmount() {
    return this.lineAmount;
  }

  public void setLineAmount(BigDecimal amount) {
    this.lineAmount = amount;
  }

  public FiscalPosition getFiscalPosition() {
    return fiscalPosition;
  }

  public AnalyticDistributionTemplate getAnalyticDistributionTemplate() {
    return Optional.of(this)
        .map(AnalyticLineModel::getAnalyticLine)
        .map(AnalyticLine::getAnalyticDistributionTemplate)
        .orElse(null);
  }

  public List<AnalyticMoveLine> getAnalyticMoveLineList() {
    return Optional.of(this)
        .map(AnalyticLineModel::getAnalyticLine)
        .map(AnalyticLine::getAnalyticMoveLineList)
        .orElse(Collections.emptyList());
  }

  /*
  public void setAnalyticMoveLineList(List<AnalyticMoveLine> analyticMoveLineList) {
    this.analyticMoveLineList = analyticMoveLineList;
  }

  public void addAnalyticMoveLineListItem(AnalyticMoveLine analyticMoveLine) {
    this.analyticMoveLineList.add(analyticMoveLine);

    if (this.saleOrderLine != null) {
      analyticMoveLine.setSaleOrderLine(this.saleOrderLine);
    } else if (this.purchaseOrderLine != null) {
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

  public AnalyticAccount getAxis1AnalyticAccount() {
    return Optional.of(this).map(AnalyticLineModel::getAnalyticLine).map(AnalyticLine::getAxis1AnalyticAccount);
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
  }*/

}
