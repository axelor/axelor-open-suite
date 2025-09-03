/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface MoveLineMassEntryRecordService {

  void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  void resetDebit(MoveLineMassEntry moveLine);

  void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company);

  void setCutOff(MoveLineMassEntry moveLine);

  void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException;

  void setMovePartnerBankDetails(MoveLineMassEntry moveLine, Move move);

  void setCurrencyCode(MoveLineMassEntry moveLine);

  void resetPartner(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine);

  void setMovePaymentCondition(MoveLineMassEntry moveLine, int journalTechnicalTypeSelect);

  void setMovePaymentMode(MoveLineMassEntry line, Integer technicalTypeSelect);

  void setVatSystemSelect(MoveLineMassEntry moveLine, Move move) throws AxelorException;

  void loadAccountInformation(Move move, MoveLineMassEntry moveLine) throws AxelorException;

  void setAnalytics(MoveLine newMoveLine, MoveLine moveLine);

  void setMoveStatusSelect(List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect);

  void setNextTemporaryMoveNumber(MoveLineMassEntry moveLine, Move move);

  void setPartner(MoveLineMassEntry moveLine, Move move) throws AxelorException;

  MoveLineMassEntry setInputAction(MoveLineMassEntry moveLine, Move move);

  void fillAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine);

  void fillAnalyticMoveLineList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine);

  void fillAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLine);
}
