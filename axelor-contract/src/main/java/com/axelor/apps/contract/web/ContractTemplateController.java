package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ContractTemplateController {

  public void changeProduct(ActionRequest request, ActionResponse response) {
    ContractLineService contractLineService = Beans.get(ContractLineService.class);
    ContractLine contractLine = new ContractLine();

    try {
      contractLine = request.getContext().asType(ContractLine.class);
      Product product = contractLine.getProduct();
      contractLine = contractLineService.fill(contractLine, product);

      response.setValues(contractLine);
    } catch (Exception e) {
      response.setValues(contractLineService.reset(contractLine));
    }
  }
}
