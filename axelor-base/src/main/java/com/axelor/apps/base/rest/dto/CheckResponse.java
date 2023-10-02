package com.axelor.apps.base.rest.dto;

import com.axelor.db.Model;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class CheckResponse extends ResponseStructure {
  private Long modelId;
  private String modelName;
  private final List<CheckResponseLine> checkReponseLineList;
  private List<CheckResponse> modelLineCheckResponseList;

  public CheckResponse(Model model, List<CheckResponseLine> checkReponseLineList) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checkReponseLineList = checkReponseLineList;
  }

  public CheckResponse(
      Model model,
      List<CheckResponseLine> checkReponseLineList,
      List<CheckResponse> modelLineCheckResponse) {
    super(model.getVersion());
    this.modelId = model.getId();
    this.modelName = model.getClass().getSimpleName();
    this.checkReponseLineList = checkReponseLineList;
    this.modelLineCheckResponseList = modelLineCheckResponse;
  }

  public List<CheckResponseLine> getCheckReponseLineList() {
    return checkReponseLineList;
  }

  public List<CheckResponse> getModelLineCheckResponseList() {
    return modelLineCheckResponseList;
  }

  public Long getModelId() {
    return modelId;
  }

  public String getModelName() {
    return modelName;
  }
}
