package com.axelor.apps.base.service;

import java.io.InputStream;

import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.exception.AxelorException;

public interface BarcodeGeneratorService {

	InputStream createBarCode(String serialno, BarcodeTypeConfig barcodeTypeConfig, boolean isPadding)
			throws AxelorException;
}
