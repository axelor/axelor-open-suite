/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.accountorganisation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.stock.db.IStockMove;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveServiceAccountOrganisationImpl extends StockMoveServiceImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveServiceAccountOrganisationImpl.class); 

	@Inject
	private StockMoveLineServiceAccountOrganisationImpl stockMoveLineServiceAccountOrganisationImpl;
	
	public Project getBusinessProject(StockMove stockMove)  {
		
		if(stockMove.getSaleOrder() != null)  {
			
			return stockMove.getSaleOrder().getProject();
			
		}
		
		return null;
	}
	
	//TODO méthode à factoriser avec super class
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void plan(StockMove stockMove) throws AxelorException  {
		
		LOG.debug("Plannification du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		Location fromLocation = stockMove.getFromLocation();
		Location toLocation = stockMove.getToLocation();
		
		if(fromLocation == null)  {
			throw new AxelorException(String.format("Aucun emplacement source selectionné pour le mouvement de stock %s",
					 stockMove.getName()), IException.CONFIGURATION_ERROR);
		}
		if(toLocation == null)  {
			throw new AxelorException(String.format("Aucun emplacement destination selectionné pour le mouvement de stock %s",
					stockMove.getName()), IException.CONFIGURATION_ERROR);
		}
		
		// Set the type select
		if(stockMove.getTypeSelect() == null || stockMove.getTypeSelect() == 0)  {
			stockMove.setTypeSelect(this.getStockMoveType(fromLocation, toLocation));
		}

		
		if(stockMove.getTypeSelect() == IStockMove.TYPE_OUTGOING)  {
			
		}
		
		// Set the sequence
		if(stockMove.getStockMoveSeq() == null || stockMove.getStockMoveSeq().isEmpty())  {
			stockMove.setStockMoveSeq(
					this.getSequenceStockMove(stockMove.getTypeSelect(), stockMove.getCompany()));
		}
		
		if(stockMove.getName() == null || stockMove.getName().isEmpty())  {
			stockMove.setName(stockMove.getStockMoveSeq());
		}
		
		stockMoveLineServiceAccountOrganisationImpl.updateLocations(
				fromLocation, 
				toLocation, 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_PLANNED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				this.getBusinessProject(stockMove),
				false);
		
		if(stockMove.getEstimatedDate() == null)  {
			stockMove.setEstimatedDate(this.today);
		}
		
		stockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		
		stockMove.save();
		
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String realize(StockMove stockMove) throws AxelorException  {
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		String newStockSeq = null;

		stockMoveLineServiceAccountOrganisationImpl.updateLocations(
				stockMove.getFromLocation(), 
				stockMove.getToLocation(), 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_REALIZED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				this.getBusinessProject(stockMove),
				true);
		
		stockMove.setStatusSelect(IStockMove.STATUS_REALIZED);
		stockMove.setRealDate(this.today);
		stockMove.save();
		if(!stockMove.getIsWithBackorder() && !stockMove.getIsWithReturnSurplus())
			return null;
		if(stockMove.getIsWithBackorder() && this.mustBeSplit(stockMove.getStockMoveLineList()))  {
			StockMove newStockMove = this.copyAndSplitStockMove(stockMove);
			newStockSeq = newStockMove.getStockMoveSeq();
		}
		if(stockMove.getIsWithReturnSurplus() && this.mustBeSplit(stockMove.getStockMoveLineList()))  {
			StockMove newStockMove = this.copyAndSplitStockMoveReverse(stockMove, true);
			if(newStockSeq != null)
				newStockSeq = newStockSeq+" "+newStockMove.getStockMoveSeq();
			else
				newStockSeq = newStockMove.getStockMoveSeq();
		}
		
		return newStockSeq;
	}
	
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(StockMove stockMove) throws AxelorException  {
	
		LOG.debug("Annulation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		stockMoveLineServiceAccountOrganisationImpl.updateLocations(
				stockMove.getFromLocation(), 
				stockMove.getToLocation(), 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_CANCELED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				this.getBusinessProject(stockMove),
				false);
		
		stockMove.setStatusSelect(IStockMove.STATUS_CANCELED);
		stockMove.setRealDate(this.today);
		stockMove.save();
	}
	
}
