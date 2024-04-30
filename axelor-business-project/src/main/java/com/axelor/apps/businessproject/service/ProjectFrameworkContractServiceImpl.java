package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectFrameworkContractServiceImpl implements ProjectFrameworkContractService {

  private ProductCompanyService productCompanyService;

  @Inject
  public ProjectFrameworkContractServiceImpl(ProductCompanyService productCompanyService) {
    this.productCompanyService = productCompanyService;
  }

  /**
   * get unit price if framework contract and product are set on task
   *
   * @param projectTask
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, Object> getProductDataFromContract(ProjectTask projectTask)
      throws AxelorException {
    Contract frameworkCustomerContract = projectTask.getFrameworkCustomerContract();
    Product product = projectTask.getProduct();

    Map<String, Object> productMap = new HashMap<>();
    if (product == null) {
      return productMap;
    }

    if (frameworkCustomerContract == null) {
      productMap.put(
          "unitPrice",
          productCompanyService.get(product, "salePrice", projectTask.getProject().getCompany()));
      productMap.put(
          "currency",
          productCompanyService.get(
              product, "saleCurrency", projectTask.getProject().getCompany()));
      return productMap;
    }

    List<ContractLine> contractLines =
        frameworkCustomerContract.getCurrentContractVersion().getContractLineList().stream()
            .filter(contractLine -> Objects.equals(product, contractLine.getProduct()))
            .collect(Collectors.toList());

    if (contractLines.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BusinessProjectExceptionMessage.PROJECT_TASK_FRAMEWORK_CONTRACT_PRODUCT_NOT_FOUND),
          projectTask.getName());
    }
    if (contractLines.size() > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BusinessProjectExceptionMessage.PROJECT_TASK_FRAMEWORK_CONTRACT_PRODUCT_NB_ERROR),
          projectTask.getName());
    }

    ContractLine contractLine = contractLines.get(0);
    productMap.put("unitPrice", contractLine.getPrice());
    productMap.put(
        "currency",
        Optional.ofNullable(contractLine.getContractVersion())
            .map(ContractVersion::getContract)
            .map(Contract::getCurrency)
            .orElse(product.getSaleCurrency()));

    return productMap;
  }
}
