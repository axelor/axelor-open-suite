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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufOrder;
import java.util.List;

public interface ManufOrderWorkflowService {

  void start(ManufOrder manufOrder) throws AxelorException;

  void pause(ManufOrder manufOrder) throws AxelorException;

  void resume(ManufOrder manufOrder);

  boolean finish(ManufOrder manufOrder) throws AxelorException;

  void finishManufOrder(ManufOrder manufOrder) throws AxelorException;

  boolean partialFinish(ManufOrder manufOrder) throws AxelorException;

  void cancel(ManufOrder manufOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException;

  void allOpFinished(ManufOrder manufOrder) throws AxelorException;

  List<Partner> getOutsourcePartners(ManufOrder manufOrder) throws AxelorException;

  void setOperationOrderMaxPriority(ManufOrder manufOrder);

  boolean sendPartialFinishMail(ManufOrder manufOrder);

  boolean sendFinishedMail(ManufOrder manufOrder);
}
