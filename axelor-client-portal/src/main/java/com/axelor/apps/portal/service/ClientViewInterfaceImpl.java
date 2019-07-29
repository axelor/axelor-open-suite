package com.axelor.apps.portal.service;

import java.util.HashMap;
import java.util.Map;

public class ClientViewInterfaceImpl implements ClientViewInterface {

  @Override
  public Map<String, Object> updateClientViewIndicators() {
    Map<String, Object> map = new HashMap<>();
    map.put("$orders", 2);
    map.put("$quotationInProgress", 4);
    map.put("$lastOrder", "19/02/2019");
    map.put("$lastDelivery", "15/01/2019");
    map.put("$nextDelivery", "28/02/2019");
    map.put("$realizedDelivery", 4);
    map.put("$overdueInvoices", 1);
    map.put("$awaitingInvoices", 1);
    map.put("$customerTickets", 2);
    map.put("$companyTickets", 105);
    map.put("$resolvedTickets", 84);
    map.put("$totalProjects", 2);
    map.put("$tasksInProgress", 5);
    map.put("$tasksDue", 8);
    map.put("$totalRemaining", 157);

    return map;
  }
}
