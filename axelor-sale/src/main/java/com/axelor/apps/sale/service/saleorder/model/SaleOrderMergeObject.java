package com.axelor.apps.sale.service.saleorder.model;

/**
 * Specific object for merge process of sale orders. When merging sales order, there is a control
 * that checks if the sale orders that we try to merge shares common values on specific fields (ex:
 * company, taxNumber, etc..)
 *
 * <p>But because there are some differences in the control (some fields are mandatory and some will
 * require confirmation if different) it was needed to create a {@link SaleOrderMergeObject} in
 * order to differentiate them.
 */
public class SaleOrderMergeObject {

  /** Boolean that indicate that commonObject can be null. */
  private boolean canBeNull;

  private Object commonObject;
  private boolean existDiff = false;

  public SaleOrderMergeObject(Object commonObject, boolean canBeNull) {
    this.commonObject = commonObject;
    this.canBeNull = canBeNull;
  }

  public boolean isDifferent(Object object) {
    // If it can be null, then both null is not different
    if (commonObject == null && object == null) {
      return canBeNull ? false : true;
    }
    // If only one of them is null then it is different
    if (commonObject == null && object != null || commonObject != null && object == null) {
      return true;
    }
    return !commonObject.equals(object);
  }

  public Object getCommonObject() {
    return commonObject;
  }

  public void setCommonObject(Object commonObject) {
    this.commonObject = commonObject;
  }

  public boolean getExistDiff() {
    return existDiff;
  }

  public void setExistDiff(boolean existDiff) {
    this.existDiff = existDiff;
  }
}
