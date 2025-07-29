package com.axelor.apps.project.rest.service;

import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.repo.ProjectCheckListItemRepository;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPutStructure;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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

    boolean complete = projectCheckListItemPutStructure.isComplete();
    projectCheckListItem.setCompleted(complete);

    List<ProjectCheckListItem> childItems = projectCheckListItem.getProjectCheckListItemList();

    if (childItems != null) {
      for (ProjectCheckListItem checkListItem : childItems) {
        checkListItem.setCompleted(complete);
      }
    }
    projectCheckListItemRepository.save(projectCheckListItem);
  }
}
