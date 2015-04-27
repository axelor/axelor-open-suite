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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.invoice.RefundInvoice;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * InvoiceService est une classe implémentant l'ensemble des services de
 * facturations.
 * 
 */
public class InvoiceServiceImpl extends InvoiceRepository implements InvoiceService  {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceServiceImpl.class);
	
	private ValidateFactory validateFactory;
	private VentilateFactory ventilateFactory;
	private CancelFactory cancelFactory;
	private AlarmEngineService<Invoice> aes;
	
	@Inject
	public InvoiceServiceImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory, CancelFactory cancelFactory, AlarmEngineService<Invoice> aes) {

		this.validateFactory = validateFactory;
		this.ventilateFactory = ventilateFactory;
		this.cancelFactory = cancelFactory;
		this.aes = aes;
	}
	
	
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
	public Invoice compute(final Invoice invoice) throws AxelorException {

		LOG.debug("Calcule de la facture");
		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator() {
			
			@Override
			public Invoice generate() throws AxelorException {

				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.addAll(invoice.getInvoiceLineList());
				
				populate(invoice, invoiceLines);
				
				return invoice;
			}
			
		};
		
		return invoiceGenerator.generate();
		
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
		save(invoice);
		
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
		
		save(invoice);
		
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
		
		save(invoice);
		
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
			
			Beans.get(MoveRepository.class).save(move);
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
		
		invoice.addRefundInvoiceListItem((new RefundInvoice(invoice)).generate());
		save(invoice);
		
	}
	
}
