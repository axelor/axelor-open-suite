package com.axelor.apps.base.rest.dto;

import com.axelor.db.Model;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class CheckResponse extends ResponseStructure {
  private Long modelId;
  private String modelName;
  private final List<CheckResponseLine> checks;
  private List<CheckResponse> otherChecks;

  public CheckResponse(Model model, List<CheckResponseLine> checks) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checks = checks;
  }

  public CheckResponse(
      Model model, List<CheckResponseLine> checks, List<CheckResponse> otherChecks) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checks = checks;
    this.otherChecks = otherChecks;
  }

  public List<CheckResponseLine> getChecks() {
    return checks;
  }

  public List<CheckResponse> getOtherChecks() {
    return otherChecks;
  }

  public Long getModelId() {
    return modelId;
  }

  public String getModelName() {
    return modelName;
  }
}
