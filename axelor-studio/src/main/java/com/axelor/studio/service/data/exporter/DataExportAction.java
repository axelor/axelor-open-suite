package com.axelor.studio.service.data.exporter;

import java.util.List;

import com.axelor.meta.schema.actions.Action;
import com.axelor.studio.db.ActionBuilder;

public class DataExportAction {
	
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
		"Record filters",
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
	public static final int RECORD_FILTERS = 11;
	public static final int REPORT_BUILDERS = 12;
	public static final int EMAIL_TEMPLATE = 13;
	public static final int LINE_TARGET = 14;
	public static final int LINE_VALUE = 15;
	public static final int LINE_CONDITIONS = 16;
	public static final int LINE_FILTERS = 17;
	public static final int LINE_VALIDATION_TYPE = 18;
	public static final int LINE_VALIDATION_MSG = 19;
	
	public void export(DataWriter writer, List<String> modules) {
		
	}
	
}
