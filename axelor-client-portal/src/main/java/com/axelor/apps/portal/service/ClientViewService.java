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
  public String getOrdersOfUser(User user);

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
