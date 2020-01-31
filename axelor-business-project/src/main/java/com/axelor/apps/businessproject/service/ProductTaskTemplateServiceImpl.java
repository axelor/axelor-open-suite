/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.businessproject.db.ProductTaskTemplate;
import com.axelor.apps.businessproject.db.repo.ProductTaskTemplateRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductTaskTemplateServiceImpl implements ProductTaskTemplateService {

  protected ProductTaskTemplateRepository repository;
  protected TeamTaskBusinessService teamTaskBusinessService;
  protected TeamTaskRepository teamTaskRepository;

  @Inject
  public ProductTaskTemplateServiceImpl(
      ProductTaskTemplateRepository repository,
      TeamTaskBusinessService teamTaskBusinessService,
      TeamTaskRepository teamTaskRepository) {
    this.repository = repository;
    this.teamTaskBusinessService = teamTaskBusinessService;
    this.teamTaskRepository = teamTaskRepository;
  }

  @Override
  @Transactional
  public List<TeamTask> convert(
      List<? extends TaskTemplate> templates,
      Project project,
      TeamTask parent,
      LocalDateTime startDate,
      BigDecimal qty) {
    List<TeamTask> tasks = new ArrayList<>();

    for (TaskTemplate template : templates) {
      BigDecimal qtyTmp = (template.getIsUniqueTaskForMultipleQuantity() ? BigDecimal.ONE : qty);

      while (qtyTmp.signum() > 0) {
        LocalDateTime dateWithDelay = startDate.plusHours(template.getDelayToStart().longValue());

        TeamTask task = teamTaskBusinessService.create(template, project, dateWithDelay, qty);
        task.setParentTask(parent);
        tasks.add(teamTaskRepository.save(task));

        // Only parent task can have multiple quantities
        List<TeamTask> children =
            convert(template.getTaskTemplateList(), project, task, dateWithDelay, BigDecimal.ONE);
        tasks.addAll(children);

        qtyTmp = qtyTmp.subtract(BigDecimal.ONE);
      }
    }

    return tasks;
  }

  @Override
  @Transactional
  public void remove(ProductTaskTemplate productTaskTemplate) {
    productTaskTemplate = repository.find(productTaskTemplate.getId());
    if (productTaskTemplate != null) {
      repository.remove(productTaskTemplate);
    }
  }
}
