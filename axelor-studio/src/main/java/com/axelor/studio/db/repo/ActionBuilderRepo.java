package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.studio.db.ActionBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ActionBuilderRepo extends ActionBuilderRepository {

	@Inject
	private MetaActionRepository metaActionRepo;

	@Transactional
	@Override
	public void remove(ActionBuilder actionBuilder) {

		super.remove(actionBuilder);

		MetaAction metaAction = metaActionRepo.findByName(actionBuilder
				.getName());
		if (metaAction != null) {
			metaAction.setRemoveAction(true);
			metaActionRepo.save(metaAction);
		}

		metaAction = metaActionRepo.findByName(actionBuilder.getName()
				+ "-assign");
		if (metaAction != null) {
			metaAction.setRemoveAction(true);
			metaActionRepo.save(metaAction);
		}
	}

}
