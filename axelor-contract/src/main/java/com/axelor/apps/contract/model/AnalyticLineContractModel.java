package com.axelor.apps.contract.model;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

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

    this.contractLine = contractLine;

    this.analyticMoveLineList = contractLine.getAnalyticMoveLineList();
    this.axis1AnalyticAccount = contractLine.getAxis1AnalyticAccount();
    this.axis2AnalyticAccount = contractLine.getAxis2AnalyticAccount();
    this.axis3AnalyticAccount = contractLine.getAxis3AnalyticAccount();
    this.axis4AnalyticAccount = contractLine.getAxis4AnalyticAccount();
    this.axis5AnalyticAccount = contractLine.getAxis5AnalyticAccount();
    this.analyticDistributionTemplate = contractLine.getAnalyticDistributionTemplate();

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

  public Company getCompany() {
    return Optional.ofNullable(this.contractLine)
        .map(ContractLine::getContractVersion)
        .map(ContractVersion::getContract)
        .map(Contract::getCompany)
        .orElse(super.getCompany());
  }
}
