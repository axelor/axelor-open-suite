package com.axelor.apps.base.rest.dto;

import com.axelor.db.Model;
import com.axelor.utils.api.ResponseStructure;

public class CheckResponseLine extends ResponseStructure {

  public static final String CHECK_TYPE_ALERT = "alert";
  public static final String CHECK_TYPE_ERROR = "error";

  private String message;
  private String checkType;
  private String modelName;

  public CheckResponseLine(Model model, String message, String checkType) {
    super(model.getVersion());
    this.message = message;
    this.checkType = checkType;
    this.modelName = model.getClass().getSimpleName();
  }

  public String getMessage() {
    return message;
  }

  public String getCheckType() {
    return checkType;
  }

  public String getModelName() {
    return modelName;
  }
}
