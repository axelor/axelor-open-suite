package com.axelor.studio.service.data.exporter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelor.apps.message.db.Template;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ReportBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepo;
import com.google.inject.Inject;

public class ExportAction {
	
	public static final String[] HEADERS = new String[] {
		"Notes",
		"Module",
		"Name",
		"Object",
		"Type",
		"View",
		"Target object",
		"Target field",
		"Loop on field",
		"First groupby",
		"Second groupby",
		"Report builders",
		"Email template",
		"Line Target",
		"Line Value",
		"Line Conditions",
		"Line Filters",
		"Line Validation type",
		"Line Validation message"
	};
	
	
	public static final int MODULE = 1;
	public static final int NAME = 2;
	public static final int OBJECT = 3;
	public static final int TYPE = 4;
	public static final int VIEW = 5;
	public static final int TARGET_OBJECT = 6;
	public static final int TARGET_FIELD = 7;
	public static final int LOOOP_FIELD = 8;
	public static final int FIRST_GROUPBY = 9;
	public static final int SECOND_GROUPBY = 10;
	public static final int REPORT_BUILDERS = 11;
	public static final int EMAIL_TEMPLATE = 12;
	public static final int LINE_TARGET = 13;
	public static final int LINE_VALUE = 14;
	public static final int LINE_CONDITIONS = 15;
	public static final int LINE_FILTERS = 16;
	public static final int LINE_VALIDATION_TYPE = 17;
	public static final int LINE_VALIDATION_MSG = 18;
	
	public static final List<Integer> LINE_TYPES = Arrays.asList(new Integer[]{0,1,5});
	
	private Map<Integer, String> typeMap = new HashMap<Integer, String>();
	
	@Inject
	private ActionBuilderRepo actionBuilderRepo;
	
	public void export(DataWriter writer) {
		
		setTypeMap();
		
		List<ActionBuilder> actionBuilders = actionBuilderRepo.all().fetch();
		
		writer.write("Actions", null, HEADERS);
		
		for (ActionBuilder builder : actionBuilders) {
			
			String[] values = extractBuilder(builder);
			
			if (LINE_TYPES.contains(builder.getTypeSelect()) && !builder.getLines().isEmpty()) {
				for (ActionBuilderLine line : builder.getLines()) {
					String[] lineVals = extractLine(line, values);
					writer.write("Actions", null, lineVals);
				}
			}
			else {
				writer.write("Actions", null, values);
			}
			
		}
		
	}
	
	private void setTypeMap() {
		
		typeMap = new HashMap<Integer, String>();
		
		for (Option option : MetaStore.getSelectionList("studio.action.builder.type.select")) {
			typeMap.put(Integer.parseInt(option.getValue()), option.getTitle());
		}
	}
	
	private String[] extractBuilder(ActionBuilder builder) {
		
		String[] values = new String[HEADERS.length];
		
		values[MODULE] = builder.getMetaModule().getName();
		values[NAME] = builder.getName();
		MetaModel model = builder.getMetaModel();
		if (model != null) {
			values[OBJECT] = model.getName();
		}
		
		values[TYPE] = typeMap.get(builder.getTypeSelect());
		ViewBuilder view = builder.getViewBuilder();
		if (view != null) {
			values[VIEW] = builder.getViewBuilder().getName();
		}
		
		model = builder.getTargetModel();
		if (model != null) {
			values[TARGET_OBJECT] = model.getName();
		}
		
		MetaField field = builder.getTargetField();
		if (field != null) {
			values[TARGET_FIELD] = field.getName();
		}
		
		field = builder.getLoopOnField();
		if (field != null) {
			values[LOOOP_FIELD] = field.getName();
		}
		
		values[FIRST_GROUPBY] = builder.getFirstGroupBy();
		values[SECOND_GROUPBY] = builder.getSecondGroupBy();
		values[REPORT_BUILDERS] = getReportBuilders(builder.getReportBuilderSet());
		
		Template template = builder.getEmailTemplate();
		if (template != null) {
			values[EMAIL_TEMPLATE] = template.getName();
		}
		
		return values;
	}
	
	private String getReportBuilders(Set<ReportBuilder> reportBuilders) {
		
		String reports = null;
		
		for (ReportBuilder reportBuilder : reportBuilders) {
			if (reports == null) {
				reports = reportBuilder.getName();
			}
			else {
				reports += "," + reportBuilder.getName();
			}
		}
		
		return reports;
	}
	
	private String[] extractLine(ActionBuilderLine line, String[] values) {
		
		String[] vals = Arrays.copyOf(values, HEADERS.length);
		
		vals[LINE_TARGET] = line.getTargetField();
		vals[LINE_VALUE] = line.getValue();
		vals[LINE_CONDITIONS] = line.getConditionText();
		vals[LINE_FILTERS] = line.getFilter();
		vals[LINE_VALIDATION_TYPE] = line.getValidationTypeSelect();
		vals[LINE_VALIDATION_MSG] = line.getValidationMsg();
		
		return vals;
	}
	
	
	
}
