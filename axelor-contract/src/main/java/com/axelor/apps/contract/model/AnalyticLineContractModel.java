package com.axelor.apps.contract.model;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class AnalyticLineContractModel extends AnalyticLineModel {

  protected ContractLine contractLine;
  protected ContractVersion contractVersion;
  protected Contract contract;

  public AnalyticLineContractModel(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    super(saleOrderLine, saleOrder);
  }

  public AnalyticLineContractModel(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    // TODO uncomment after merge ticket #66646
    // super(purchaseOrderLine, purchaseOrder);
  }

  public AnalyticLineContractModel(
      ContractLine contractLine, ContractVersion contractVersion, Contract contract) {
    super();

    this.contractLine = contractLine;
    this.contractVersion =
        contractVersion != null ? contractVersion : contractLine.getContractVersion();
    this.contract = contract != null ? contract : this.contractVersion.getContract();

    this.analyticMoveLineList = contractLine.getAnalyticMoveLineList();
    this.axis1AnalyticAccount = contractLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = contractLine.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = contractLine.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = contractLine.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = contractLine.getAxis5AnalyticAccount();
    this.analyticDistributionTemplate = contractLine.getAnalyticDistributionTemplate();

    this.product = contractLine.getProduct();

    this.exTaxTotal = contractLine.getExTaxTotal();
  }

  @Override
  public <T extends AnalyticLineModel> T getExtension(Class<T> klass) throws AxelorException {
    try {
      if (contractLine != null) {
        return klass.getDeclaredConstructor(ContractLine.class).newInstance(this.contractLine);
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

  @Override
  public void addAnalyticMoveLineListItem(AnalyticMoveLine analyticMoveLine) {
    super.addAnalyticMoveLineListItem(analyticMoveLine);

    if (this.contractLine != null) {
      analyticMoveLine.setContractLine(this.contractLine);
    }
  }

  @Override
  public Company getCompany() {
    return Optional.ofNullable(this.contractLine)
        .map(ContractLine::getContractVersion)
        .map(ContractVersion::getContract)
        .map(Contract::getCompany)
        .orElse(super.getCompany());
  }

  @Override
  public Partner getPartner() {
    if (this.contract != null) {
      this.partner = this.contract.getPartner();
    } else {
      super.getPartner();
    }

    return this.partner;
  }

  @Override
  public FiscalPosition getFiscalPosition() {
    super.getFiscalPosition();

    if (this.contractLine != null) {
      this.fiscalPosition = this.contractLine.getFiscalPosition();
    }

    return this.fiscalPosition;
  }

  @Override
  public void copyToModel() {
    super.copyToModel();

    if (this.contractLine != null) {
      this.copyToContract();
    }
  }

  protected void copyToContract() {
    this.contractLine.setAnalyticDistributionTemplate(this.analyticDistributionTemplate);
    this.contractLine.setAxis1AnalyticAccount(this.axis1AnalyticAccount);
    this.contractLine.setAxis2AnalyticAccount(this.axis2AnalyticAccount);
    this.contractLine.setAxis3AnalyticAccount(this.axis3AnalyticAccount);
    this.contractLine.setAxis4AnalyticAccount(this.axis4AnalyticAccount);
    this.contractLine.setAxis5AnalyticAccount(this.axis5AnalyticAccount);
    this.contractLine.setAnalyticMoveLineList(this.analyticMoveLineList);
  }
}
