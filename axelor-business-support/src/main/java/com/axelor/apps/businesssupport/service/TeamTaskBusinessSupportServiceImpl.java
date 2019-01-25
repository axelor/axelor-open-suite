package com.axelor.apps.businesssupport.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.service.TeamTaskBusinessServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class TeamTaskBusinessSupportServiceImpl extends TeamTaskBusinessServiceImpl {

  @Inject
  public TeamTaskBusinessSupportServiceImpl(
      TeamTaskRepository teamTaskRepo,
      PriceListLineRepository priceListLineRepository,
      PriceListService priceListService) {
    super(teamTaskRepo, priceListLineRepository, priceListService);
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

      // Module 'business support' fields
      newTeamTask.setAssignment(TeamTaskRepository.ASSIGNMENT_PROVIDER);

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

      // Module 'business support' fields
      nextTeamTask.setAssignment(TeamTaskRepository.ASSIGNMENT_PROVIDER);
      nextTeamTask.setIsPrivate(teamTask.getIsPrivate());
      nextTeamTask.setTargetVersion(teamTask.getTargetVersion());

      teamTaskRepo.save(nextTeamTask);
      updateNextTask(nextTeamTask);
    }
  }
}
