package com.axelor.apps.account.util;

import java.util.Comparator;

import com.axelor.apps.account.db.InvoiceLine;

public class InvoiceLineComparator implements Comparator<InvoiceLine>{

	@Override
	public int compare(InvoiceLine invl1, InvoiceLine invl2) {
		return invl1.getSequence() - invl2.getSequence();
	}

}
