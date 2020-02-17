/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.auth.db.User;
import java.util.Map;

public interface ClientViewService {

  public User getClientUser();

  public Map<String, Object> updateClientViewIndicators();

  /* Project */
  public String getTotalProjectsOfUser(User user);

  public String getNewTasksOfUser(User user);

  public String getTasksInProgressOfUser(User user);

  public String getTasksDueOfUser(User user);

  /* SaleOrder */
  public String getOrdersInProgressOfUser(User user);

  public String getQuotationsOfUser(User user);

  public String getLastOrderOfUser(User user);

  /* StockMove */
  public String getLastDeliveryOfUser(User user);

  public String getNextDeliveryOfUser(User user);

  public String getPlannedDeliveriesOfUser(User user);

  public String getReversionsOfUser(User user);

  /* Invoice */
  public String getOverdueInvoicesOfUser(User user);

  public String getAwaitingInvoicesOfUser(User user);

  public String getTotalRemainingOfUser(User user);

  public String getRefundOfUser(User user);

  /* Ticket */
  public String getTicketsOfUser(User user);

  public String getCompanyTicketsOfUser(User user);

  public String getResolvedTicketsOfUser(User user);

  public String getLateTicketsOfUser(User user);
}
