/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface MoveGroupService {

  Map<String, Object> getOnNewValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(Move move) throws AxelorException;

  Map<String, Object> getOnLoadValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrsMap(Move move) throws AxelorException;

  void checkBeforeSave(Move move) throws AxelorException;

  void onSave(Move move, boolean paymentConditionChange, boolean dateChange, boolean headerChange)
      throws AxelorException;

  Map<String, Object> getDateOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getDateOnChangeAttrsMap(
      Move move, boolean paymentConditionChange);

  Map<String, Object> getJournalOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getJournalOnChangeAttrsMap(Move move);

  Map<String, Object> getPartnerOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException;

  Map<String, Map<String, Object>> getPartnerOnChangeAttrsMap(
      Move move, boolean paymentConditionChange);

  Map<String, Object> getMoveLineListOnChangeValuesMap(
      Move move, boolean paymentConditionChange, boolean dateChange) throws AxelorException;

  Map<String, Map<String, Object>> getMoveLineListOnChangeAttrsMap(
      Move move, boolean paymentConditionChange) throws AxelorException;

  Map<String, Object> getOriginDateOnChangeValuesMap(Move move, boolean paymentConditionChange)
      throws AxelorException;

  Map<String, Map<String, Object>> getOriginDateOnChangeAttrsMap(
      Move move, boolean paymentConditionChange);

  Map<String, Object> getOriginOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Object> getPaymentConditionOnChangeValuesMap(
      Move move, boolean dateChange, boolean headerChange) throws AxelorException;

  Map<String, Map<String, Object>> getPaymentConditionOnChangeAttrsMap(Move move);

  Map<String, Object> getDescriptionOnChangeValuesMap(Move move);

  Map<String, Object> getCompanyOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getCompanyOnChangeAttrsMap(Move move) throws AxelorException;

  Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getHeaderChangeAttrsMap();

  Map<String, Object> getCurrencyOnChangeValuesMap(Move move);

  Map<String, Object> getFiscalPositionOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Object> getDateOfReversionSelectOnChangeValuesMap(
      LocalDate moveDate, int dateOfReversionSelect);

  Map<String, Object> getGenerateCounterpartOnClickValuesMap(Move move, LocalDate dueDate)
      throws AxelorException;

  Map<String, Object> getGenerateTaxLinesOnClickValuesMap(Move move) throws AxelorException;

  Map<String, Object> getApplyCutOffDatesOnClickValuesMap(
      Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate) throws AxelorException;

  Map<String, Map<String, Object>> getPartnerOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getPaymentModeOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getPartnerBankDetailsOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getTradingNameOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getWizardDefaultAttrsMap(LocalDate moveDate);
}
