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
