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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
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
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
      TeamTaskRepository teamTaskRepo) {
    this.saleOrderRepo = saleOrderRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.projectRepo = projectRepo;
    this.ticketRepo = ticketRepo;
    this.invoiceRepo = invoiceRepo;
    this.teamTaskRepo = teamTaskRepo;
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
    map.put("$newTasks", getNewTasksIndicator(user));
    map.put("$tasksInProgress", getTasksInProgressIndicator(user));
    map.put("$tasksDue", getTasksDueIndicator(user));
    return map;
  }

  /* SaleOrder Indicators */
  protected Integer getOrdersInProgressIndicator(User user) {
    List<SaleOrder> saleOrderList =
        saleOrderRepo.all().filter(getOrdersInProgressOfUser(user)).fetch();
    return !saleOrderList.isEmpty() ? saleOrderList.size() : 0;
  }

  protected Integer getQuotationsIndicator(User user) {
    List<SaleOrder> saleOrderList = saleOrderRepo.all().filter(getQuotationsOfUser(user)).fetch();
    return !saleOrderList.isEmpty() ? saleOrderList.size() : 0;
  }

  protected String getLastOrderIndicator(User user) {
    SaleOrder saleOrder = saleOrderRepo.all().filter(getLastOrderOfUser(user)).fetchOne();
    return saleOrder != null
        ? saleOrder.getConfirmationDateTime().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  /* StockMove Indicators */
  protected String getLastDeliveryIndicator(User user) {
    StockMove stockMove = stockMoveRepo.all().filter(getLastDeliveryOfUser(user)).fetchOne();
    return stockMove != null
        ? stockMove.getRealDate().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  protected String getNextDeliveryIndicator(User user) {
    StockMove stockMove = stockMoveRepo.all().filter(getNextDeliveryOfUser(user)).fetchOne();
    return stockMove != null
        ? stockMove.getEstimatedDate().format(DATE_FORMATTER)
        : I18n.get(CLIENT_PORTAL_NO_DATE);
  }

  protected Integer getPlannedDeliveriesIndicator(User user) {
    List<StockMove> stockMoveList =
        stockMoveRepo.all().filter(getPlannedDeliveriesOfUser(user)).fetch();
    return !stockMoveList.isEmpty() ? stockMoveList.size() : 0;
  }

  protected Integer getReversionsIndicator(User user) {
    List<StockMove> stockMoveList = stockMoveRepo.all().filter(getReversionsOfUser(user)).fetch();
    return !stockMoveList.isEmpty() ? stockMoveList.size() : 0;
  }

  /* Invoice Indicators */
  protected Integer getOverdueInvoicesIndicator(User user) {
    List<Invoice> invoiceList = invoiceRepo.all().filter(getOverdueInvoicesOfUser(user)).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  protected Integer getAwaitingInvoicesIndicator(User user) {
    List<Invoice> invoiceList = invoiceRepo.all().filter(getAwaitingInvoicesOfUser(user)).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  protected String getTotalRemainingIndicator(User user) {
    List<Invoice> invoiceList = invoiceRepo.all().filter(getTotalRemainingOfUser(user)).fetch();
    if (!invoiceList.isEmpty()) {
      BigDecimal total =
          invoiceList
              .stream()
              .map(Invoice::getAmountRemaining)
              .reduce((x, y) -> x.add(y))
              .orElse(BigDecimal.ZERO);
      return total.toString() + invoiceList.get(0).getCurrency().getSymbol();
    }
    return BigDecimal.ZERO.toString();
  }

  protected Integer getRefundIndicator(User user) {
    List<Invoice> invoiceList = invoiceRepo.all().filter(getRefundOfUser(user)).fetch();
    return !invoiceList.isEmpty() ? invoiceList.size() : 0;
  }

  /* Helpdesk Indicators */
  protected Integer getCustomerTicketsIndicator(User user) {
    List<Ticket> ticketList = ticketRepo.all().filter(getTicketsOfUser(user)).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Integer getCompanyTicketsIndicator(User user) {
    List<Ticket> ticketList = ticketRepo.all().filter(getCompanyTicketsOfUser(user)).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Integer getResolvedTicketsIndicator(User user) {
    List<Ticket> ticketList = ticketRepo.all().filter(getResolvedTicketsOfUser(user)).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  protected Object getLateTicketsIndicator(User user) {
    List<Ticket> ticketList = ticketRepo.all().filter(getLateTicketsOfUser(user)).fetch();
    return !ticketList.isEmpty() ? ticketList.size() : 0;
  }

  /* Project Indicators */
  protected Integer getTotalProjectsIndicator(User user) {
    List<Project> projectList = projectRepo.all().filter(getTotalProjectsOfUser(user)).fetch();
    return !projectList.isEmpty() ? projectList.size() : 0;
  }

  protected Integer getNewTasksIndicator(User user) {
    List<TeamTask> teamTaskList = teamTaskRepo.all().filter(getNewTasksOfUser(user)).fetch();
    return !teamTaskList.isEmpty() ? teamTaskList.size() : 0;
  }

  protected Integer getTasksInProgressIndicator(User user) {
    List<TeamTask> teamTaskList = teamTaskRepo.all().filter(getTasksInProgressOfUser(user)).fetch();
    return !teamTaskList.isEmpty() ? teamTaskList.size() : 0;
  }

  protected Integer getTasksDueIndicator(User user) {
    List<TeamTask> teamTaskList = teamTaskRepo.all().filter(getTasksDueOfUser(user)).fetch();
    return !teamTaskList.isEmpty() ? teamTaskList.size() : 0;
  }

  /* SaleOrder Query */
  @Override
  public String getOrdersInProgressOfUser(User user) {
    String query =
        "self.clientPartner.id = "
            + user.getPartner().getId()
            + " AND self.statusSelect = "
            + SaleOrderRepository.STATUS_ORDER_CONFIRMED;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getQuotationsOfUser(User user) {
    String query =
        "self.clientPartner.id = "
            + user.getPartner().getId()
            + " AND self.statusSelect IN ("
            + SaleOrderRepository.STATUS_DRAFT_QUOTATION
            + ","
            + SaleOrderRepository.STATUS_FINALIZED_QUOTATION
            + ")";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getLastOrderOfUser(User user) {
    String query =
        "self.clientPartner.id = "
            + user.getPartner().getId()
            + " AND self.statusSelect = "
            + SaleOrderRepository.STATUS_ORDER_COMPLETED;

    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    query = query + " ORDER BY self.confirmationDateTime DESC";
    return query;
  }

  /* StockMove Query */
  @Override
  public String getLastDeliveryOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.typeSelect = "
            + StockMoveRepository.TYPE_OUTGOING
            + " AND self.statusSelect = "
            + StockMoveRepository.STATUS_REALIZED
            + " AND self.isReversion != true";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    query = query + " ORDER BY self.realDate DESC";
    return query;
  }

  @Override
  public String getNextDeliveryOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.typeSelect = "
            + StockMoveRepository.TYPE_OUTGOING
            + " AND self.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.isReversion != true";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    query = query + " ORDER BY self.estimatedDate ASC";
    return query;
  }

  @Override
  public String getPlannedDeliveriesOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.typeSelect = "
            + StockMoveRepository.TYPE_OUTGOING
            + " AND self.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.isReversion != true";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getReversionsOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.typeSelect = "
            + StockMoveRepository.TYPE_OUTGOING
            + " AND self.isReversion = true";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  /* Invoice Query */
  @Override
  public String getOverdueInvoicesOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.dueDate < current_date() "
            + " AND self.amountRemaining != 0 AND self.statusSelect != "
            + InvoiceRepository.STATUS_CANCELED;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getAwaitingInvoicesOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.dueDate < current_date() "
            + " AND self.amountRemaining != 0 AND self.statusSelect != "
            + InvoiceRepository.STATUS_CANCELED;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getTotalRemainingOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.amountRemaining != 0 AND self.statusSelect != "
            + InvoiceRepository.STATUS_CANCELED;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getRefundOfUser(User user) {
    String query =
        "self.partner.id = "
            + user.getPartner().getId()
            + " AND self.operationTypeSelect = "
            + InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  /* Helpdesk Query */
  @Override
  public String getTicketsOfUser(User user) {
    return "self.customer.id = "
        + user.getPartner().getId()
        + " AND self.assignedToUser.id = "
        + user.getId();
  }

  @Override
  public String getCompanyTicketsOfUser(User user) {
    return "self.customer.id = "
        + user.getPartner().getId()
        + " AND self.assignedToUser.id = "
        + user.getActiveCompany().getId();
  }

  @Override
  public String getResolvedTicketsOfUser(User user) {
    return "self.customer.id = "
        + user.getPartner().getId()
        + " AND self.assignedToUser.id = "
        + user.getId()
        + " AND self.statusSelect IN ("
        + TicketRepository.STATUS_RESOLVED
        + ", "
        + TicketRepository.STATUS_CLOSED
        + ")";
  }

  @Override
  public String getLateTicketsOfUser(User user) {
    return "self.customer.id = "
        + user.getPartner().getId()
        + " AND self.assignedToUser.id = "
        + user.getId()
        + " AND ((self.endDateT != null AND self.endDateT > self.deadlineDateT) "
        + " OR (self.endDateT = null and self.deadlineDateT < current_date() ) )";
  }

  /* Project Query */
  @Override
  public String getTotalProjectsOfUser(User user) {
    String query =
        "self.isProject = true AND self.clientPartner.id = "
            + user.getPartner().getId()
            + " AND self.statusSelect != "
            + ProjectRepository.STATE_CANCELED;
    if (user.getActiveCompany() != null) {
      query = query + " AND self.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getNewTasksOfUser(User user) {
    String query =
        "self.status = 'new' "
            + " AND self.typeSelect = '"
            + TeamTaskRepository.TYPE_TASK
            + "' AND self.project.clientPartner.id = "
            + user.getPartner().getId();
    if (user.getActiveCompany() != null) {
      query = query + " AND self.project.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getTasksInProgressOfUser(User user) {
    String query =
        "self.status = 'in-progress'"
            + " AND self.typeSelect = '"
            + TeamTaskRepository.TYPE_TASK
            + "' AND self.project.clientPartner.id = "
            + user.getPartner().getId();
    if (user.getActiveCompany() != null) {
      query = query + " AND self.project.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }

  @Override
  public String getTasksDueOfUser(User user) {
    String query =
        "self.status IN ('in-progress','new')"
            + " AND self.project.clientPartner.id = "
            + user.getPartner().getId()
            + " AND self.typeSelect = '"
            + TeamTaskRepository.TYPE_TASK
            + "' AND self.taskEndDate  < current_date() ";
    if (user.getActiveCompany() != null) {
      query = query + " AND self.project.company.id = " + user.getActiveCompany().getId();
    }
    return query;
  }
}
