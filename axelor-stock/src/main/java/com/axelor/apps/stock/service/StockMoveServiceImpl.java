/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.ILocation;
import com.axelor.apps.stock.db.IStockMove;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveServiceImpl extends StockMoveRepository implements StockMoveService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveServiceImpl.class); 

	@Inject
	protected StockMoveLineService stockMoveLineService;
	
	@Inject
	private SequenceService sequenceService;
	
	protected LocalDate today;
	
	@Inject
	private  StockMoveLineRepository stockMoveLineRepo;
	
	@Inject
	public StockMoveServiceImpl() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Méthode permettant d'obtenir la séquence du StockMove.
	 * @param stockMoveType Type de mouvement de stock
	 * @param company la société
	 * @return la chaine contenant la séquence du StockMove
	 * @throws AxelorException Aucune séquence de StockMove n'a été configurée
	 */
	public String getSequenceStockMove(int stockMoveType, Company company) throws AxelorException {

		String ref = "";
		
		switch(stockMoveType)  {
			case IStockMove.TYPE_INTERNAL:
				ref = sequenceService.getSequenceNumber(IAdministration.INTERNAL, company);
				if (ref == null)  {
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_1),
							company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
				
			case IStockMove.TYPE_INCOMING:
				ref = sequenceService.getSequenceNumber(IAdministration.INCOMING, company);
				if (ref == null)  {
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_2),
							company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
				
			case IStockMove.TYPE_OUTGOING:
				ref = sequenceService.getSequenceNumber(IAdministration.OUTGOING, company);
				if (ref == null)  {
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_3),
							company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
			
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_4),
						company.getName()), IException.CONFIGURATION_ERROR);
		
		}
		
		return ref;
	}
	
	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param fromAddress l'adresse destination
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStockMove(Address fromAddress, Address toAddress, Company company, Partner clientPartner, Location fromLocation, Location toLocation, LocalDate estimatedDate) throws AxelorException {

		return this.createStockMove(fromAddress, toAddress, company, clientPartner, fromLocation, toLocation, null, estimatedDate);
	}
	
	
	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @param refSequence la séquence du StockMove
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStockMove(Address fromAddress, Address toAddress, Company company, Partner clientPartner, Location fromLocation, Location toLocation, LocalDate realDate, LocalDate estimatedDate) throws AxelorException {

		StockMove stockMove = new StockMove();
		stockMove.setFromAddress(fromAddress);
		stockMove.setToAddress(toAddress);
		stockMove.setCompany(company);
		stockMove.setStatusSelect(IStockMove.STATUS_DRAFT);
		stockMove.setRealDate(realDate);
		stockMove.setEstimatedDate(estimatedDate);
		stockMove.setPartner(clientPartner);
		stockMove.setFromLocation(fromLocation);
		stockMove.setToLocation(toLocation);
		
		return stockMove;
	}
	
	
	public int getStockMoveType(Location fromLocation, Location toLocation)  {
		
		if(fromLocation.getTypeSelect() == ILocation.INTERNAL && toLocation.getTypeSelect() == ILocation.INTERNAL) {
			return IStockMove.TYPE_INTERNAL;
		}
		else if(fromLocation.getTypeSelect() != ILocation.INTERNAL && toLocation.getTypeSelect() == ILocation.INTERNAL) {	
			return IStockMove.TYPE_INCOMING;
		}
		else if(fromLocation.getTypeSelect() == ILocation.INTERNAL && toLocation.getTypeSelect() != ILocation.INTERNAL) {
			return IStockMove.TYPE_OUTGOING;
		}
		return 0;
	}

	
	public void validate(StockMove stockMove) throws AxelorException  {
		
		this.plan(stockMove);
		this.realize(stockMove);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void plan(StockMove stockMove) throws AxelorException  {
		
		LOG.debug("Plannification du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		Location fromLocation = stockMove.getFromLocation();
		Location toLocation = stockMove.getToLocation();
		
		if(fromLocation == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_5),
					stockMove.getName()), IException.CONFIGURATION_ERROR);
		}
		if(toLocation == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_6),
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
		
		stockMoveLineService.updateLocations(
				fromLocation, 
				toLocation, 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_PLANNED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				false);
		
		if(stockMove.getEstimatedDate() == null)  {
			stockMove.setEstimatedDate(this.today);
		}
		
		stockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		
		save(stockMove);
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String realize(StockMove stockMove) throws AxelorException  {
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		String newStockSeq = null;

		stockMoveLineService.updateLocations(
				stockMove.getFromLocation(), 
				stockMove.getToLocation(), 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_REALIZED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				true);
		
		stockMove.setStatusSelect(IStockMove.STATUS_REALIZED);
		stockMove.setRealDate(this.today);
		save(stockMove);
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
	
	public boolean mustBeSplit(List<StockMoveLine> stockMoveLineList)  {
		
		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			
			if(stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) != 0)  {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}

	
	public StockMove copyAndSplitStockMove(StockMove stockMove) throws AxelorException  {
		
		StockMove newStockMove = JPA.copy(stockMove, false);
		
		for(StockMoveLine stockMoveLine : stockMove.getStockMoveLineList())  {
			
			if(stockMoveLine.getQty().compareTo(stockMoveLine.getRealQty()) > 0)   {
				StockMoveLine newStockMoveLine = JPA.copy(stockMoveLine, false);
				
				newStockMoveLine.setRealQty(BigDecimal.ZERO);
				newStockMoveLine.setQty(stockMoveLine.getQty().subtract(stockMoveLine.getRealQty()));
				
				newStockMove.addStockMoveLineListItem(newStockMoveLine);
			}
		}
		
		newStockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		newStockMove.setRealDate(null);
		newStockMove.setStockMoveSeq(this.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));
		newStockMove.setName(newStockMove.getStockMoveSeq() + " " + I18n.get(IExceptionMessage.STOCK_MOVE_7) + " " + stockMove.getStockMoveSeq() + " )" );
		
		return save(newStockMove);
		
	}
	
	
	public StockMove copyAndSplitStockMoveReverse(StockMove stockMove, boolean split) throws AxelorException  {
		
		StockMove newStockMove = new StockMove();
		
		newStockMove.setCompany(stockMove.getCompany());
		newStockMove.setPartner(stockMove.getPartner());
		newStockMove.setFromLocation(stockMove.getToLocation());
		newStockMove.setToLocation(stockMove.getFromLocation());
		newStockMove.setEstimatedDate(stockMove.getEstimatedDate());
		newStockMove.setFromAddress(stockMove.getFromAddress());
		if(stockMove.getToAddress() != null)
			newStockMove.setFromAddress(stockMove.getToAddress());
		if(stockMove.getTypeSelect() == 3)
			newStockMove.setTypeSelect(2);
		if(stockMove.getTypeSelect() == 2)
			newStockMove.setTypeSelect(3);
		if(stockMove.getTypeSelect() == 1)
			newStockMove.setTypeSelect(1);
		newStockMove.setStatusSelect(1);
		newStockMove.setStockMoveSeq(getSequenceStockMove(newStockMove.getTypeSelect(),newStockMove.getCompany()));
		
		for(StockMoveLine stockMoveLine : stockMove.getStockMoveLineList())  {
			
			if(stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) > 0)   {
				StockMoveLine newStockMoveLine = JPA.copy(stockMoveLine, false);
				
				newStockMoveLine.setRealQty(BigDecimal.ZERO);
				
				if(!split)  {
					newStockMoveLine.setQty(stockMoveLine.getRealQty().subtract(stockMoveLine.getQty()));
				}
				
				newStockMove.addStockMoveLineListItem(newStockMoveLine);
			}
		}
		
		newStockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		newStockMove.setRealDate(null);
		newStockMove.setStockMoveSeq(this.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));
		newStockMove.setName(newStockMove.getStockMoveSeq() + " " + I18n.get(IExceptionMessage.STOCK_MOVE_8) + " " + stockMove.getStockMoveSeq() + " )" );
		
		return save(newStockMove);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(StockMove stockMove) throws AxelorException  {
	
		LOG.debug("Annulation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		stockMoveLineService.updateLocations(
				stockMove.getFromLocation(), 
				stockMove.getToLocation(), 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_CANCELED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate(),
				false);
		
		stockMove.setStatusSelect(IStockMove.STATUS_CANCELED);
		stockMove.setRealDate(this.today);
		save(stockMove);
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean splitStockMoveLinesUnit(List<StockMoveLine> stockMoveLines, BigDecimal splitQty){
		
		Boolean selected = false;

		for(StockMoveLine moveLine : stockMoveLines){
			if(moveLine.isSelected()){
				selected = true;
				StockMoveLine line = stockMoveLineRepo.find(moveLine.getId());
				BigDecimal totalQty = line.getQty();
				LOG.debug("Move Line selected: {}, Qty: {}",new Object[]{line,totalQty});
				while(splitQty.compareTo(totalQty) < 0){
					totalQty = totalQty.subtract(splitQty);
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(splitQty);
					stockMoveLineRepo.save(newLine);
				}
				LOG.debug("Qty remains: {}",totalQty);
				if(totalQty.compareTo(BigDecimal.ZERO) > 0){
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(totalQty);
					stockMoveLineRepo.save(newLine);
					LOG.debug("New line created: {}",newLine);
				}
				stockMoveLineRepo.remove(line);
			}
		}
		
		return selected;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean splitStockMoveLinesSpecial(List<HashMap> stockMoveLines, BigDecimal splitQty){
		
		Boolean selected = false;
		LOG.debug("SplitQty: {}",new Object[] {splitQty});
		
		for(HashMap moveLine : stockMoveLines){
			LOG.debug("Move line: {}",new Object[]{moveLine});
			if((Boolean)(moveLine.get("selected"))){
				selected = true;
				StockMoveLine line = stockMoveLineRepo.find(Long.parseLong(moveLine.get("id").toString()));
				BigDecimal totalQty = line.getQty();
				LOG.debug("Move Line selected: {}, Qty: {}",new Object[]{line,totalQty});
				while(splitQty.compareTo(totalQty) < 0){
					totalQty = totalQty.subtract(splitQty);
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(splitQty);
					stockMoveLineRepo.save(newLine);
				}
				LOG.debug("Qty remains: {}",totalQty);
				if(totalQty.compareTo(BigDecimal.ZERO) > 0){
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(totalQty);
					stockMoveLineRepo.save(newLine);
					LOG.debug("New line created: {}",newLine);
				}
				stockMoveLineRepo.remove(line);
			}
		}
		
		return selected;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void copyQtyToRealQty(StockMove stockMove){
		for(StockMoveLine line : stockMove.getStockMoveLineList())
			line.setRealQty(line.getQty());
		save(stockMove);
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateReversion(StockMove stockMove) throws AxelorException  {
	
		LOG.debug("Creation d'un mouvement de stock inverse pour le mouvement de stock: {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		save(this.copyAndSplitStockMoveReverse(stockMove, false));
		
	}
	
}
