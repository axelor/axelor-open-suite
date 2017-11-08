package com.axelor.apps.stock.exception;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class InconsistentLogisticalFormLines extends AxelorException {

	private static final long serialVersionUID = 7036277936135855411L;

	public InconsistentLogisticalFormLines(LogisticalForm logisticalForm, String message, Object... messageArgs) {
		super(logisticalForm, IException.INCONSISTENCY, message, messageArgs);
	}

}
