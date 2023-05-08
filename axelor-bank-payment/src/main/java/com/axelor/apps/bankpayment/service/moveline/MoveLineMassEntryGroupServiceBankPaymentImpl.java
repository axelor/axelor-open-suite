/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.MoveLineAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryAttrsService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryGroupServiceImpl;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class MoveLineMassEntryGroupServiceBankPaymentImpl
    extends MoveLineMassEntryGroupServiceImpl {

  @Inject
  public MoveLineMassEntryGroupServiceBankPaymentImpl(
      MassEntryService massEntryService,
      MoveLineGroupService moveLineGroupService,
      MoveLineAttrsService moveLineAttrsService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineService moveLineService,
      MoveLineMassEntryAttrsService moveLineMassEntryAttrsService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineRecordService moveLineRecordService) {
    super(
        massEntryService,
        moveLineGroupService,
        moveLineAttrsService,
        moveLineDefaultService,
        moveLineComputeAnalyticService,
        moveLineService,
        moveLineMassEntryAttrsService,
        moveLineMassEntryRecordService,
        moveLineCheckService,
        moveLineInvoiceTermService,
        moveLineRecordService);
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    Map<String, Object> valuesMap = super.getOnNewValuesMap(moveLine, move);
    valuesMap.put("interbankCodeLine", moveLine.getInterbankCodeLine());
    return valuesMap;
  }
}
