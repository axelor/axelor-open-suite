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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface StockMoveToolService {

  /**
   * Méthode permettant d'obtenir la séquence du StockMove.
   *
   * @param stockMoveType Type de mouvement de stock
   * @param company la société
   * @return la chaine contenant la séquence du StockMove
   * @throws AxelorException Aucune séquence de StockMove n'a été configurée
   */
  public String getSequenceStockMove(int stockMoveType, Company company) throws AxelorException;

  public int getStockMoveType(StockLocation fromStockLocation, StockLocation toStockLocation);

  public BigDecimal compute(StockMove stockMove);

  public boolean getDefaultISPM(Partner clientPartner, Address toAddress);

  /**
   * Fill {@link StockMove#fromAddressStr} and {@link StockMove#toAddressStr}
   *
   * @param stockMove
   */
  void computeAddressStr(StockMove stockMove);

  /**
   * Compute stock move name.
   *
   * @param stockMove
   * @return
   */
  String computeName(StockMove stockMove);

  /**
   * Compute stock move name with the given name.
   *
   * @param stockMove
   * @param name
   * @return
   */
  String computeName(StockMove stockMove, String name);

  /**
   * Get from address from stock move or stock location.
   *
   * @param stockMove
   * @return
   */
  Address getFromAddress(StockMove stockMove);

  /**
   * Get to address from stock move or stock location.
   *
   * @param stockMove
   * @return
   */
  Address getToAddress(StockMove stockMove);

  /**
   * Get partner address.
   *
   * @param stockMove
   * @return
   * @throws AxelorException
   */
  Address getPartnerAddress(StockMove stockMove) throws AxelorException;

  /**
   * Get company address.
   *
   * @param stockMove
   * @return
   * @throws AxelorException
   */
  Address getCompanyAddress(StockMove stockMove) throws AxelorException;
}
