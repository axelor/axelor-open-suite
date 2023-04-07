package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveRecordSetService {

  /**
   * Set the payment mode of move.
   *
   * <p>Note: This method can set paymentMode to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPaymentMode(Move move);

  /**
   * Set the paymentCondition of move.
   *
   * <p>Note: This method can set paymentCondition to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPaymentCondition(Move move);

  /**
   * Set the partnerBankDetails of move.
   *
   * <p>Note: This method can set partnerBankDetails to null even if it was not before.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setPartnerBankDetails(Move move);

  /**
   * Set the currency of move by using the move.partner.
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setCurrencyByPartner(Move move);

  /**
   * Set the currencyCode of the move by using current currency
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setCurrencyCode(Move move);

  /**
   * Set the journal of the move by using the move company
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setJournal(Move move);

  /**
   * Set the functionOriginSelect of the move
   *
   * @param move
   * @return a map of modified fields
   */
  Map<String, Object> setFunctionalOriginSelect(Move move);

  void setPeriod(Move move) throws AxelorException;
}
