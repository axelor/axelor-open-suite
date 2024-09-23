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
