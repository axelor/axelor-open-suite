package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;

public interface MoveRecordService {

  /**
   * Set the payment mode of move.
   *
   * <p>Note: This method can set paymentMode to null even if it was not before.
   *
   * @param move
   * @return Modified move
   */
  Move setPaymentMode(Move move);

  /**
   * Set the paymentCondition of move.
   *
   * <p>Note: This method can set paymentCondition to null even if it was not before.
   *
   * @param move
   * @return Modified move
   */
  Move setPaymentCondition(Move move);

  /**
   * Set the partnerBankDetails of move.
   *
   * <p>Note: This method can set partnerBankDetails to null even if it was not before.
   *
   * @param move
   * @return Modified move
   */
  Move setPartnerBankDetails(Move move);

  /**
   * Set the currency of move by using the move.partner.
   *
   * @param move
   * @return Modified move
   */
  Move setCurrencyByPartner(Move move);

  /**
   * Set the currencyCode of the move by using current currency
   *
   * @param move
   * @return Modified move
   */
  Move setCurrencyCode(Move move);

  /**
   * Set the journal of the move by using the move company
   *
   * @param move
   * @return Modified move
   */
  Move setJournal(Move move);

  /**
   * Set the functionOriginSelect of the move
   *
   * @param move
   * @return modified move
   */
  Move setFunctionalOriginSelect(Move move);
}
