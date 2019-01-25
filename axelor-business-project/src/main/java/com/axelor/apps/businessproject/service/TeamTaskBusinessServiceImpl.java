/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.TeamTaskServiceProjectImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeamTaskBusinessServiceImpl extends TeamTaskServiceProjectImpl
    implements TeamTaskBusinessService {

  private PriceListLineRepository priceListLineRepository;

  private PriceListService priceListService;

  @Inject
  public TeamTaskBusinessServiceImpl(
      TeamTaskRepository teamTaskRepo,
      PriceListLineRepository priceListLineRepository,
      PriceListService priceListService) {
    super(teamTaskRepo);
    this.priceListLineRepository = priceListLineRepository;
    this.priceListService = priceListService;
  }

  @Override
  public TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo) {
    TeamTask task = create(saleOrderLine.getFullName() + "_task", project, assignedTo);
    task.setProduct(saleOrderLine.getProduct());
    task.setUnit(saleOrderLine.getUnit());
    task.setCurrency(project.getClientPartner().getCurrency());
    if (project.getPriceList() != null) {
      PriceListLine line =
          priceListLineRepository.findByPriceListAndProduct(
              project.getPriceList(), saleOrderLine.getProduct());
      if (line != null) {
        task.setUnitPrice(line.getAmount());
      }
    }
    if (task.getUnitPrice() == null) {
      task.setUnitPrice(saleOrderLine.getProduct().getSalePrice());
    }
    task.setQuantity(saleOrderLine.getQty());
    task.setSaleOrderLine(saleOrderLine);
    task.setToInvoice(
        saleOrderLine.getSaleOrder() != null
            ? saleOrderLine.getSaleOrder().getToInvoiceViaTask()
            : false);
    return task;
  }

  @Override
  public TeamTask create(
      TaskTemplate template, Project project, LocalDateTime date, BigDecimal qty) {
    TeamTask task = create(template.getName(), project, template.getAssignedTo());

    task.setTaskDate(date.toLocalDate());
    task.setTaskEndDate(date.plusHours(template.getDuration().longValue()).toLocalDate());

    BigDecimal plannedHrs = template.getTotalPlannedHrs();
    if (template.getIsUniqueTaskForMultipleQuantity() && qty.compareTo(BigDecimal.ONE) > 0) {
      plannedHrs = plannedHrs.multiply(qty);
      task.setName(task.getName() + " x" + qty.intValue());
    }
    task.setTotalPlannedHrs(plannedHrs);

    return task;
  }

  @Override
  public TeamTask updateDiscount(TeamTask teamTask) {
    PriceList priceList = teamTask.getProject().getPriceList();
    if (priceList == null) {
      this.emptyDiscounts(teamTask);
      return teamTask;
    }

    PriceListLine priceListLine = this.getPriceListLine(teamTask, priceList);
    Map<String, Object> discounts =
        priceListService.getReplacedPriceAndDiscounts(
            priceList, priceListLine, teamTask.getUnitPrice());

    if (discounts == null) {
      this.emptyDiscounts(teamTask);
    } else {
      teamTask.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
      teamTask.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      if (discounts.get("price") != null) {
        teamTask.setPriceDiscounted((BigDecimal) discounts.get("price"));
      }
    }
    return teamTask;
  }

  private void emptyDiscounts(TeamTask teamTask) {
    teamTask.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    teamTask.setDiscountAmount(BigDecimal.ZERO);
    teamTask.setPriceDiscounted(BigDecimal.ZERO);
  }

  private PriceListLine getPriceListLine(TeamTask teamTask, PriceList priceList) {

    return priceListService.getPriceListLine(
        teamTask.getProduct(), teamTask.getQuantity(), priceList);
  }

  @Override
  public TeamTask compute(TeamTask teamTask) {
    if (teamTask.getProduct() == null && teamTask.getProject() == null
        || teamTask.getUnitPrice() == null
        || teamTask.getQuantity() == null) {
      return teamTask;
    }
    BigDecimal priceDiscounted = this.computeDiscount(teamTask);
    BigDecimal exTaxTotal = this.computeAmount(teamTask.getQuantity(), priceDiscounted);

    teamTask.setPriceDiscounted(priceDiscounted);
    teamTask.setExTaxTotal(exTaxTotal);

    return teamTask;
  }

  private BigDecimal computeDiscount(TeamTask teamTask) {

    return priceListService.computeDiscount(
        teamTask.getUnitPrice(), teamTask.getDiscountTypeSelect(), teamTask.getDiscountAmount());
  }

  private BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        price
            .multiply(quantity)
            .setScale(AppSaleService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

    return amount;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TeamTask> teamTaskList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (TeamTask teamTask : teamTaskList) {
      invoiceLineList.addAll(this.createInvoiceLine(invoice, teamTask, priority * 100 + count));
      count++;
    }
    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, TeamTask teamTask, int priority)
      throws AxelorException {

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            teamTask.getProduct(),
            teamTask.getName(),
            teamTask.getUnitPrice(),
            BigDecimal.ZERO,
            teamTask.getPriceDiscounted(),
            teamTask.getDescription(),
            teamTask.getQuantity(),
            teamTask.getUnit(),
            null,
            priority,
            teamTask.getDiscountAmount(),
            teamTask.getDiscountTypeSelect(),
            teamTask.getExTaxTotal(),
            BigDecimal.ZERO,
            false,
            false,
            0) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(teamTask.getProject());
            teamTask.setInvoiceLine(invoiceLine);

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  @Override
  public void generateTasks(TeamTask teamTask, Frequency frequency) {
    List<LocalDate> taskDates =
        Beans.get(FrequencyService.class)
            .getDates(frequency, teamTask.getTaskDate(), frequency.getEndDate());

    taskDates.removeIf(date -> date.equals(teamTask.getTaskDate()));

    TeamTask lastTask = teamTask;
    for (LocalDate date : taskDates) {
      TeamTask newTeamTask = teamTaskRepo.copy(teamTask, false);
      newTeamTask.setIsFirst(false);
      newTeamTask.setHasDateOrFrequencyChanged(false);
      newTeamTask.setDoApplyToAllNextTasks(false);
      newTeamTask.setFrequency(
          Beans.get(FrequencyRepository.class).copy(teamTask.getFrequency(), false));
      newTeamTask.setTaskDate(date);
      newTeamTask.setTaskDeadline(date);
      newTeamTask.setNextTeamTask(null);

      // Module 'project' fields
      newTeamTask.setProgressSelect(0);
      newTeamTask.setTaskEndDate(date);

      // Module 'business project' fields
      // none

      teamTaskRepo.save(newTeamTask);

      lastTask.setNextTeamTask(newTeamTask);
      teamTaskRepo.save(lastTask);
      lastTask = newTeamTask;
    }
  }

  @Override
  public void updateNextTask(TeamTask teamTask) {
    TeamTask nextTeamTask = teamTask.getNextTeamTask();
    if (nextTeamTask != null) {
      nextTeamTask.setName(teamTask.getName());
      nextTeamTask.setTeam(teamTask.getTeam());
      nextTeamTask.setPriority(teamTask.getPriority());
      nextTeamTask.setStatus(teamTask.getStatus());
      nextTeamTask.setTaskDuration(teamTask.getTaskDuration());
      nextTeamTask.setAssignedTo(teamTask.getAssignedTo());
      nextTeamTask.setDescription(teamTask.getDescription());

      // Module 'project' fields
      nextTeamTask.setFullName(teamTask.getFullName());
      nextTeamTask.setProject(teamTask.getProject());
      nextTeamTask.setProjectCategory(teamTask.getProjectCategory());
      nextTeamTask.setProgressSelect(0);

      teamTask.getMembersUserSet().forEach(nextTeamTask::addMembersUserSetItem);

      nextTeamTask.setTeam(teamTask.getTeam());
      nextTeamTask.setParentTask(teamTask.getParentTask());
      nextTeamTask.setProduct(teamTask.getProduct());
      nextTeamTask.setUnit(teamTask.getUnit());
      nextTeamTask.setQuantity(teamTask.getQuantity());
      nextTeamTask.setUnitPrice(teamTask.getUnitPrice());
      nextTeamTask.setTaskEndDate(teamTask.getTaskEndDate());
      nextTeamTask.setBudgetedTime(teamTask.getBudgetedTime());
      nextTeamTask.setCurrency(teamTask.getCurrency());

      // Module 'business project' fields
      nextTeamTask.setToInvoice(teamTask.getToInvoice());
      nextTeamTask.setExTaxTotal(teamTask.getExTaxTotal());
      nextTeamTask.setDiscountTypeSelect(teamTask.getDiscountTypeSelect());
      nextTeamTask.setDiscountAmount(teamTask.getDiscountAmount());
      nextTeamTask.setPriceDiscounted(teamTask.getPriceDiscounted());
      nextTeamTask.setInvoicingType(teamTask.getInvoicingType());
      nextTeamTask.setTimeInvoicing(teamTask.getTimeInvoicing());
      nextTeamTask.setCustomerReferral(teamTask.getCustomerReferral());

      teamTaskRepo.save(nextTeamTask);
      updateNextTask(nextTeamTask);
    }
  }
}
