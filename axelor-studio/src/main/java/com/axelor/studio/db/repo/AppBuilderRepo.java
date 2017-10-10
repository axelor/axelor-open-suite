package com.axelor.studio.db.repo;

import javax.validation.ValidationException;

import com.axelor.exception.AxelorException;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.service.builder.AppBuilderService;
import com.google.inject.Inject;

public class AppBuilderRepo extends AppBuilderRepository {
	
	@Inject
	private AppBuilderService appBuilderService;
	
	@Override
	public AppBuilder save(AppBuilder appBuilder) {
		
		try {
			appBuilderService.build(appBuilder);
		} catch (AxelorException e) {
			throw new ValidationException(e.getMessage());
		}
		
		return super.save(appBuilder);
	}
	
	@Override
	public void remove(AppBuilder appBuilder) {
		
		appBuilderService.clean(appBuilder);
		
		super.remove(appBuilder);
	}

}
