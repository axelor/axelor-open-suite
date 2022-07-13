package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import com.axelor.auth.db.User;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class InventoryPutRequest extends RequestStructure {

  @NotNull
  @Min(InventoryRepository.STATUS_DRAFT)
  @Max(InventoryRepository.STATUS_CANCELED)
  private int status;

  private Long userId;

  public InventoryPutRequest() {}

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  // Transform id to object
  public User fetchUser() {
    if (this.userId != null) {
      return ObjectFinder.find(User.class, userId, ObjectFinder.NO_VERSION);
    } else {
      return null;
    }
  }
}
