package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.project.db.ProjectTask;
import java.util.Map;

public interface ProjectFrameworkContractService {

  Map<String, Object> getProductDataFromContract(ProjectTask projectTask) throws AxelorException;

  Product getEmployeeProduct(ProjectTask projectTask) throws AxelorException;

  String getCustomerContractDomain(ProjectTask projectTask);

  String getSupplierContractDomain(ProjectTask projectTask);
}
