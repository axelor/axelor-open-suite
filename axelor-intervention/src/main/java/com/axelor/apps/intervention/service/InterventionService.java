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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDateTime;

public interface InterventionService {
  Intervention create(CustomerRequest request) throws AxelorException;

  Intervention create(Contract contract) throws AxelorException;

  void fillFromContract(Intervention intervention, Contract contract);

  void fillFromRequest(Intervention intervention, CustomerRequest request);

  void start(Intervention intervention, LocalDateTime dateTime);

  void reschedule(Intervention intervention) throws AxelorException;

  void cancel(Intervention intervention);

  void suspend(Intervention intervention, LocalDateTime dateTime);

  LocalDateTime computeEstimatedEndDateTime(
      LocalDateTime planificationDateTime, Duration plannedInterventionDuration);

  void plan(
      Intervention intervention,
      ActionResponse response,
      User technicianUser,
      LocalDateTime planificationDateTime,
      LocalDateTime estimatedEndDateTime)
      throws AxelorException;

  void computeTag(Long interventionId);

  void finish(Intervention intervention, LocalDateTime dateTime) throws AxelorException;

  SaleOrder generateSaleOrder(Intervention intervention) throws AxelorException;

  Partner getDefaultInvoicedPartner(Intervention intervention);
}
