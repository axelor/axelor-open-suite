/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface StockMoveService {

	/**
	 * Méthode permettant d'obtenir la séquence du StockMove.
	 * @param stockMoveType Type de mouvement de stock
	 * @param company la société
	 * @return la chaine contenant la séquence du StockMove
	 * @throws AxelorException Aucune séquence de StockMove n'a été configurée
	 */
	public String getSequenceStockMove(int stockMoveType, Company company) throws AxelorException;

	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param fromAddress l'adresse destination
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStockMove(Address fromAddress, Address toAddress, Company company, Partner clientPartner, Location fromLocation,
			Location toLocation, LocalDate estimatedDate, String description) throws AxelorException;

	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @param refSequence la séquence du StockMove
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStockMove(Address fromAddress, Address toAddress, Company company, Partner clientPartner, Location fromLocation,
			Location toLocation, LocalDate realDate, LocalDate estimatedDate, String description) throws AxelorException;

	public int getStockMoveType(Location fromLocation, Location toLocation);

	public void validate(StockMove stockMove) throws AxelorException;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void plan(StockMove stockMove) throws AxelorException;

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String realize(StockMove stockMove) throws AxelorException;

	public boolean mustBeSplit(List<StockMoveLine> stockMoveLineList);


	public StockMove copyAndSplitStockMove(StockMove stockMove) throws AxelorException;


	public StockMove copyAndSplitStockMoveReverse(StockMove stockMove, boolean split) throws AxelorException;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(StockMove stockMove) throws AxelorException;

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean splitStockMoveLinesUnit(List<StockMoveLine> stockMoveLines, BigDecimal splitQty);

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean splitStockMoveLinesSpecial(List<HashMap> stockMoveLines, BigDecimal splitQty);

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void copyQtyToRealQty(StockMove stockMove);


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateReversion(StockMove stockMove) throws AxelorException;

	public Long splitInto2(Long originalStockMoveId, List<StockMoveLine> stockMoveLines);
	
	public BigDecimal compute(StockMove stockMove);

}
