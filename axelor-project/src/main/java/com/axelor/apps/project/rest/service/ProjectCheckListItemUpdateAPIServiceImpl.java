package com.axelor.apps.project.rest.service;

import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.repo.ProjectCheckListItemRepository;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPutStructure;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

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
  public void updateCompleteStatus(
      ProjectCheckListItem projectCheckListItem,
      ProjectCheckListItemPutStructure projectCheckListItemPutStructure) {

    boolean isComplete = projectCheckListItemPutStructure.isComplete();
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
}
