package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class UserPermissionResponse extends ResponseStructure {

  protected final List<PermissionResponse> permissionResponseList;

  public UserPermissionResponse(int version, List<PermissionResponse> permissionResponseList) {
    super(version);
    this.permissionResponseList = permissionResponseList;
  }

  public List<PermissionResponse> getPermissionResponseList() {
    return permissionResponseList;
  }
}
