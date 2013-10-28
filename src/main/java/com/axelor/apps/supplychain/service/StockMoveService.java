/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
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
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @param refSequence la séquence du StockMove
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStockMove(Address toAddress, Company company, Partner clientPartner, Location fromLocation, Location toLocation, LocalDate estimatedDate) throws AxelorException {

		return this.createStockMove(toAddress, company, clientPartner, fromLocation, toLocation, null, estimatedDate);
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
	public StockMove createStockMove(Address toAddress, Company company, Partner clientPartner, Location fromLocation, Location toLocation, LocalDate realDate, LocalDate estimatedDate) throws AxelorException {

		StockMove stockMove = new StockMove();
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
				stockMove.getEstimatedDate());
		
		if(stockMove.getEstimatedDate() == null)  {
			stockMove.setEstimatedDate(this.today);
		}
		
		stockMove.setStatusSelect(IStockMove.STATUS_PLANNED);
		
		stockMove.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void realize(StockMove stockMove) throws AxelorException  {
	
		LOG.debug("Réalisation du mouvement de stock : {} ", new Object[] { stockMove.getStockMoveSeq() });
		
		stockMoveLineService.updateLocations(
				stockMove.getFromLocation(), 
				stockMove.getToLocation(), 
				stockMove.getStatusSelect(), 
				IStockMove.STATUS_REALIZED, 
				stockMove.getStockMoveLineList(),
				stockMove.getEstimatedDate());
		
		stockMove.setStatusSelect(IStockMove.STATUS_REALIZED);
		stockMove.setRealDate(this.today);
		stockMove.save();
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
				stockMove.getEstimatedDate());
		
		stockMove.setStatusSelect(IStockMove.STATUS_CANCELED);
		stockMove.setRealDate(this.today);
		stockMove.save();
	}
	
	
	
}
