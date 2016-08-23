package com.axelor.studio.service.data.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ReportBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ReportBuilderRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.exporter.ExportAction;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportAction extends CommonService {
	
	private Map<String, Integer> typeMap = new HashMap<String, Integer>(); 
	
	private Set<Long> actionCleared;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private ActionBuilderRepository actionBuilderRepo;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private ViewBuilderRepository viewBuilderRepo;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private ReportBuilderRepository reportBuilderRepo;
	
	@Inject
	private TemplateRepository templateRepo;
	
	public void importActions(XSSFSheet sheet) {
		
		setTypeMap();
		actionCleared = new HashSet<Long>();
				
		Iterator<Row> rowIter = sheet.rowIterator();
		rowIter.next();
		
		while(rowIter.hasNext()) {
			Row row = rowIter.next();
			
			String module = getValue(row, ExportAction.MODULE);
			if (module == null) {
				continue;
			}
				
			MetaModule metaModule = configService.getCustomizedModule(module);
			if (metaModule != null) {
				importActionBuilder(row, metaModule);
			}
		}
		
	}
	
	private void setTypeMap() {
		
		typeMap = new HashMap<String, Integer>();
		
		for (Option option : MetaStore.getSelectionList("studio.action.builder.type.select")) {
			typeMap.put(option.getTitle(), Integer.parseInt(option.getValue()));
		}
	}
	
	@Transactional
	public void importActionBuilder(Row row, MetaModule module) {
		
		String name = getValue(row, ExportAction.NAME);
		String type = getValue(row, ExportAction.TYPE);

		if (name == null || type == null) {
			return;
		}
		
		ActionBuilder builder = findCreateAction(name, type, module);
		
		builder = setModel(row, builder);
		builder = setView(row, builder);
		builder.setFirstGroupBy(getValue(row, ExportAction.FIRST_GROUPBY));
		builder.setSecondGroupBy(getValue(row, ExportAction.SECOND_GROUPBY));
		builder.setReportBuilderSet(getReportBuilders(getValue(row, ExportAction.REPORT_BUILDERS)));
		builder.setEmailTemplate(getEmailTemplate(getValue(row, ExportAction.EMAIL_TEMPLATE)));
		builder.setEdited(true);
		builder.setRecorded(false);
		
		builder = actionBuilderRepo.save(builder);
		
		builder = importLines(row, builder);
		
		actionBuilderRepo.save(builder);
		
	}

	private ActionBuilder findCreateAction(String name, String type,
			MetaModule module) {

		Integer typeSelect = typeMap.get(type); 
		ActionBuilder builder = actionBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule = ?2 and self.typeSelect = ?3",
						name, typeSelect, module).fetchOne();
		
		
		if (builder == null) {
			builder = new ActionBuilder(name);
			builder.setTypeSelect(typeSelect);
			builder.setMetaModule(module);
		}
		
		return builder;
	}
	
	private ActionBuilder setModel(Row row, ActionBuilder builder) {
		
		String model = getValue(row, ExportAction.OBJECT);
		MetaModel metaModel = metaModelRepo.findByName(model);
		builder.setMetaModel(metaModel);
		
		if (metaModel != null) {
			MetaField field = getMetaField(metaModel, getValue(row, ExportAction.TARGET_FIELD));
			builder.setTargetField(field);
			field = getMetaField(metaModel, getValue(row, ExportAction.LOOOP_FIELD));
			builder.setLoopOnField(field);
		}
		
		metaModel = metaModelRepo.findByName(getValue(row, ExportAction.TARGET_OBJECT));
		builder.setTargetModel(metaModel);
		
		return builder;
	}
	
	private ActionBuilder setView(Row row, ActionBuilder builder) {
		
		String view = getValue(row, ExportAction.VIEW);
		if (view != null) {
			ViewBuilder viewBuilder = viewBuilderRepo
					.all()
					.filter("self.name = ?1 and self.metaModule = ?2", view, builder.getMetaModule()).fetchOne();
			builder.setViewBuilder(viewBuilder);
		}
		
		return builder;
	}
	
	
	private MetaField getMetaField(MetaModel model, String name) {
		
		if (name != null) {
			return metaFieldRepo
					.all()
					.filter("self.name = ?1 and self.metaModel = ?2", name, model).fetchOne();
		}
		
		return null;
	}
	
	private Set<ReportBuilder> getReportBuilders(String names) {
		
		if (names == null) {
			return null;
		}
		
		List<String> nameList = Arrays.asList(names.split(","));
		
		List<ReportBuilder> reportBuilders = reportBuilderRepo.all().filter("self.name in (?1)", nameList).fetch();
		
		Set<ReportBuilder> reportBuilderSet = new HashSet<ReportBuilder>();
		reportBuilders.addAll(reportBuilders);
		
		return reportBuilderSet;
	}
	
	
	private Template getEmailTemplate(String name) {
		
		if (name == null) {
			return null;
		}
		
		return templateRepo.findByName(name);
	}
	
	private ActionBuilder importLines(Row row, ActionBuilder builder) {
		
		if (!actionCleared.contains(builder.getId())) {
			builder.clearLines();
			actionCleared.add(builder.getId());
		}
		
		ActionBuilderLine line = new ActionBuilderLine();
		
		String target =  getValue(row, ExportAction.LINE_TARGET);
		if (target != null) {
			line.setTargetField(target);
			if (target.contains(".")) {
				target = target.split("\\.")[0];
			}
			if (builder.getMetaModel() != null) {
				MetaField field = metaFieldRepo.all()
						.filter("self.name = ?1 and self.metaModel = ?2", 
						 target, builder.getMetaModel()).fetchOne();
				line.setMetaField(field);
			}
		}
		
		line.setValue(getValue(row, ExportAction.LINE_VALUE));
		line.setConditionText(getValue(row, ExportAction.LINE_CONDITIONS));
		line.setFilter(getValue(row, ExportAction.LINE_FILTERS));
		line.setValidationMsg(getValue(row, ExportAction.LINE_VALIDATION_MSG));
		line.setValidationTypeSelect(getValue(row, ExportAction.LINE_VALIDATION_TYPE));
		
		builder.addLine(line);
		
		return builder;
	}
	
	

}
