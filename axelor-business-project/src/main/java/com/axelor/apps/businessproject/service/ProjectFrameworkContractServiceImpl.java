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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    Product product = projectTask.getProduct();

    Map<String, Object> productMap = new HashMap<>();
    if (product == null) {
      return productMap;
    }
    productMap.putAll(getProductDataFromCustomerContract(projectTask, product));
    productMap.putAll(getProductDataFromSupplierContract(projectTask, product));

    return productMap;
  }

  protected Map<String, Object> getProductDataFromCustomerContract(
      ProjectTask projectTask, Product product) throws AxelorException {
    Contract frameworkCustomerContract = projectTask.getFrameworkCustomerContract();

    Map<String, Object> productMap = new HashMap<>();
    if (projectTask.getProject() == null) {
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

    frameworkCustomerContract = JPA.find(Contract.class, frameworkCustomerContract.getId());
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
    productMap.put("unitPrice", contractLine.getPriceDiscounted());
    productMap.put(
        "currency",
        Optional.ofNullable(contractLine.getContractVersion())
            .map(ContractVersion::getContract)
            .map(Contract::getCurrency)
            .orElse(product.getSaleCurrency()));

    return productMap;
  }

  protected Map<String, Object> getProductDataFromSupplierContract(
      ProjectTask projectTask, Product product) throws AxelorException {
    Contract frameworkSupplierContract = projectTask.getFrameworkSupplierContract();

    Map<String, Object> productMap = new HashMap<>();

    if (projectTask.getProject() == null) {
      return productMap;
    }

    if (frameworkSupplierContract == null) {
      productMap.put(
          "unitCost",
          productCompanyService.get(product, "salePrice", projectTask.getProject().getCompany()));
      return productMap;
    }

    frameworkSupplierContract = JPA.find(Contract.class, frameworkSupplierContract.getId());
    List<ContractLine> contractLines =
        frameworkSupplierContract.getCurrentContractVersion().getContractLineList().stream()
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
    productMap.put("unitCost", contractLine.getPriceDiscounted());

    return productMap;
  }

  @Override
  public Product getEmployeeProduct(ProjectTask projectTask) throws AxelorException {
    Contract frameworkCustomerContract = projectTask.getFrameworkCustomerContract();
    Contract frameworkSupplierContract = projectTask.getFrameworkSupplierContract();
    Product product =
        Optional.ofNullable(projectTask.getAssignedTo())
            .map(User::getEmployee)
            .map(Employee::getProduct)
            .orElse(null);
    if (product == null
        || (frameworkCustomerContract == null && frameworkSupplierContract == null)) {
      return product;
    } else if (frameworkCustomerContract == null || frameworkSupplierContract == null) {
      // product is not null but either frameworkCustomerContract is or frameworkSupplierContract
      // is.
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.PROJECT_EMPLOYEE_PRODUCT_NOT_IN_CONTRACT),
          projectTask.getName());
    }

    List<ContractLine> customerContractLines =
        JPA.find(Contract.class, frameworkCustomerContract.getId())
            .getCurrentContractVersion()
            .getContractLineList();
    List<ContractLine> supplierContractLines =
        JPA.find(Contract.class, frameworkSupplierContract.getId())
            .getCurrentContractVersion()
            .getContractLineList();

    if (customerContractLines.stream()
            .anyMatch(contractLine -> product.equals(contractLine.getProduct()))
        || supplierContractLines.stream()
            .anyMatch(contractLine -> product.equals(contractLine.getProduct()))) {
      return product;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BusinessProjectExceptionMessage.PROJECT_EMPLOYEE_PRODUCT_NOT_IN_CONTRACT),
        projectTask.getName());
  }

  @Override
  public String getCustomerContractDomain(ProjectTask projectTask) {
    Set<Contract> frameworkCustomerContractSet =
        projectTask.getProject().getFrameworkCustomerContractSet();
    Product product = projectTask.getProduct();
    List<String> filteredContract = getFilteredContractList(frameworkCustomerContractSet, product);
    return "id IN ( " + String.join(",", filteredContract) + ")";
  }

  @Override
  public String getSupplierContractDomain(ProjectTask projectTask) {
    Set<Contract> frameworkSupplierContractSet =
        projectTask.getProject().getFrameworkSupplierContractSet();

    Product product = projectTask.getProduct();
    List<String> filteredContract = getFilteredContractList(frameworkSupplierContractSet, product);

    return "id IN ( " + String.join(",", filteredContract) + ")";
  }

  protected List<String> getFilteredContractList(Set<Contract> frameworkContract, Product product) {
    List<String> filteredContract = new ArrayList<>();

    if (product != null) {
      filteredContract.addAll(
          frameworkContract.stream()
              .filter(contract -> contract.getCurrentContractVersion() != null)
              .filter(
                  contract -> contract.getCurrentContractVersion().getContractLineList() != null)
              .filter(
                  contract ->
                      contract.getCurrentContractVersion().getContractLineList().stream()
                          .anyMatch(contractLine -> contractLine.getProduct().equals(product)))
              .map(Contract::getId)
              .map(String::valueOf)
              .collect(Collectors.toList()));
    } else {
      filteredContract.addAll(
          frameworkContract.stream()
              .map(Contract::getId)
              .map(String::valueOf)
              .collect(Collectors.toList()));
    }

    return filteredContract;
  }
}
