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
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.businessproject.db.ProjectHoldBackLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.businessproject.db.ProjectHoldBackATI;
import com.axelor.apps.businessproject.db.repo.ProjectHoldBackATIRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.businessproject.db.ProjectHoldBack;
import com.axelor.apps.businessproject.db.repo.ProjectHoldBackRepository;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
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

  @Override
  public List<InvoiceLine> generateInvoiceLinesForReleasedHoldBacks(
      Invoice invoice, List<Integer> projectHoldBacksIds) throws AxelorException {

    Project project = invoice.getProject();
    int sequence = 0;
    List<InvoiceLine> invoiceLineList = new ArrayList<>();

    invoiceLineList.addAll(
        generateInvoiceLinesForReleasedInTaxTotalHoldBacks(invoice, projectHoldBacksIds, project));
    invoiceLineList.addAll(
        generateInvoiceLinesForReleasedExTaxTotalHoldBacks(invoice, projectHoldBacksIds, project));

    if (CollectionUtils.isEmpty(invoiceLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BusinessProjectExceptionMessage.NO_HOLD_BACK_LINES_TO_RELEASE));
    }

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

  protected List<InvoiceLine> generateInvoiceLinesForReleasedExTaxTotalHoldBacks(
      Invoice invoice, List<Integer> projectHoldBacksIds, Project project) throws AxelorException {
    List<InvoiceLine> holdBacksInvoiceLines =
        invoiceLineRepository
            .all()
            .filter(
                "self.invoice.project= :_project AND self.projectHoldBackLine.projectHoldBack.id IN :_projectHoldBacksIds AND self.invoice.statusSelect = :_ventilated AND self.isVentilatedReleasedProjectHoldBackLineInvoiceLine = false")
            .bind("_project", project)
            .bind("_projectHoldBacksIds", projectHoldBacksIds)
            .bind("_ventilated", InvoiceRepository.STATUS_VENTILATED)
            .fetch();

    return this.createInvoiceLinesForReleasedExTaxHoldBacks(
        invoice, holdBacksInvoiceLines, invoice.getInvoiceLineList().size());
  }

  protected List<InvoiceLine> generateInvoiceLinesForReleasedInTaxTotalHoldBacks(
      Invoice invoice, List<Integer> projectHoldBacksIds, Project project) throws AxelorException {
    List<ProjectHoldBackATI> projectHoldBackATIS =
        projectHoldBackATIRepository
            .all()
            .filter(
                "self.invoice.project= :_project AND self.projectHoldBack.id IN :_projectHoldBacksIds AND self.invoice.statusSelect = :_ventilated AND self.isVentilatedProjectHoldBackATI = false")
            .bind("_project", project)
            .bind("_projectHoldBacksIds", projectHoldBacksIds)
            .bind("_ventilated", InvoiceRepository.STATUS_VENTILATED)
            .fetch();
    projectHoldBackATIRepository
        .all()
        .filter(
            "self.invoice.project= :_project AND self.projectHoldBack.id IN :_projectHoldBacksIds AND self.invoice.statusSelect = :_ventilated AND self.isVentilatedProjectHoldBackATI = false")
        .bind("_project", project)
        .bind("_projectHoldBacksIds", projectHoldBacksIds)
        .bind("_ventilated", InvoiceRepository.STATUS_VENTILATED)
        .fetch();

    return this.createInvoiceLinesForReleasedInTaxHoldBacks(
        invoice, projectHoldBackATIS, invoice.getInvoiceLineList().size());
  }

  protected List<InvoiceLine> createInvoiceLinesForReleasedExTaxHoldBacks(
      Invoice invoice, List<InvoiceLine> holdBacksInvoiceLines, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (InvoiceLine holdBackInvoiceLine : holdBacksInvoiceLines) {
      invoiceLineList.addAll(
          this.createInvoiceLineForReleasedExTaxHoldBacks(
              invoice,
              holdBackInvoiceLine.getProjectHoldBackLine().getProjectHoldBack(),
              holdBackInvoiceLine.getPrice().negate(),
              holdBackInvoiceLine,
              priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLinesForReleasedInTaxHoldBacks(
      Invoice invoice, List<ProjectHoldBackATI> projectHoldBackATIS, int priority)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ProjectHoldBackATI projectHoldBackATI : projectHoldBackATIS) {
      invoiceLineList.addAll(
          this.createInvoiceLineForReleasedInTaxHoldBacks(
              invoice,
              projectHoldBackATI.getProjectHoldBack(),
              projectHoldBackATI.getAmount().negate(),
              projectHoldBackATI,
              priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  protected List<InvoiceLine> createInvoiceLineForReleasedExTaxHoldBacks(
      Invoice invoice,
      ProjectHoldBack holdBack,
      BigDecimal price,
      InvoiceLine holdBackInvoiceLine,
      int priority)
      throws AxelorException {

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            holdBack.getProjectHoldBackProduct(),
            holdBack.getProjectHoldBackProduct().getName(),
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
            invoiceLine.setProductName(
                String.format(
                    I18n.get("%s %s%% - %s of %s"),
                    holdBackInvoiceLine.getProjectHoldBackLine().getProjectHoldBack().getName(),
                    holdBackInvoiceLine.getProjectHoldBackLine().getPercentage(),
                    holdBackInvoiceLine.getInvoice().getInvoiceId(),
                    holdBackInvoiceLine.getInvoice().getInvoiceDate()));
            invoiceLine.setRelatedProjectHoldBackLineInvoiceLine(holdBackInvoiceLine);
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  protected List<InvoiceLine> createInvoiceLineForReleasedInTaxHoldBacks(
      Invoice invoice,
      ProjectHoldBack holdBack,
      BigDecimal price,
      ProjectHoldBackATI projectHoldBackATI,
      int priority)
      throws AxelorException {

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            holdBack.getProjectHoldBackProduct(),
            holdBack.getProjectHoldBackProduct().getName(),
            price,
            price,
            price,
            null,
            BigDecimal.ONE,
            null,
            null,
            priority,
            BigDecimal.ZERO,
            0,
            price,
            price,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setTaxLineSet(new HashSet<>());

            if (projectHoldBackATI != null) {
              invoiceLine.setProductName(
                  String.format(
                      I18n.get("%s %s%% - %s of %s"),
                      projectHoldBackATI.getProjectHoldBack().getName(),
                      projectHoldBackATI.getProjectHoldBack().getDefaultPercentage(),
                      projectHoldBackATI.getInvoice().getInvoiceId(),
                      projectHoldBackATI.getInvoice().getInvoiceDate()));
              invoiceLine.setRelatedProjectHoldBackATI(projectHoldBackATI);
            }
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
    invoice.setProjectHoldBackATIList(this.createHoldBackATIs(invoice));
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

  protected List<ProjectHoldBackATI> createHoldBackATIs(Invoice invoice) throws AxelorException {
    List<ProjectHoldBackATI> projectHoldBackATIList = new ArrayList<>();
    List<ProjectHoldBackLine> projectHoldBackLineList =
        invoice.getProject().getProjectHoldBackLineList().stream()
            .filter(
                hb ->
                    hb.getProjectHoldBack().getHoldBackTypeSelect()
                        == ProjectHoldBackRepository.HOLD_BACK_AS_IN_TAX_TOTAL)
            .collect(Collectors.toList());
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
      for (ProjectHoldBackLine line : holdBackLines) {
        amount = amount.add(calculateHoldBackLinePrice(invoiceLineList, line));
      }
      projectHoldBackATI.setAmount(amount);
      projectHoldBackATI.setCompanyAmount(
          invoiceServiceProject.getAmountInCompanyCurrency(amount, invoice));
      projectHoldBackATIList.add(projectHoldBackATI);
    }
    return projectHoldBackATIList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Map<String, Object>> loadProjectRelatedHoldBacks(Project project) {
    List<Map<String, Object>> holdBacksToInvoice = new ArrayList<>();
    List<ProjectHoldBackLine> projectHoldBackLineList = project.getProjectHoldBackLineList();
    if (CollectionUtils.isNotEmpty(projectHoldBackLineList)) {
      List<ProjectHoldBack> projectHoldBacks =
          projectHoldBackLineList.stream()
              .map(ProjectHoldBackLine::getProjectHoldBack)
              .distinct()
              .collect(Collectors.toList());
      this.loadExTaxTotalProjectRelatedHoldBacks(holdBacksToInvoice, project, projectHoldBacks);
      this.loadIxTaxTotalProjectRelatedHoldBacks(holdBacksToInvoice, project, projectHoldBacks);
    }
    return holdBacksToInvoice;
  }

  protected void loadExTaxTotalProjectRelatedHoldBacks(
      List<Map<String, Object>> holdBacksToInvoice,
      Project project,
      List<ProjectHoldBack> projectHoldBacks) {

    Query q =
        JPA.em()
            .createQuery(
                "SELECT il.projectHoldBackLine.projectHoldBack.id AS projectHoldBack, "
                    + "SUM(il.exTaxTotal) AS totalSum, "
                    + "COUNT(il) AS lineCount "
                    + "FROM InvoiceLine il "
                    + "WHERE il.invoice.project = :project "
                    + "AND il.projectHoldBackLine.projectHoldBack IN :_projectHoldBackList "
                    + "AND il.invoice.statusSelect = :statusVentilated "
                    + "AND il.isVentilatedReleasedProjectHoldBackLineInvoiceLine = false "
                    + "GROUP BY il.projectHoldBackLine.projectHoldBack.id");
    q.setParameter("project", project);
    q.setParameter("_projectHoldBackList", projectHoldBacks);
    q.setParameter("statusVentilated", InvoiceRepository.STATUS_VENTILATED);
    List<Object[]> lines = q.getResultList();
    this.creatProjectRelatedHoldBacks(holdBacksToInvoice, lines, projectHoldBacks);
  }

  protected void loadIxTaxTotalProjectRelatedHoldBacks(
      List<Map<String, Object>> holdBacksToInvoice,
      Project project,
      List<ProjectHoldBack> projectHoldBackList) {

    Query q =
        JPA.em()
            .createQuery(
                "SELECT phati.projectHoldBack.id AS projectHoldBack, "
                    + "SUM(phati.amount) AS totalSum, "
                    + "COUNT(phati) AS lineCount "
                    + "FROM ProjectHoldBackATI phati "
                    + "WHERE phati.invoice.project = :project "
                    + "AND phati.projectHoldBack IN :_projectHoldBackList "
                    + "AND phati.invoice.statusSelect = :statusVentilated "
                    + "AND phati.isVentilatedProjectHoldBackATI = false "
                    + "GROUP BY phati.projectHoldBack.id");
    q.setParameter("project", project);
    q.setParameter("_projectHoldBackList", projectHoldBackList);
    q.setParameter("statusVentilated", InvoiceRepository.STATUS_VENTILATED);
    List<Object[]> lines = q.getResultList();
    this.creatProjectRelatedHoldBacks(holdBacksToInvoice, lines, projectHoldBackList);
  }

  protected void creatProjectRelatedHoldBacks(
      List<Map<String, Object>> holdBacksToInvoice,
      List<Object[]> lines,
      List<ProjectHoldBack> projectHoldBackList) {
    if (CollectionUtils.isEmpty(lines)) {
      return;
    }
    for (Object[] result : lines) {
      ProjectHoldBack projectHoldBack =
          projectHoldBackList.stream()
              .filter(hb -> hb.getId() == result[0])
              .findFirst()
              .orElse(null);
      BigDecimal totalSum = (BigDecimal) result[1];
      long lineCount = (long) result[2];
      assert projectHoldBack != null;
      holdBacksToInvoice.add(
          this.creatProjectRelatedHoldBack(projectHoldBack, totalSum, lineCount));
    }
  }

  protected Map<String, Object> creatProjectRelatedHoldBack(
      ProjectHoldBack projectHoldBack, BigDecimal totalSum, long lineCount) {

    Map<String, Object> projectRelatedHoldBack = new HashMap<>();
    projectRelatedHoldBack.put("name", projectHoldBack.getName());
    projectRelatedHoldBack.put("holdBackTypeSelect", projectHoldBack.getHoldBackTypeSelect());
    projectRelatedHoldBack.put("$linesNumber", lineCount);
    projectRelatedHoldBack.put("$linesTotalAmount", totalSum);
    projectRelatedHoldBack.put("holdBackId", projectHoldBack.getId());
    return projectRelatedHoldBack;
  }
}
