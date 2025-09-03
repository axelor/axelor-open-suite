package com.axelor.apps.project.rest.dto;

import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;

public class ProjectCheckListItemPutStructure extends RequestStructure {

  @NotNull protected boolean complete;

  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }
}
