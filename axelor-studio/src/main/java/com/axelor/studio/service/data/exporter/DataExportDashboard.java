package com.axelor.studio.service.data.exporter;

import javax.xml.bind.JAXBException;

import com.axelor.db.mapper.Mapper;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Dashboard;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.studio.service.data.DataCommon;
import com.axelor.studio.service.data.DataTranslationService;
import com.google.inject.Inject;

public class DataExportDashboard {
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	private DataTranslationService translationService;
	
	public void export(DataExportService exportService, String name) throws JAXBException {
		
		MetaView view =  metaViewRepo.all().filter(
				"self.type = 'dashboard'  and self.name = ?", 
				name).fetchOne();
		
		if (view != null) {
			
			ObjectViews views = XMLViews.fromXML(view.getXml());
			
			Dashboard dashboard = (Dashboard) views.getViews().get(0);
			
			for (AbstractWidget widget : dashboard.getItems()) {
				String[] values = processDashlet((Dashlet)widget, view.getModule(), null, name, null, null);
				if (values != null) {
					exportService.writeRow(values, true);
				}
			}
			
		}
		
	}
	
	public String[] processDashlet(Dashlet dashlet, String module, String model, String view, Mapper mapper, String parentModule) {
		
		String title = dashlet.getTitle();
		String action = dashlet.getAction();
		
		if (title == null && action != null) {
			Action metaAction = MetaStore.getAction(dashlet.getAction());
			if (metaAction instanceof ActionView) {
				title = ((ActionView) metaAction).getTitle();
			}
		}
		
		String[] values = null;
		if (title != null || dashlet.getName() != null) {
			values = new String[DataCommon.HEADERS.length];
			values[DataCommon.MODULE] = module;
			values[DataCommon.MODEL] =	null; 
			values[DataCommon.VIEW] =	view; 
			values[DataCommon.NAME] = dashlet.getName(); 
			values[DataCommon.TITLE] = title;
			values[DataCommon.TITLE_FR] =	translationService.getTranslation(title, "fr");
			values[DataCommon.TYPE] =	"dashlet"; 
			values[DataCommon.IF_CONFIG] = dashlet.getConditionToCheck();
			values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(dashlet, parentModule);
			
			if (dashlet.getColSpan() != null) {
				values[DataCommon.COLSPAN] = dashlet.getColSpan().toString();
			}
			
		}
		
		return values;
	}
}
