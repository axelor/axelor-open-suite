package com.axelor.studio.web;

import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.WkfRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MetaJsonModelController {
	
	@Inject
	private MetaJsonModelRepository metaJsonModelRepo;
	
	@Inject
	private WkfRepository wkfRepo;
	
	@Inject
	private ViewBuilderRepository viewBuilderRepo;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	public void openWorkflow(ActionRequest request, ActionResponse response) {
		
		MetaJsonModel jsonModel = request.getContext().asType(MetaJsonModel.class);
		jsonModel = metaJsonModelRepo.find(jsonModel.getId());
		
		Wkf wkf = null;
		
		MetaView formView = jsonModel.getFormView();
		if (formView != null) {
			wkf = wkfRepo.all().filter("self.viewBuilder.name = ?1", formView.getName()).fetchOne();
		}
		
		ActionViewBuilder builder = ActionView.define("Workflow")
			.add("form","wkf-form")
			.model("com.axelor.studio.db.Wkf");
			
		if (wkf == null) {
			builder.context("_modelName", MetaJsonRecord.class.getSimpleName()) ;
			builder.context("_viewBuilder", getViewBuilder(jsonModel));
			builder.context("_wkfField", "status");
			builder.context("_module", "axelor-custom");
		}
		else {
			builder.context("_showRecord", wkf.getId().toString());
		}
		
		response.setView(builder.map());
		
	}
	
	@Transactional
	public String getViewBuilder(MetaJsonModel jsonModel) {
		
		MetaView formView = jsonModel.getFormView();
		
		ViewBuilder viewBuilder = viewBuilderRepo.all().filter("self.name = ?1 and self.viewType = 'form'", formView.getName()).fetchOne();
		
		if (viewBuilder != null) {
			return viewBuilder.getName();
		}
		
		viewBuilder = new ViewBuilder();
		viewBuilder.setMetaModule(getDefaultModule());
		viewBuilder.setMetaView(formView);
		viewBuilder.setMetaModel(metaModelRepo.findByName(MetaJsonRecord.class.getSimpleName()));
		viewBuilder.setTitle(formView.getTitle());
		viewBuilder.setName(formView.getName());
		viewBuilder.setViewType("form");
		viewBuilder.setModel(MetaJsonRecord.class.getName());
		
		viewBuilderRepo.save(viewBuilder);
		
		return viewBuilder.getName();
		
	}
	
	@Transactional
	public MetaModule getDefaultModule() {
		
		MetaModule module = metaModuleRepo.findByName("axelor-custom");
		
		if (module != null) {
			return module;
		}
		
		module = new MetaModule("axelor-custom");
		module.setTitle("Axelor::Custom");
		module.setDepends("axelor-studio");
		module.setModuleVersion("1.0.0");
		module.setDescription("Default custom module");
		module.setRemovable(true);
		module.setCustomised(true);
		
		return metaModuleRepo.save(module);
	}


}
