package com.axelor.studio.service.data.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ReportBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.exporter.ActionExporter;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ActionImporter {
	
	private Map<String, Integer> typeMap = new HashMap<String, Integer>(); 
	
	private Set<Long> actionCleared;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private ActionBuilderRepository actionBuilderRepo;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private ReportBuilderRepository reportBuilderRepo;
	
	@Inject
	private TemplateRepository templateRepo;
	
	@Inject
	private MenuImporter menuImporter;
	
	public void importActions(DataReader reader, String key) {
		
		setTypeMap();
		actionCleared = new HashSet<Long>();
				
		int totalLines = reader.getTotalLines(key);
		
		for (int ind = 0; ind < totalLines; ind++) {
			String[] row = reader.read(key, ind);
			if (row == null) {
				continue;
			}

			String module = row[ActionExporter.MODULE];
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
	public void importActionBuilder(String[] values, MetaModule module) {
		
		String name = values[ActionExporter.NAME];
		String type = values[ActionExporter.TYPE];

		if (name == null || type == null) {
			return;
		}
		
		ActionBuilder builder = findCreateAction(name, type, module);
		
		builder = setModel(values, builder);
		if (values[ActionExporter.VIEW] != null) {
			builder = menuImporter.setActionViews(builder, values[ActionExporter.VIEW]);
		}
		builder.setFirstGroupBy(values[ActionExporter.FIRST_GROUPBY]);
		builder.setSecondGroupBy(values[ActionExporter.SECOND_GROUPBY]);
		builder.setReportBuilderSet(getReportBuilders(values[ActionExporter.REPORT_BUILDERS]));
		builder.setEmailTemplate(getEmailTemplate(values[ActionExporter.EMAIL_TEMPLATE]));
		builder.setEdited(true);
		builder.setRecorded(false);
		
		builder = actionBuilderRepo.save(builder);
		
		builder = importLines(values, builder);
		
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
	
	private ActionBuilder setModel(String[] values, ActionBuilder builder) {
		
		String model = values[ActionExporter.OBJECT];
		MetaModel metaModel = metaModelRepo.findByName(model);
		builder.setMetaModel(metaModel);
		
		if (metaModel != null) {
			MetaField field = getMetaField(metaModel, values[ActionExporter.TARGET_FIELD]);
			builder.setTargetField(field);
			field = getMetaField(metaModel, values[ActionExporter.LOOOP_FIELD]);
			builder.setLoopOnField(field);
		}
		
		metaModel = metaModelRepo.findByName(values[ActionExporter.TARGET_OBJECT]);
		builder.setTargetModel(metaModel);
		
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
	
	private ActionBuilder importLines(String[] values, ActionBuilder builder) {
		
		if (!actionCleared.contains(builder.getId())) {
			builder.clearLines();
			actionCleared.add(builder.getId());
		}
		
		ActionBuilderLine line = new ActionBuilderLine();
		
		String target =  values[ActionExporter.LINE_TARGET];
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
		
		line.setValue(values[ActionExporter.LINE_VALUE]);
		line.setConditionText(values[ActionExporter.LINE_CONDITIONS]);
		line.setFilter(values[ActionExporter.LINE_FILTERS]);
		line.setValidationMsg(values[ActionExporter.LINE_VALIDATION_MSG]);
		line.setValidationTypeSelect(values[ActionExporter.LINE_VALIDATION_TYPE]);
		
		builder.addLine(line);
		
		return builder;
	}
	

}
