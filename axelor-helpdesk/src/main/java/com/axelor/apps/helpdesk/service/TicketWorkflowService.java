package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.helpdesk.db.Ticket;

public interface TicketWorkflowService {

  /**
   * This method start a ticket by setting its status to on going status.
   *
   * @param ticket
   */
  void startTicket(Ticket ticket) throws AxelorException;

  /**
   * This method resolve a ticket by setting its status to resolved status.
   *
   * @param ticket
   */
  void resolveTicket(Ticket ticket) throws AxelorException;

  /**
   * This method close a ticket by setting its status to closed status.
   *
   * @param ticket
   */
  void closeTicket(Ticket ticket) throws AxelorException;

  /**
   * This method close a ticket by setting its status to open status.
   *
   * @param ticket
   */
  void openTicket(Ticket ticket) throws AxelorException;
}
