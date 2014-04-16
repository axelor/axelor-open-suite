/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

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
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveService.class); 

	@Inject
	private StockMoveLineService stockMoveLineService;
	
	@Inject
	private SequenceService sequenceService;
	
	private LocalDate today;
	private String exceptionMsg;
	
	@Inject
	public StockMoveService() {

		this.today = GeneralService.getTodayDate();
		this.exceptionMsg = GeneralService.getExceptionAccountingMsg();
		
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
				ref = sequenceService.getSequence(IAdministration.INTERNAL, company, false);
				if (ref == null)  {
					throw new AxelorException(String.format("%s Aucune séquence configurée pour les mouvements internes de stock pour la société %s",
							exceptionMsg, company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
				
			case IStockMove.TYPE_INCOMING:
				ref = sequenceService.getSequence(IAdministration.INCOMING, company, false);
				if (ref == null)  {
					throw new AxelorException(String.format("%s Aucune séquence configurée pour les receptions de stock pour la société %s",
							exceptionMsg, company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
				
			case IStockMove.TYPE_OUTGOING:
				ref = sequenceService.getSequence(IAdministration.OUTGOING, company, false);
				if (ref == null)  {
					throw new AxelorException(String.format("%s Aucune séquence configurée pour les livraisons de stock pour la société %s",
							exceptionMsg, company.getName()), IException.CONFIGURATION_ERROR);
				}
				break;
			
			default:
				throw new AxelorException(String.format("%s Type de mouvement de stock non déterminé ",
						exceptionMsg, company.getName()), IException.CONFIGURATION_ERROR);
		
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
	
	public Project getBusinessProject(StockMove stockMove)  {
		
		if(stockMove.getSalesOrder() != null)  {
			
			return stockMove.getSalesOrder().getProject();
			
		}
		
		return null;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void plan(StockMove stockMove) throws AxelorException  {
		
		LOG.debug("Plannification du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		Location fromLocation = stockMove.getFromLocation();
		Location toLocation = stockMove.getToLocation();
		
		if(fromLocation == null)  {
			throw new AxelorException(String.format("%s Aucun emplacement source selectionné pour le mouvement de stock %s",
					exceptionMsg, stockMove.getName()), IException.CONFIGURATION_ERROR);
		}
		if(toLocation == null)  {
			throw new AxelorException(String.format("%s Aucun emplacement destination selectionné pour le mouvement de stock %s",
					exceptionMsg, stockMove.getName()), IException.CONFIGURATION_ERROR);
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
				this.getBusinessProject(stockMove),
				false);
		
		if(stockMove.getEstimatedDate() == null)  {
			stockMove.setEstimatedDate(this.today);
		}
		
		stockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		
		stockMove.save();
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public StockMove realize(StockMove stockMove) throws AxelorException  {
	
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		stockMoveLineService.updateLocations(
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
		
		if(this.mustBeSplit(stockMove.getStockMoveLineList()))  {
			return this.copyAndSplitStockMove(stockMove);
		}
		
		return null;
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
			
			if(stockMoveLine.getQty().compareTo(stockMoveLine.getRealQty()) != 0)   {
				StockMoveLine newStockMoveLine = JPA.copy(stockMoveLine, false);
				
				newStockMoveLine.setRealQty(BigDecimal.ZERO);
				newStockMoveLine.setQty(stockMoveLine.getQty().subtract(stockMoveLine.getRealQty()));
				
				newStockMove.addStockMoveLineListItem(newStockMoveLine);
			}
		}
		
		newStockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		newStockMove.setRealDate(null);
		newStockMove.setStockMoveSeq(this.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));
		newStockMove.setName(newStockMove.getStockMoveSeq() + " Partial stock move (From " + stockMove.getStockMoveSeq() + " )" );
		
		return newStockMove.save();
		
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
				this.getBusinessProject(stockMove),
				false);
		
		stockMove.setStatusSelect(IStockMove.STATUS_CANCELED);
		stockMove.setRealDate(this.today);
		stockMove.save();
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Boolean splitStockMoveLinesUnit(List<StockMoveLine> stockMoveLines, BigDecimal splitQty){
		
		Boolean selected = false;

		for(StockMoveLine moveLine : stockMoveLines){
			if(moveLine.isSelected()){
				selected = true;
				StockMoveLine line = StockMoveLine.find(moveLine.getId());
				BigDecimal totalQty = line.getQty();
				LOG.debug("Move Line selected: {}, Qty: {}",new Object[]{line,totalQty});
				while(splitQty.compareTo(totalQty) < 0){
					totalQty = totalQty.subtract(splitQty);
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(splitQty);
					newLine.save();
				}
				LOG.debug("Qty remains: {}",totalQty);
				if(totalQty.compareTo(BigDecimal.ZERO) > 0){
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(totalQty);
					newLine.save();
					LOG.debug("New line created: {}",newLine.save());
				}
				line.remove();
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
				StockMoveLine line = StockMoveLine.find(Long.parseLong(moveLine.get("id").toString()));
				BigDecimal totalQty = line.getQty();
				LOG.debug("Move Line selected: {}, Qty: {}",new Object[]{line,totalQty});
				while(splitQty.compareTo(totalQty) < 0){
					totalQty = totalQty.subtract(splitQty);
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(splitQty);
					newLine.save();
				}
				LOG.debug("Qty remains: {}",totalQty);
				if(totalQty.compareTo(BigDecimal.ZERO) > 0){
					StockMoveLine newLine = JPA.copy(line, false);
					newLine.setQty(totalQty);
					newLine.save();
					LOG.debug("New line created: {}",newLine.save());
				}
				line.remove();
			}
		}
		
		return selected;
	}
	
}
