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
package com.axelor.apps.account.service.invoice;

import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Alarm;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

/**
 * InvoiceService est une classe implémentant l'ensemble des services de
 * facturations.
 * 
 */
public interface InvoiceService {
	
	public Map<Invoice, List<Alarm>> getAlarms(Invoice... invoices);
	
	/**
	 * Lever l'ensemble des alarmes d'une facture.
	 * 
	 * @param invoice
	 * 			Une facture.
	 * 
	 * @throws Exception 
	 */
	public void raisingAlarms(Invoice invoice, String alarmEngineCode);

	
	
	/**
	 * Fonction permettant de calculer l'intégralité d'une facture :
	 * <ul>
	 * 	<li>Détermine les taxes;</li>
	 * 	<li>Détermine la TVA;</li>
	 * 	<li>Détermine les totaux.</li>
	 * </ul>
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture.
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice compute(final Invoice invoice) throws AxelorException;
	
	
	/**
	 * Validation d'une facture.
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture.
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Invoice invoice) throws AxelorException;
	
	/**
	 * Ventilation comptable d'une facture.
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture.
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void ventilate( Invoice invoice ) throws AxelorException;

	/**
	 * Annuler une facture.
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture.
	 * 
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(Invoice invoice) throws AxelorException;
	

	
	/**
	 * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur l'écriture de facture.
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture
	 */
	@Transactional
	public void usherProcess(Invoice invoice);
	
	/**
	 * Créer un avoir.
	 * <p>
	 * Un avoir est une facture "inversée". Tout le montant sont opposés à la facture originale.
	 * </p>
	 * 
	 * @param invoice
	 * 
	 * @return
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createRefund(Invoice invoice) throws AxelorException;
	
	
	public void setDraftSequence(Invoice invoice);
	
	
	public void updateAmountPaid(Invoice invoice) throws AxelorException;
	
	public void generateBudgetDistribution(Invoice invoice);

}
