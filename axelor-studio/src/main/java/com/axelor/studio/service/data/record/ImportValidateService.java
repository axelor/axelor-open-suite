package com.axelor.studio.service.data.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.axelor.studio.db.RecordImportRule;
import com.axelor.studio.db.RecordImportRuleLine;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.common.base.Joiner;

public class ImportValidateService {
	
	public String validate(Map<String, RecordImportRule> ruleMap, String[] tabNames, DataReader reader) {
		
		String log = "";
		for (String tab : tabNames) {
			String notMapped = validateTab(ruleMap.get(tab), reader, tab);
			if (notMapped != null) {
				log += "\n " + tab + " : " + notMapped;
			}
		}
		
		if (!log.isEmpty()) {
			log = "No mapping found for following columns: \n" +  log;
		}
		
		return log;
	}
	
	private String validateTab(RecordImportRule recordImportRule, DataReader reader, String tab) {
		
		String notMapped = null;
		List<String> header = new ArrayList<String>();
		header.addAll(Arrays.asList(reader.read(tab, 0)));
		header = validateHeader(recordImportRule.getLines(), header);
		
		if (!header.isEmpty()) {
			notMapped = Joiner.on(",").join(header);
		}
		
		return notMapped;
	}

	private List<String> validateHeader(List<RecordImportRuleLine> lines, List<String> header) {
		
		for (RecordImportRuleLine line : lines) {
			
			String column = line.getColumnName();
			RecordImportRule refRule = line.getRefRule();
			if (refRule != null) {
				header = validateHeader(refRule.getLines(), header);
			}
			else if (header.contains(column)){
				header.remove(column);
			}
		}
		
		return header;
	}
}
