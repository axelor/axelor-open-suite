package com.axelor.apps.supplychain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;

public class VariantService {
	
	private static final Logger LOG = LoggerFactory.getLogger(VariantService.class); 

	@Inject
	private SequenceService sequenceService;
	
	private String exceptionMsg;
	
	@Inject
	public VariantService() {

		this.exceptionMsg = GeneralService.getExceptionAccountingMsg();
		
	}
	
	
	
	
	
}
