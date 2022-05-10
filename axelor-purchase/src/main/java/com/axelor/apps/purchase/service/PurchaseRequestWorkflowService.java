package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.exception.AxelorException;

public interface PurchaseRequestWorkflowService {
  /**
   * Set the purchase request status to requested.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void requestPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to accepted.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void acceptPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to purchased.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void purchasePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to refused.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void refusePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to canceled.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void cancelPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to draft.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void draftPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;
}
