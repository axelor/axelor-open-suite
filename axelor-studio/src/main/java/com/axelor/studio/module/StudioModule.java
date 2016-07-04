package com.axelor.studio.module;

import com.axelor.app.AxelorModule;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.StudioMetaModelRepository;
import com.axelor.studio.db.repo.ActionBuilderRepo;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.StudioWkfRepository;
import com.axelor.studio.db.repo.ViewBuilderRepo;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.WkfRepository;

public class StudioModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(MetaModelRepository.class).to(StudioMetaModelRepository.class);
		bind(WkfRepository.class).to(StudioWkfRepository.class);
		bind(ViewBuilderRepository.class).to(ViewBuilderRepo.class);
		bind(ActionBuilderRepository.class).to(ActionBuilderRepo.class);
	}

}
