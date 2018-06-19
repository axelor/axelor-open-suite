package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.db.ProductTaskTemplate;
import com.axelor.apps.businessproject.db.repo.ProductTaskTemplateRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.service.TeamTaskService;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductTaskTemplateServiceImpl implements ProductTaskTemplateService {

  protected ProductTaskTemplateRepository repository;
  protected TeamTaskService teamTaskService;
  protected TeamTaskRepository teamTaskRepository;

  @Inject
  public ProductTaskTemplateServiceImpl(
      ProductTaskTemplateRepository repository,
      TeamTaskService teamTaskService,
      TeamTaskRepository teamTaskRepository) {
    this.repository = repository;
    this.teamTaskService = teamTaskService;
    this.teamTaskRepository = teamTaskRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<TeamTask> convert(
      List<? extends TaskTemplate> templates,
      Project project,
      TeamTask parent,
      LocalDateTime startDate,
      int qty) {
    List<TeamTask> tasks = new ArrayList<>();

    for (TaskTemplate template : templates) {
      int qtyTmp = (template.getIsUniqueTaskForMultipleQuantity() ? 1 : qty);
      while (qtyTmp-- > 0) {
        LocalDateTime dateWithDelay = startDate.plusHours(template.getDelayToStart().longValue());

        TeamTask task =
            teamTaskService.create(template.getName(), project, template.getAssignedTo());
        task.setParentTask(parent);
        task.setTaskDate(dateWithDelay.toLocalDate());
        task.setTaskEndDate(
            dateWithDelay.plusHours(template.getDuration().longValue()).toLocalDate());
        task.setTotalPlannedHrs(template.getTotalPlannedHrs());
        tasks.add(teamTaskRepository.save(task));

        List<TeamTask> children =
            convert(template.getTaskTemplateList(), project, task, dateWithDelay, 1);
        tasks.addAll(children);
      }
    }

    return tasks;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void remove(ProductTaskTemplate productTaskTemplate) {
    productTaskTemplate = repository.find(productTaskTemplate.getId());
    if (productTaskTemplate != null) {
      repository.remove(productTaskTemplate);
    }
  }
}
