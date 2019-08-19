package com.axelor.apps.portal.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.portal.service.ClientViewService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class ClientViewController {
  static final String TITLE_COMPANY_TICKETS = /*$$(*/ "Tickets %s" /*)*/;

  public void completeClientViewIndicators(ActionRequest request, ActionResponse response) {
    Map<String, Object> map;
    map = Beans.get(ClientViewService.class).updateClientViewIndicators();

    response.setValues(map);
  }

  /* SALEORDER OnCLick */
  public void showClientMyOrders(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getOrdersOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Orders"))
            .model(SaleOrder.class.getName())
            .add("grid", "sale-order-grid")
            .add("form", "sale-order-form")
            .domain(domain)
            .map());
  }

  public void showClientMyQuotationInProgress(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getQuotationInProgressOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Quotation in progress"))
            .model(SaleOrder.class.getName())
            .add("grid", "sale-order-grid")
            .add("form", "sale-order-form")
            .domain(domain)
            .map());
  }

  public void showClientMyLastOrder(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getLastOrderOfUser(clientUser);
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).all().filter(domain).fetchOne();
    if (saleOrder != null) {
      response.setView(
          ActionView.define(I18n.get("Last order"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-form")
              .context("_showRecord", saleOrder.getId())
              .map());
    }
  }

  /* PROJECT OnClick */
  public void showClientMyTotalProjects(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getTotalProjectsOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Total projects"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .domain(domain)
            .map());
  }

  public void showClientMyTasksInProgress(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getTasksInProgressOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Tasks in progress"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .domain(domain)
            .map());
  }

  public void showClientMyTasksDue(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getTasksDueOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Tasks due"))
            .model(SaleOrder.class.getName())
            .add("grid", "project-grid")
            .add("form", "project-form")
            .domain(domain)
            .map());
  }

  /* STOCKMOVE OnClick */
  public void showClientMyLastDelivery(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getLastDeliveryOfUser(clientUser);
    StockMove stockMove = Beans.get(StockMoveRepository.class).all().filter(domain).fetchOne();
    response.setView(
        ActionView.define(I18n.get("Last delivery"))
            .model(StockMove.class.getName())
            .add("form", "stock-move-form")
            .context("_showRecord", stockMove.getId())
            .domain(domain)
            .map());
  }

  public void showClientMyNextDelivery(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getNextDeliveryOfUser(clientUser);
    StockMove stockMove = Beans.get(StockMoveRepository.class).all().filter(domain).fetchOne();
    response.setView(
        ActionView.define(I18n.get("Next delivery"))
            .model(StockMove.class.getName())
            .add("form", "stock-move-form")
            .context("_showRecord", stockMove.getId())
            .domain(domain)
            .map());
  }

  public void showClientMyRealizedDelivery(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getRealizedDeliveryOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Realized delivery"))
            .model(StockMove.class.getName())
            .add("grid", "stock-move-grid")
            .add("form", "stock-move-form")
            .domain(domain)
            .map());
  }

  /* INVOICE OnClick */
  public void showClientMyOverdueInvoices(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getOverdueInvoicesOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Overdue invoices"))
            .model(Invoice.class.getName())
            .add("grid", "invoice-grid")
            .add("form", "invoice-form")
            .domain(domain)
            .map());
  }

  public void showClientMyAwaitingInvoices(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getAwaitingInvoicesOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Awaiting invoices"))
            .model(Invoice.class.getName())
            .add("grid", "invoice-grid")
            .add("form", "invoice-form")
            .domain(domain)
            .map());
  }

  public void showClientMyTotalRemaining(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getTotalRemainingOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Total remaining"))
            .model(Invoice.class.getName())
            .add("grid", "invoice-grid")
            .add("form", "invoice-form")
            .domain(domain)
            .map());
  }

  /* TICKETS OnClick */
  public void showClientMyCustomerTickets(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getTicketsOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Customer tickets"))
            .model(Ticket.class.getName())
            .add("grid", "ticket-grid")
            .add("form", "ticket-form")
            .domain(domain)
            .map());
  }

  public void showClientMyCompanyTickets(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getCompanyTicketsOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Company tickets"))
            .model(Ticket.class.getName())
            .add("grid", "ticket-grid")
            .add("form", "ticket-form")
            .domain(domain)
            .map());
  }

  public void showClientMyResolvedTickets(ActionRequest request, ActionResponse response) {
    ClientViewService clientViewService = Beans.get(ClientViewService.class);
    User clientUser = clientViewService.getClientUser();
    String domain = clientViewService.getResolvedTicketsOfUser(clientUser);
    response.setView(
        ActionView.define(I18n.get("Resolved tickets"))
            .model(Ticket.class.getName())
            .add("grid", "ticket-grid")
            .add("form", "ticket-form")
            .domain(domain)
            .map());
  }
}
