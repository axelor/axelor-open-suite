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
package com.axelor.apps.account.service.invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.invoice.RefundInvoice;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * InvoiceService est une classe implémentant l'ensemble des services de
 * facturations.
 * 
 * @author Cédric Guerrier
 * 
 * @version 4.0
 */
public class InvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceService.class);
	
	
	@Inject
	private ValidateFactory validateFactory;
	
	@Inject
	private VentilateFactory ventilateFactory;
	
	@Inject
	private CancelFactory cancelFactory;
	
	@Inject
	private AlarmEngineService<Invoice> aes;
	

// WKF
	
	public Map<Invoice, List<Alarm>> getAlarms(Invoice... invoices){

		return aes.get( Invoice.class, invoices );
	}
	
	
	/**
	 * Lever l'ensemble des alarmes d'une facture.
	 * 
	 * @param invoice
	 * 			Une facture.
	 * 
	 * @throws Exception 
	 */
	public void raisingAlarms(Invoice invoice, String alarmEngineCode) {

		Alarm alarm = aes.get(alarmEngineCode, invoice, true);
		
		if (alarm != null){
			
			alarm.setInvoice(invoice);
			
		}

	}

	
	
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
	public void compute(final Invoice invoice) throws AxelorException {

		LOG.debug("Calcule de la facture");
		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator() {
			
			@Override
			public Invoice generate() throws AxelorException {

				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.addAll(invoice.getInvoiceLineList());
				invoiceLines.addAll(invoice.getTaxInvoiceLineList());
				
				populate(invoice, invoiceLines);
				
				return invoice;
			}
			
		};
		
		invoiceGenerator.generate().save();
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeFooter(final Invoice invoice) throws AxelorException {

		LOG.debug("Calcule du pied de facture");
		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator() {
			
			@Override
			public Invoice generate() throws AxelorException {

				computeInvoice(invoice);
					
				return invoice;
			}
			
		};
		
		invoiceGenerator.generate().save();
		
	}
	
	
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
	public void validate(Invoice invoice) throws AxelorException {

		LOG.debug("Validation de la facture");
		
		validateFactory.getValidator(invoice).process( );
		invoice.save();
		
	}

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
	public void ventilate( Invoice invoice ) throws AxelorException {

		LOG.debug("Ventilation de la facture {}", invoice.getInvoiceId());
		
		ventilateFactory.getVentilator(invoice).process();
		
		invoice.save();
		
	}

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
	public void cancel(Invoice invoice) throws AxelorException {

		LOG.debug("Annulation de la facture {}", invoice.getInvoiceId());
		
		cancelFactory.getCanceller(invoice).process();
		
		invoice.save();
		
	}
	

	
	/**
	 * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur l'écriture de facture.
	 * (Transaction)
	 * 
	 * @param invoice
	 * 		Une facture
	 */
	@Transactional
	public void usherProcess(Invoice invoice)  {
		Move move = invoice.getMove();
		
		if(move != null)  {
			if(invoice.getUsherPassageOk())  {
				for(MoveLine moveLine : move.getMoveLineList())  {
					moveLine.setUsherPassageOk(true);
				}
			}
			else  {
				for(MoveLine moveLine : move.getMoveLineList())  {
					moveLine.setUsherPassageOk(false);
				}
			}
			move.save();
		}
	}
	
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
	public void createRefund(Invoice invoice) throws AxelorException {
		
		LOG.debug("Créer un avoir pour la facture {}", new Object[] { invoice.getInvoiceId() });
		
		invoice.setRefundInvoice((new RefundInvoice(invoice)).generate());
		invoice.save();
		
	}
	
}
