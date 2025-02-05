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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.businessproject.db.ProjectHoldBack;
import com.axelor.apps.businessproject.db.ProjectHoldBackATI;
import com.axelor.apps.businessproject.db.ProjectHoldBackLine;
import com.axelor.apps.businessproject.db.repo.ProjectHoldBackATIRepository;
import com.axelor.apps.businessproject.db.repo.ProjectHoldBackRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProjectHoldBackLineServiceImpl implements ProjectHoldBackLineService {

  protected final InvoiceLineRepository invoiceLineRepository;
  protected final InvoiceRepository invoiceRepository;
  protected final InvoicingProjectService invoicingProjectService;
  protected final InvoiceLineService invoiceLineService;
  protected final InvoiceLineAnalyticService invoiceLineAnalyticService;
  protected final AnalyticLineService analyticLineService;
  protected final CurrencyScaleService currencyScaleService;
  protected final InvoiceTermService invoiceTermService;
  protected final CurrencyService currencyService;
  protected final ProjectHoldBackATIRepository projectHoldBackATIRepository;
  protected final InvoiceServiceProject invoiceServiceProject;

  @Inject
  ProjectHoldBackLineServiceImpl(
      InvoiceLineRepository invoiceLineRepository,
      InvoiceRepository invoiceRepository,
      InvoicingProjectService invoicingProjectService,
      InvoiceLineService invoiceLineService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      AnalyticLineService analyticLineService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermService invoiceTermService,
      CurrencyService currencyService,
      ProjectHoldBackATIRepository projectHoldBackATIRepository,
      InvoiceServiceProject invoiceServiceProject) {
    this.invoiceRepository = invoiceRepository;
    this.invoiceLineRepository = invoiceLineRepository;
    this.invoicingProjectService = invoicingProjectService;
    this.invoiceLineService = invoiceLineService;
    this.invoiceLineAnalyticService = invoiceLineAnalyticService;
    this.analyticLineService = analyticLineService;
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermService = invoiceTermService;
    this.currencyService = currencyService;
    this.projectHoldBackATIRepository = projectHoldBackATIRepository;
    this.invoiceServiceProject = invoiceServiceProject;
  }

  public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<InvoiceLine> invoiceLines)
      throws AxelorException {

    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList().stream()
            .filter(
                hb ->
                    hb.getProjectHoldBack().getHoldBackTypeSelect()
                        == ProjectHoldBackRepository.HOLD_BACK_AS_EX_TAX_TOTAL)
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(projectHoldBackLineList)) {
      return new ArrayList<>();
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ProjectHoldBackLine projectHoldBackLine : projectHoldBackLineList) {
      invoiceLineList.addAll(
          this.createInvoiceLine(
              invoice,
              projectHoldBackLine,
              invoice.getInvoiceLineList().size() * 100 + count,
              invoiceLines));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      ProjectHoldBackLine projectHoldBackLine,
      int priority,
      List<InvoiceLine> invoiceLineList)
      throws AxelorException {

    BigDecimal price = calculateHoldBackLinePrice(invoiceLineList, projectHoldBackLine);

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct(),
            projectHoldBackLine.getProjectHoldBack().getName(),
            price,
            price,
            price,
            null,
            BigDecimal.ONE,
            projectHoldBackLine.getProjectHoldBack().getProjectHoldBackProduct().getUnit(),
            null,
            priority,
            BigDecimal.ZERO,
            0,
            null,
            null,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProjectHoldBackLine(projectHoldBackLine);
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  protected BigDecimal calculateHoldBackLinePrice(
      List<InvoiceLine> invoiceLineList, ProjectHoldBackLine projectHoldBackLine) {
    BigDecimal price = BigDecimal.ZERO;
    int holdBackTypeSelect = projectHoldBackLine.getProjectHoldBack().getHoldBackTypeSelect();
    BigDecimal percentage = projectHoldBackLine.getPercentage().divide(BigDecimal.valueOf(100));
    if (holdBackTypeSelect == ProjectHoldBackRepository.HOLD_BACK_AS_EX_TAX_TOTAL) {
      price = exTaxPrice(invoiceLineList, projectHoldBackLine, percentage);
    }
    if (holdBackTypeSelect == ProjectHoldBackRepository.HOLD_BACK_AS_IN_TAX_TOTAL) {
      price = inTaxPrice(invoiceLineList, projectHoldBackLine, percentage);
    }

    return price.negate();
  }

  protected BigDecimal exTaxPrice(
      List<InvoiceLine> invoiceLineList,
      ProjectHoldBackLine projectHoldBackLine,
      BigDecimal percentage) {
    Set<Product> products = projectHoldBackLine.getProjectHoldBack().getProductsHeldBackSet();
    return invoiceLineList.stream()
        .filter(
            invLine ->
                products == null || products.isEmpty() || products.contains(invLine.getProduct()))
        .map(InvoiceLine::getExTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .multiply(percentage);
  }

  protected BigDecimal inTaxPrice(
      List<InvoiceLine> invoiceLineList,
      ProjectHoldBackLine projectHoldBackLine,
      BigDecimal percentage) {
    Set<Product> products = projectHoldBackLine.getProjectHoldBack().getProductsHeldBackSet();
    return invoiceLineList.stream()
        .filter(
            invLine ->
                products == null || products.isEmpty() || products.contains(invLine.getProduct()))
        .map(InvoiceLine::getInTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .multiply(percentage);
  }

  @Override
  public void generateHoldBackATIs(Invoice invoice) throws AxelorException {
    invoice.setProjectHoldBackATIList(this.createHoldBackATIsFromProject(invoice));
    calculateHoldBacksTotal(invoice);
  }

  protected void calculateHoldBacksTotal(Invoice invoice) throws AxelorException {

    BigDecimal holdBacksTotal = BigDecimal.ZERO;
    BigDecimal companyHoldBacksTotal = BigDecimal.ZERO;
    List<ProjectHoldBackATI> projectHoldBackATIList = invoice.getProjectHoldBackATIList();
    if (CollectionUtils.isEmpty(projectHoldBackATIList)) {
      return;
    }
    for (ProjectHoldBackATI projectHoldBackATI : projectHoldBackATIList) {

      holdBacksTotal =
          currencyScaleService.getScaledValue(
              invoice, holdBacksTotal.add(projectHoldBackATI.getAmount()));
      companyHoldBacksTotal =
          currencyScaleService.getScaledValue(
              invoice,
              companyHoldBacksTotal.add(
                  invoiceServiceProject.getAmountInCompanyCurrency(
                      projectHoldBackATI.getAmount(), invoice)));
    }

    invoice.setHoldBacksTotal(holdBacksTotal);
    invoice.setCompanyHoldBacksTotal(companyHoldBacksTotal);
  }

  protected List<ProjectHoldBackATI> createHoldBackATIsFromProject(Invoice invoice)
      throws AxelorException {
    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList().stream()
            .filter(
                hb ->
                    hb.getProjectHoldBack().getHoldBackTypeSelect()
                        == ProjectHoldBackRepository.HOLD_BACK_AS_IN_TAX_TOTAL)
            .collect(Collectors.toList());
    invoice.setProjectHoldBackLineSet(new HashSet<>(projectHoldBackLineList));
    return createHoldBackATIs(invoice, projectHoldBackLineList);
  }

  protected List<ProjectHoldBackATI> createHoldBackATIs(
      Invoice invoice, List<ProjectHoldBackLine> projectHoldBackLineList) throws AxelorException {
    List<ProjectHoldBackATI> projectHoldBackATIList = new ArrayList<>();
    if (CollectionUtils.isEmpty(projectHoldBackLineList)) {
      return projectHoldBackATIList;
    }
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return projectHoldBackATIList;
    }

    Map<ProjectHoldBack, List<ProjectHoldBackLine>> groupedByHoldBack =
        projectHoldBackLineList.stream()
            .collect(Collectors.groupingBy(ProjectHoldBackLine::getProjectHoldBack));
    for (Map.Entry<ProjectHoldBack, List<ProjectHoldBackLine>> entry :
        groupedByHoldBack.entrySet()) {
      ProjectHoldBack holdBack = entry.getKey();
      List<ProjectHoldBackLine> holdBackLines = entry.getValue();

      ProjectHoldBackATI projectHoldBackATI = new ProjectHoldBackATI();
      projectHoldBackATI.setProjectHoldBack(holdBack);
      projectHoldBackATI.setName(holdBack.getName());
      projectHoldBackATI.setInvoice(invoice);
      BigDecimal amount = BigDecimal.ZERO;
      BigDecimal percentage = BigDecimal.ZERO;
      for (ProjectHoldBackLine line : holdBackLines) {
        percentage = percentage.add(line.getPercentage());
        amount = amount.add(calculateHoldBackLinePrice(invoiceLineList, line));
      }
      projectHoldBackATI.setPercentage(percentage);
      projectHoldBackATI.setAmount(amount);
      projectHoldBackATI.setCompanyAmount(
          invoiceServiceProject.getAmountInCompanyCurrency(amount, invoice));
      projectHoldBackATIList.add(projectHoldBackATI);
    }
    return projectHoldBackATIList;
  }

  @Override
  public void updateHoldBackATI(Invoice invoice) throws AxelorException {
    invoice.getProjectHoldBackATIList().clear();
    invoice
        .getProjectHoldBackATIList()
        .addAll(createHoldBackATIs(invoice, new ArrayList<>(invoice.getProjectHoldBackLineSet())));
    calculateHoldBacksTotal(invoice);
  }
}
