/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.rpc.filter.Filter;
import java.util.List;
import java.util.Map;

public interface ClientViewService {

  public User getClientUser();

  public Map<String, Object> updateClientViewIndicators();

  /* Project */
  public List<Filter> getTotalProjectsOfUser(User user);

  public List<Filter> getNewTasksOfUser(User user);

  public List<Filter> getTasksInProgressOfUser(User user);

  public List<Filter> getTasksDueOfUser(User user);

  /* SaleOrder */
  public List<Filter> getOrdersInProgressOfUser(User user);

  public List<Filter> getQuotationsOfUser(User user);

  public List<Filter> getLastOrderOfUser(User user);

  /* StockMove */
  public List<Filter> getLastDeliveryOfUser(User user);

  public List<Filter> getNextDeliveryOfUser(User user);

  public List<Filter> getPlannedDeliveriesOfUser(User user);

  public List<Filter> getReversionsOfUser(User user);

  /* Invoice */
  public List<Filter> getOverdueInvoicesOfUser(User user);

  public List<Filter> getAwaitingInvoicesOfUser(User user);

  public List<Filter> getTotalRemainingOfUser(User user);

  public List<Filter> getRefundOfUser(User user);

  /* Ticket */
  public List<Filter> getTicketsOfUser(User user);

  public List<Filter> getCompanyTicketsOfUser(User user);

  public List<Filter> getResolvedTicketsOfUser(User user);

  public List<Filter> getLateTicketsOfUser(User user);
}
