package com.axelor.studio.service.data.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.GridView;
import com.axelor.studio.service.ViewLoaderService;
import com.google.inject.Inject;

public class ExportModel {
	
	private static final Logger log = LoggerFactory.getLogger(ExportModel.class);
	
	private final List<String> SUPPORTED_TYPES = Arrays.asList(new String[]{"form", "dashboard", "grid"});
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	private ExportDashboard exportDashboard;
	
	@Inject
	private ExportForm exportForm;
	
	private ExportService exportService;
	
	public void export(ExportService exportService, MetaAction action) {
		
		this.exportService = exportService;
		
		Map<String, String> views = new HashMap<String, String>();
		
		try {
			
			ObjectViews objectViews = XMLViews.fromXML(action.getXml());
			ActionView actionView = (ActionView) objectViews.getActions().get(0);
			
			for (View view : actionView.getViews()) {
				String type = view.getType();
				if  (SUPPORTED_TYPES.contains(type)) {
					views.put(type, view.getName());
				}
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		processModel(action.getModule(), action.getModel(), views);
		
	}
	
	private void processModel(String module, String model, Map<String, String> views) {
		
		try{
			
			String dashboard = views.get("dashboard");
			if (dashboard != null) {
				if (!exportService.isViewProcessed(dashboard)) {
					exportDashboard.export(exportService, dashboard);
				}
			}
			else if (model != null) {
				
				String name = views.get("form");
				if (exportService.isViewProcessed(name)) {
					return;
				}
				
				MetaView formView = getMetaView(model, "form", views.get("form"));
				if (formView == null 
						|| exportService.isViewProcessed(formView.getName()) 
						|| !exportService.isExportModule(formView.getModule())) {
					log.debug("Form view not considered: {}", formView);
					return;
				}
				
				MetaView grid = getMetaView(model, "grid", views.get("grid"));
				
				List<String[]> o2mViews = exportForm.export(exportService, formView, getGridFields(grid));
				
				addO2MViews(o2mViews, module, model);
				
				o2mViews = new ArrayList<String[]>();
			}
				
			
		}
		catch (IllegalArgumentException | JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> getGridFields(MetaView view) {
		
		List<String> fields = new ArrayList<String>();
		
		if (view == null) {
			return fields;
		}
		
		try {
			ObjectViews views = XMLViews.fromXML(view.getXml());
			
			GridView gridView = (GridView) views.getViews().get(0);
			
			String name = view.getName();
			String defaultName = ViewLoaderService.getDefaultViewName(view.getModel(), "grid");
			
			if (name.equals(defaultName)) {
				fields.add("x");
			}
			else {
				fields.add(name);
			}
			
			for (AbstractWidget item : gridView.getItems()) {
				if (item instanceof Field) {
					fields.add(((Field) item).getName());
				}
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return fields;
	}
	
	
	private MetaView getMetaView(String model, String type, String name) {
		
		if (name == null) {
			name = ViewLoaderService.getDefaultViewName(model, type);
		}
		
		MetaView view =  metaViewRepo.all().filter(
				"self.type = ? and self.model = ? and self.name = ?", 
				 type, model, name).fetchOne();
		
		return view;
		
	}
	
	private void addO2MViews(List<String[]> views, String module, String model) {
		
		for (String[] view : views) {
			
			if (view[3] != null) {
				exportService.setMenuPath(view[3]);
			}
			
			Map<String,String> viewMap = new HashMap<String, String>();
			viewMap.put("form", view[1]);
			viewMap.put("grid", view[2]);
			processModel(module, view[0], viewMap);
			exportService.setMenuPath(null);
		}
				
		
	}

}
