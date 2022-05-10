package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;

public class PurchaseRequestWorkflowServiceImpl implements PurchaseRequestWorkflowService {

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void requestPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_REQUEST_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_REQUESTED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void acceptPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_REQUESTED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_ACCEPT_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_ACCEPTED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void purchasePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_ACCEPTED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_PURCHASE_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_PURCHASED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void refusePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_REQUESTED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_REFUSE_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_REFUSED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancelPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() == PurchaseRequestRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_CANCEL_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_CANCELED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void draftPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException {
    if (purchaseRequest.getStatusSelect() == null
        || purchaseRequest.getStatusSelect() != PurchaseRequestRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PURCHASE_REQUEST_DRAFT_WRONG_STATUS));
    }
    purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_DRAFT);
  }
}
