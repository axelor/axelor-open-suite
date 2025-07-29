package com.axelor.apps.project.rest.service;

import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.rest.dto.ProjectCheckListItemPutStructure;

public interface ProjectCheckListItemUpdateAPIService {
  void updateCompleteStatus(
      ProjectCheckListItem projectCheckListItem,
      ProjectCheckListItemPutStructure projectCheckListItemPutStructure);
}
