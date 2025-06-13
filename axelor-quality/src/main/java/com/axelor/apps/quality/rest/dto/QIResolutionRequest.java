package com.axelor.apps.quality.rest.dto;

import com.axelor.utils.api.RequestStructure;
import java.util.List;

public class QIResolutionRequest extends RequestStructure {

  protected List<QIResolutionDefaultRequest> defects;

  public List<QIResolutionDefaultRequest> getDefects() {
    return defects;
  }

  public void setDefects(List<QIResolutionDefaultRequest> defects) {
    this.defects = defects;
  }
}
