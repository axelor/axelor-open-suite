package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.DashboardBuilder;
import com.axelor.studio.service.builder.DashboardBuilderService;
import com.google.inject.Inject;

public class DashboardBuilderRepo extends DashboardBuilderRepository {
	
	@Inject
	private DashboardBuilderService dashboardBuilderService;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Override
	public DashboardBuilder save(DashboardBuilder dashboardBuilder) {
	
		dashboardBuilder = super.save(dashboardBuilder);
		
		MetaView metaView = dashboardBuilderService.build(dashboardBuilder);
		if (metaView != null) {
			dashboardBuilder.setMetaViewGenerated(metaView);
		}
		else {
			metaView = dashboardBuilder.getMetaViewGenerated();
			if (metaView != null) {
				metaViewRepo.remove(metaView);
			}
		}
		return dashboardBuilder;
	}
	
	@Override
	public void remove(DashboardBuilder dashboardBuilder) {
		
		MetaView metaView = dashboardBuilder.getMetaViewGenerated();
		if (metaView != null) {
			metaViewRepo.remove(metaView);
		}
		
		super.remove(dashboardBuilder);
	}
}
