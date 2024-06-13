package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class UserMetaPermissionRuleResponse extends ResponseStructure {

  protected final List<MetaPermissionRuleResponse> metaPermissionRuleResponses;

  public UserMetaPermissionRuleResponse(
      int version, List<MetaPermissionRuleResponse> metaPermissionResponseList) {
    super(version);
    this.metaPermissionRuleResponses = metaPermissionResponseList;
  }

  public List<MetaPermissionRuleResponse> getMetaPermissionRuleResponses() {
    return metaPermissionRuleResponses;
  }
}
