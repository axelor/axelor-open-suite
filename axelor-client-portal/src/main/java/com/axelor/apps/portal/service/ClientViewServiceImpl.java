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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.auth.db.User;
import com.axelor.db.JpaSecurity;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientViewServiceImpl implements ClientViewService {

  protected SaleOrderRepository saleOrderRepo;
  protected StockMoveRepository stockMoveRepo;
  protected ProjectRepository projectRepo;
  protected TicketRepository ticketRepo;
  protected InvoiceRepository invoiceRepo;
  protected TeamTaskRepository teamTaskRepo;
  protected JpaSecurity security;
  protected AppService appService;

  protected static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  static final String CLIENT_PORTAL_NO_DATE = /*$$(*/ "None" /*)*/;

  @Inject
  public ClientViewServiceImpl(
      SaleOrderRepository saleOrderRepo,
      StockMoveRepository stockMoveRepo,
      ProjectRepository projectRepo,
      TicketRepository ticketRepo,
      InvoiceRepository invoiceRepo,
      TeamTaskRepository teamTaskRepo,
      JpaSecurity jpaSecurity,
      AppService appService) {
    this.saleOrderRepo = saleOrderRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.projectRepo = projectRepo;
    this.ticketRepo = ticketRepo;
    this.invoiceRepo = invoiceRepo;
    this.teamTaskRepo = teamTaskRepo;
    this.security = jpaSecurity;
    this.appService = appService;
  }

  @Override
  public User getClientUser() {
    return Beans.get(UserService.class).getUser();
  }

  @Override
  public Map<String, Object> updateClientViewIndicators() {
    Map<String, Object> map = new HashMap<>();
    User user = getClientUser();
    /* SaleOrder */
    map.put("$ordersInProgress", getOrdersInProgressIndicator(user));
    map.put("$myQuotation", getQuotationsIndicator(user));
    map.put("$lastOrder", getLastOrderIndicator(user));
    /* StockMove */
    map.put("$lastDelivery", getLastDeliveryIndicator(user));
    map.put("$nextDelivery", getNextDeliveryIndicator(user));
    map.put("$plannedDeliveries", getPlannedDeliveriesIndicator(user));
    map.put("$myReversions", getReversionsIndicator(user));
    /* Invoice */
    map.put("$overdueInvoices", getOverdueInvoicesIndicator(user));
    map.put("$awaitingInvoices", getAwaitingInvoicesIndicator(user));
    map.put("$totalRemaining", getTotalRemainingIndicator(user));
    map.put("$myRefund", getRefundIndicator(user));
    /* Helpdesk */
    map.put("$customerTickets", getCustomerTicketsIndicator(user));
    map.put("$companyTickets", getCompanyTicketsIndicator(user));
    map.put("$resolvedTickets", getResolvedTicketsIndicator(user));
    map.put("$lateTickets", getLateTicketsIndicator(user));
    /* Project */
    map.put("$totalProjects", getTotalProjectsIndicator(user));
    map.put("$tasksInCompleted", getTasksInCompletedIndicator(user));
    map.put("$tasksDue", getTasksDueIndicator(user));
    return map;
  }

  /* SaleOrder Indicators */
  protected Integer getOrdersInProgressIndicator(User user) {
    List<Filter> filters = getOrdersInProgressOfUser(user);
    List<SaleOrder> saleOrderList = Filter.and(filters).build(SaleOrder.class).fetch();
    return !saleOrderList.isEmpty() ? saleOrderList.size() : 0;
  }

  protected Integer getQuotationsIndicator(User user) {
    List<Filter> filters = getQuotationsOfUser(user);
    List<SaleOrder> saleOrderList = Filter.and(filters).build(SaleOrder.class).fetch();
    return !saleOrderList.isEmpty() ? saleOrderList.size() : 0;
  }

  protected String getLastOrderIndicator(User user) {
    List<Filter> filters = getLastOrderOfUser(user);
    SaleOrder saleOrder =
        Filter.and(filters).build(SaleOrder.class).order("-confirmationDateTime").fetchOne();
    if (saleOrder == null) {
      return I18n.get(CLIENT_PORTAL_NO_DATE);
    }
    return saleOrder.getConfirmationDateTime() != null
        ? saleOrder.getConfirmationDateTime().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  /* StockMove Indicators */
  protected String getLastDeliveryIndicator(User user) {
    List<Filter> filters = getLastDeliveryOfUser(user);
    StockMove stockMove = Filter.and(filters).build(StockMove.class).order("-realDate").fetchOne();
    if (stockMove == null) {
      return I18n.get(CLIENT_PORTAL_NO_DATE);
    }
    return stockMove.getRealDate() != null
        ? stockMove.getRealDate().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  protected String getNextDeliveryIndicator(User user) {
    List<Filter> filters = getNextDeliveryOfUser(user);
    StockMove stockMove =
        Filter.and(filters).build(StockMove.class).order("estimatedDate").fetchOne();
    if (stockMove == null) {
      return I18n.get(CLIENT_PORTAL_NO_DATE);
    }
    return stockMove.getEstimatedDate() != null
        ? stockMove.getEstimatedDate().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  protected Integer getPlannedDeliveriesIndicator(User user) {
    List<Filter> filters = getPlannedDeliveriesOfUser(user);
    List<StockMove> stockMoveList = Filter.and(filters).build(StockMove.class).fetch();
    return !stockMoveList.isEmpty() ? stockMoveList.size() : 0;
  }

  protected Integer getReversionsIndicator(User user) {
    List<Filter> filters = getReversionsOfUser(user);
    List<StockMove> stockMoveList = Filter.and(filters).build(StockMove.class).fetch();
    return !stockMoveList.isEmpty() ? stockMoveList.size() : 0;
  }

  /* Invoice Indicators */
  protected Integer getOverdueInvoicesIndicator(User user) {
    List<Filter> filters = getOverdueInvoicesOfUser(user);
    List<Invoice> invoiceList = Filter.and(filters).build(Invoice.class).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  protected Integer getAwaitingInvoicesIndicator(User user) {
    List<Filter> filters = getAwaitingInvoicesOfUser(user);
    List<Invoice> invoiceList = Filter.and(filters).build(Invoice.class).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  protected String getTotalRemainingIndicator(User user) {
    List<Filter> filters = getTotalRemainingOfUser(user);
    List<Invoice> invoiceList = Filter.and(filters).build(Invoice.class).fetch();
    if (!invoiceList.isEmpty()) {
      BigDecimal total =
          invoiceList.stream()
              .map(Invoice::getAmountRemaining)
              .reduce((x, y) -> x.add(y))
              .orElse(BigDecimal.ZERO);
      return total.toString() + invoiceList.get(0).getCurrency().getSymbol();
    }
    return BigDecimal.ZERO.toString();
  }

  protected Integer getRefundIndicator(User user) {
    List<Filter> filters = getRefundOfUser(user);
    List<Invoice> invoiceList = Filter.and(filters).build(Invoice.class).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  /* Helpdesk Indicators */
  protected Integer getCustomerTicketsIndicator(User user) {
    List<Filter> filters = getTicketsOfUser(user);
    List<Ticket> ticketList = Filter.and(filters).build(Ticket.class).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Integer getCompanyTicketsIndicator(User user) {
    List<Filter> filters = getCompanyTicketsOfUser(user);
    List<Ticket> ticketList = Filter.and(filters).build(Ticket.class).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Integer getResolvedTicketsIndicator(User user) {
    List<Filter> filters = getResolvedTicketsOfUser(user);
    List<Ticket> ticketList = Filter.and(filters).build(Ticket.class).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Object getLateTicketsIndicator(User user) {
    List<Filter> filters = getLateTicketsOfUser(user);
    List<Ticket> ticketList = Filter.and(filters).build(Ticket.class).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  /* Project Indicators */
  protected Integer getTotalProjectsIndicator(User user) {
    List<Filter> filters = getTotalProjectsOfUser(user);
    List<Project> projectList = Filter.and(filters).build(Project.class).fetch();
    return !projectList.isEmpty() ? projectList.size() : 0;
  }

  protected Integer getTasksInCompletedIndicator(User user) {
    List<Filter> filters = getTasksInCompletedOfUser(user);
    List<TeamTask> teamTaskList = Filter.and(filters).build(TeamTask.class).fetch();
    return !teamTaskList.isEmpty() ? teamTaskList.size() : 0;
  }

  protected Integer getTasksDueIndicator(User user) {
    List<Filter> filters = getTasksInCompletedOfUser(user);
    List<TeamTask> teamTaskList = Filter.and(filters).build(TeamTask.class).fetch();
    return !teamTaskList.isEmpty() ? teamTaskList.size() : 0;
  }

  /* SaleOrder Query */
  @Override
  public List<Filter> getOrdersInProgressOfUser(User user) {

    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, SaleOrder.class);
    Filter filter =
        new JPQLFilter(
            "self.clientPartner.id = "
                + user.getPartner().getId()
                + " AND self.statusSelect = "
                + SaleOrderRepository.STATUS_ORDER_CONFIRMED);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getQuotationsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, SaleOrder.class);
    Filter filter =
        new JPQLFilter(
            "self.clientPartner.id = "
                + user.getPartner().getId()
                + " AND self.statusSelect IN ("
                + SaleOrderRepository.STATUS_DRAFT_QUOTATION
                + ","
                + SaleOrderRepository.STATUS_FINALIZED_QUOTATION
                + ")");

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }

    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getLastOrderOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, SaleOrder.class);
    Filter filter =
        new JPQLFilter(
            "self.clientPartner.id = "
                + user.getPartner().getId()
                + " AND self.statusSelect = "
                + SaleOrderRepository.STATUS_ORDER_COMPLETED);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  /* StockMove Query */
  @Override
  public List<Filter> getLastDeliveryOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, StockMove.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.typeSelect = "
                + StockMoveRepository.TYPE_OUTGOING
                + " AND self.statusSelect = "
                + StockMoveRepository.STATUS_REALIZED
                + " AND self.isReversion != true");

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    if (filterFromPermission != null) {
      filter = Filter.and(filter, filterFromPermission);
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getNextDeliveryOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, StockMove.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.typeSelect = "
                + StockMoveRepository.TYPE_OUTGOING
                + " AND self.statusSelect = "
                + StockMoveRepository.STATUS_PLANNED
                + " AND self.isReversion != true");

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  private void addPermissionFilter(List<Filter> filters, Filter filterFromPermission) {
    if (filterFromPermission != null) {
      filters.add(filterFromPermission);
    }
  }

  @Override
  public List<Filter> getPlannedDeliveriesOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, StockMove.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.typeSelect = "
                + StockMoveRepository.TYPE_OUTGOING
                + " AND self.statusSelect = "
                + StockMoveRepository.STATUS_PLANNED
                + " AND self.isReversion != true");
    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getReversionsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, StockMove.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.typeSelect = "
                + StockMoveRepository.TYPE_OUTGOING
                + " AND self.isReversion = true");

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  /* Invoice Query */
  @Override
  public List<Filter> getOverdueInvoicesOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Invoice.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.dueDate < current_date() "
                + " AND self.amountRemaining != 0 AND self.statusSelect != "
                + InvoiceRepository.STATUS_DRAFT
                + " AND self.statusSelect != "
                + InvoiceRepository.STATUS_CANCELED);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getAwaitingInvoicesOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Invoice.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.amountRemaining != 0 AND self.statusSelect != "
                + InvoiceRepository.STATUS_DRAFT
                + " AND self.statusSelect != "
                + InvoiceRepository.STATUS_CANCELED);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getTotalRemainingOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Invoice.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.amountRemaining != 0 AND self.statusSelect != "
                + InvoiceRepository.STATUS_DRAFT
                + " AND self.statusSelect != "
                + InvoiceRepository.STATUS_CANCELED);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);

    return filters;
  }

  @Override
  public List<Filter> getRefundOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Invoice.class);
    Filter filter =
        new JPQLFilter(
            "self.partner.id = "
                + user.getPartner().getId()
                + " AND self.operationTypeSelect = "
                + InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);

    if (user.getActiveCompany() != null) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);

    return filters;
  }

  /* Helpdesk Query */
  @Override
  public List<Filter> getTicketsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Ticket.class);
    Filter filter =
        new JPQLFilter(
            "self.customer.id = "
                + user.getPartner().getId()
                + " AND self.assignedToUser.id = "
                + user.getId());
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getCompanyTicketsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Ticket.class);
    Filter filter =
        new JPQLFilter(
            "self.customer.id = "
                + user.getPartner().getId()
                + " AND self.assignedToUser.id = "
                + user.getActiveCompany().getId());
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getResolvedTicketsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Ticket.class);
    Filter filter =
        new JPQLFilter(
            "self.customer.id = "
                + user.getPartner().getId()
                + " AND self.assignedToUser.id = "
                + user.getId()
                + " AND self.statusSelect IN ("
                + TicketRepository.STATUS_RESOLVED
                + ", "
                + TicketRepository.STATUS_CLOSED
                + ")");
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getLateTicketsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Ticket.class);
    Filter filter =
        new JPQLFilter(
            "self.customer.id = "
                + user.getPartner().getId()
                + " AND self.assignedToUser.id = "
                + user.getId()
                + " AND ((self.endDateT != null AND self.endDateT > self.deadlineDateT) "
                + " OR (self.endDateT = null and self.deadlineDateT < current_date() ) )");
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  /* Project Query */
  @Override
  public List<Filter> getTotalProjectsOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, Project.class);
    Filter filter =
        new JPQLFilter(
            "self.clientPartner.id = "
                + user.getPartner().getId()
                + " AND self.projectStatus.isCompleted = false");
    if (user.getActiveCompany() != null && appService.isApp("business-project")) {
      filter =
          Filter.and(
              filter, new JPQLFilter(" self.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);

    return filters;
  }

  @Override
  public List<Filter> getTasksInCompletedOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter filterFromPermission = security.getFilter(JpaSecurity.CAN_READ, TeamTask.class);
    Filter filter =
        new JPQLFilter(
            "self.taskStatus.isCompleted = false"
                + " AND self.typeSelect = '"
                + TeamTaskRepository.TYPE_TASK
                + "' AND self.project.clientPartner.id = "
                + user.getPartner().getId());
    if (user.getActiveCompany() != null && appService.isApp("business-project")) {
      filter =
          Filter.and(
              filter,
              new JPQLFilter(" self.project.company.id = " + user.getActiveCompany().getId()));
    }
    filters.add(filter);
    addPermissionFilter(filters, filterFromPermission);
    return filters;
  }

  @Override
  public List<Filter> getTasksDueOfUser(User user) {
    List<Filter> filters = new ArrayList<>();
    Filter dateFilter = new JPQLFilter("self.taskEndDate  < current_date()");
    filters.add(Filter.and(getTasksInCompletedOfUser(user).get(0), dateFilter));
    return filters;
  }
}
