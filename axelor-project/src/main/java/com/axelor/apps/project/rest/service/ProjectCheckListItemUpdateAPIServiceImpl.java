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
package com.axelor.apps.project.rest.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectCheckListItemRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPostRequest;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;

public class ProjectCheckListItemUpdateAPIServiceImpl
    implements ProjectCheckListItemUpdateAPIService {

  protected ProjectCheckListItemRepository projectCheckListItemRepository;

  @Inject
  public ProjectCheckListItemUpdateAPIServiceImpl(
      ProjectCheckListItemRepository projectCheckListItemRepository) {
    this.projectCheckListItemRepository = projectCheckListItemRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void updateCompleteStatus(ProjectCheckListItem projectCheckListItem, boolean isComplete) {

    projectCheckListItem.setCompleted(isComplete);

    updateChildren(projectCheckListItem, isComplete);
    updateParentItem(projectCheckListItem, isComplete);

    projectCheckListItemRepository.save(projectCheckListItem);
  }

  protected void updateChildren(ProjectCheckListItem projectCheckListItem, boolean isComplete) {
    List<ProjectCheckListItem> childItems = projectCheckListItem.getProjectCheckListItemList();
    if (childItems != null) {
      childItems.forEach(childItem -> childItem.setCompleted(isComplete));
    }
  }

  protected void updateParentItem(ProjectCheckListItem projectCheckListItem, boolean isComplete) {
    ProjectCheckListItem parentItem = projectCheckListItem.getParentItem();

    if (parentItem == null) {
      return;
    }
    List<ProjectCheckListItem> siblings = parentItem.getProjectCheckListItemList();

    if (isComplete) {
      boolean allSiblingsCompleted = siblings.stream().allMatch(ProjectCheckListItem::getCompleted);
      if (allSiblingsCompleted) {
        parentItem.setCompleted(true);
      }
    } else {
      parentItem.setCompleted(false);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public ProjectCheckListItem createProjectCheckListItem(
      ProjectCheckListItemPostRequest requestBody) throws AxelorException {
    this.checkFields(requestBody);

    ProjectCheckListItem projectCheckListItem = new ProjectCheckListItem();
    projectCheckListItem.setTitle(requestBody.getTitle());

    Integer sequence = requestBody.getSequence();

    if (requestBody.getParentItemId() != null) {
      ProjectCheckListItem parentItem = requestBody.fetchParentItem();
      sequence = getSequence(parentItem.getProjectCheckListItemList(), sequence);

      projectCheckListItem.setParentItem(parentItem);
      parentItem.addProjectCheckListItemListItem(projectCheckListItem);

    } else if (requestBody.getProjectId() != null) {
      Project project = requestBody.fetchProject();
      sequence = getSequence(project.getProjectCheckListItemList(), sequence);
      projectCheckListItem.setProject(project);

    } else {
      ProjectTask projectTask = requestBody.fetchProjectTask();
      sequence = getSequence(projectTask.getProjectCheckListItemList(), sequence);
      projectCheckListItem.setProjectTask(projectTask);
    }

    projectCheckListItem.setSequence(sequence);
    updateCompleteStatus(projectCheckListItem, requestBody.isComplete());
    return projectCheckListItemRepository.save(projectCheckListItem);
  }

  protected int getSequence(List<ProjectCheckListItem> projectCheckListItemList, Integer sequence) {
    if (ObjectUtils.isEmpty(projectCheckListItemList)) {
      return 1;
    }
    Integer maxSeq =
        projectCheckListItemList.stream()
            .map(ProjectCheckListItem::getSequence)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);

    if (sequence != null && sequence <= maxSeq) {
      boolean seqExists =
          projectCheckListItemList.stream().anyMatch(item -> sequence.equals(item.getSequence()));
      if (seqExists) {
        projectCheckListItemList.stream()
            .filter(item -> item.getSequence() != null && item.getSequence() >= sequence)
            .forEach(item -> item.setSequence(item.getSequence() + 1));
      }
      return sequence;
    }
    return maxSeq + 1;
  }

  protected void checkFields(ProjectCheckListItemPostRequest requestBody) throws AxelorException {
    Long parentItemId = requestBody.getParentItemId();
    Long projectTaskId = requestBody.getProjectTaskId();
    Long projectId = requestBody.getProjectId();

    if (projectId == null && projectTaskId == null && parentItemId == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              ProjectExceptionMessage
                  .PROJECT_CHECK_LIST_ITEM_INVALID_PROJECT_PARENT_OR_PROJECT_TASK));
    }

    ProjectCheckListItem parentItem = requestBody.fetchParentItem();
    if (parentItem != null && parentItem.getParentItem() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_CHECK_LIST_ITEM_INVALID_PARENT));
    }

    if (parentItem == null && projectId != null && projectTaskId != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(ProjectExceptionMessage.PROJECT_CHECK_LIST_ITEM_CHOOSE_PROJECT_OR_TASK));
    }
  }
}
