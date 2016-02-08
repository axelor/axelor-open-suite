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
package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.service.InvoicePaymentServiceImpl;
import com.axelor.inject.Beans;

public class InvoicePaymentManagementRepository extends InvoicePaymentRepository {

	@Override
	public InvoicePayment save(InvoicePayment invoicePayment) {
		try {

			Beans.get(InvoicePaymentServiceImpl.class).validate(invoicePayment);
			return super.save(invoicePayment);
		} catch (Exception e) {
			System.out.println("l'erreur : "+ e.getLocalizedMessage());
			System.out.println("l'erreur : "+ e);
			System.out.println("l'erreur : "+ e.getMessage());
			System.out.println("l'erreur : "+ e.getCause());
			System.out.println("l'erreur : "+ e.getStackTrace());
			System.out.println("l'erreur : "); e.printStackTrace();





			throw new PersistenceException(e.getLocalizedMessage());
		}
	}
}
