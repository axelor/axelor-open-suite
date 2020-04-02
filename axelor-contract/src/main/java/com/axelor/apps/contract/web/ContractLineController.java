/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.web;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Singleton
public class ContractLineController {

  public void changeProduct(ActionRequest request, ActionResponse response) {
    ContractLine contractLine = request.getContext().asType(ContractLine.class);
    Partner partner = null;
    Company company = null;
    if (request.getContext().getParent().getContextClass() == Contract.class) {
      Contract contract = request.getContext().getParent().asType(Contract.class);
      partner = contract.getPartner();
      company = contract.getCompany();
    } else if (request.getContext().getParent().getContextClass() == ContractVersion.class) {
      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);
      Contract contract = contractVersion.getContract();
      partner = contract.getPartner();
      company = contract.getCompany();
    } else {
      return;
    }

    Contract contract = null;
    if (request.getContext().getParent().getContextClass() == Contract.class) {
      contract = request.getContext().getParent().asType(Contract.class);
    } else if (request.getContext().getParent().getContextClass() == ContractVersion.class) {
      ContractVersion contractVersion =
          request.getContext().getParent().asType(ContractVersion.class);
      contract =
          contractVersion.getContractNext() == null
              ? contractVersion.getContract()
              : contractVersion.getContractNext();
    }
    Product product = contractLine.getProduct();

    try {
      BigDecimal price = product.getSalePrice();
      if (contract != null) {
        TaxLine taxLine =
            Beans.get(AccountManagementService.class)
                .getTaxLine(
                    LocalDate.now(),
                    product,
                    contract.getCompany(),
                    contract.getPartner().getFiscalPosition(),
                    false);
        response.setValue("taxLine", taxLine);

        if (taxLine != null && product.getInAti()) {
          price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
        }
      }

      response.setValue("productName", product.getName());
      response.setValue(
          "unit", product.getSalesUnit() == null ? product.getUnit() : product.getSalesUnit());
      response.setValue("price", price);

    } catch (Exception e) {
      response.setFlash(e.getMessage());
      this.resetProductInformation(response);
    }
  }

  private void resetProductInformation(ActionResponse response) {
    response.setValue("taxLine", null);
    response.setValue("productName", null);
    response.setValue("unit", null);
    response.setValue("price", null);
    response.setValue("exTaxTotal", null);
    response.setValue("inTaxTotal", null);
  }

  public void compute(ActionRequest request, ActionResponse response) {
    ContractLine contractLine = request.getContext().asType(ContractLine.class);
    Product product = contractLine.getProduct();

    if (contractLine == null || product == null) {
      this.resetProductInformation(response);
      return;
    }

    BigDecimal taxRate = BigDecimal.ZERO;
    if (contractLine.getTaxLine() != null) {
      taxRate = contractLine.getTaxLine().getValue();
    }
    BigDecimal exTaxTotal =
        contractLine.getQty().multiply(contractLine.getPrice()).setScale(2, RoundingMode.HALF_EVEN);
    BigDecimal inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));

    response.setValue("exTaxTotal", exTaxTotal);
    response.setValue("inTaxTotal", inTaxTotal);
  }
}
