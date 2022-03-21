package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.exception.AxelorException;

public interface PurchaseRequestWorkflowService {

  void requestPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  void acceptPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  void purchasePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  void refusePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  void cancelPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  void draftPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;
}
