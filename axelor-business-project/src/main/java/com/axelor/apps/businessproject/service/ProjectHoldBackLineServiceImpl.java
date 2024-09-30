/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectHoldBackLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProjectHoldBackLineServiceImpl implements ProjectHoldBackLineService {

  protected final InvoiceLineRepository invoiceLineRepository;
  protected final InvoiceRepository invoiceRepository;
  protected final InvoicingProjectService invoicingProjectService;
  protected final InvoiceLineService invoiceLineService;
  protected final InvoiceLineAnalyticService invoiceLineAnalyticService;
  protected final AnalyticLineService analyticLineService;

  @Inject
  ProjectHoldBackLineServiceImpl(
      InvoiceLineRepository invoiceLineRepository,
      InvoiceRepository invoiceRepository,
      InvoicingProjectService invoicingProjectService,
      InvoiceLineService invoiceLineService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      AnalyticLineService analyticLineService) {
    this.invoiceRepository = invoiceRepository;
    this.invoiceLineRepository = invoiceLineRepository;
    this.invoicingProjectService = invoicingProjectService;
    this.invoiceLineService = invoiceLineService;
    this.invoiceLineAnalyticService = invoiceLineAnalyticService;
    this.analyticLineService = analyticLineService;
  }

  @Override
  public List<InvoiceLine> generateInvoiceLinesForReleasedHoldBacks(
      Invoice invoice, List<Integer> projectHoldBacksIds) throws AxelorException {

    Project project = invoice.getProject();
    int sequence = 0;
    List<InvoiceLine> holdBacksInvoiceLines =
        invoiceLineRepository
            .all()
            .filter(
                "self.invoice.project= :_project AND self.projectHoldBackLine.projectHoldBack.id IN :_projectHoldBacksIds AND self.invoice.statusSelect = :_ventilated AND self.isVentilatedReleasedProjectHoldBackLineInvoiceLine = false")
            .bind("_project", project)
            .bind("_projectHoldBacksIds", projectHoldBacksIds)
            .bind("_ventilated", InvoiceRepository.STATUS_VENTILATED)
            .fetch();

    if (holdBacksInvoiceLines == null || holdBacksInvoiceLines.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BusinessProjectExceptionMessage.NO_HOLD_BACK_LINES_TO_RELEASE));
    }
    List<InvoiceLine> invoiceLineList =
        createInvoiceLinesForReleasedHoldBacks(
            invoice, holdBacksInvoiceLines, invoice.getInvoiceLineList().size());
    for (InvoiceLine invoiceLine : invoiceLineList) {
      invoiceLine.setSequence(sequence);
      sequence++;

      invoiceLine.setAnalyticDistributionTemplate(project.getAnalyticDistributionTemplate());

      List<AnalyticMoveLine> analyticMoveLineList =
          invoiceLineAnalyticService.createAnalyticDistributionWithTemplate(invoiceLine);
      analyticMoveLineList.forEach(invoiceLine::addAnalyticMoveLineListItem);
      invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);

      analyticLineService.setAnalyticAccount(invoiceLine, project.getCompany());

      invoiceLineService.compute(invoice, invoiceLine);
    }

    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLinesForReleasedHoldBacks(
      Invoice invoice, List<InvoiceLine> holdBacksInvoiceLines, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (InvoiceLine holdBackInvoiceLine : holdBacksInvoiceLines) {
      invoiceLineList.addAll(
          this.createInvoiceLineForReleasedHoldBacks(
              invoice, holdBackInvoiceLine, priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLineForReleasedHoldBacks(
      Invoice invoice, InvoiceLine holdBackInvoiceLine, int priority) throws AxelorException {

    BigDecimal price = holdBackInvoiceLine.getPrice().negate();
    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            holdBackInvoiceLine.getProduct(),
            holdBackInvoiceLine.getProductName(),
            price,
            price,
            price,
            null,
            BigDecimal.ONE,
            holdBackInvoiceLine.getUnit(),
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
            invoiceLine.setRelatedProjectHoldBackLineInvoiceLine(holdBackInvoiceLine);
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<InvoiceLine> invoiceLines)
      throws AxelorException {

    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList();
    if (projectHoldBackLineList == null || projectHoldBackLineList.isEmpty()) {
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
    BigDecimal price;
    BigDecimal percentage = projectHoldBackLine.getPercentage();
    Set<Product> products = projectHoldBackLine.getProjectHoldBack().getProductsHeldBackSet();
    if (products == null || products.isEmpty()) {
      price =
          invoiceLineList.stream()
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    } else {
      price =
          invoiceLineList.stream()
              .filter(invLine -> products.contains(invLine.getProduct()))
              .map(InvoiceLine::getExTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .multiply(percentage.divide(BigDecimal.valueOf(100)));
    }

    return price.negate();
  }
}
