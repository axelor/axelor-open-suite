/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.ReportBuilder;
import com.axelor.text.StringTemplates;
import com.axelor.text.Template;

/**
 * Service class create html report for a record using html template.
 * 
 * @author axelor
 *
 */
public class ReportPrinterService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final Pattern tablePattern = Pattern
			.compile("<table class=\"table table-bordered table-header\">(.*?</table>)");
	private static final Pattern fieldPattern = Pattern
			.compile("<td>\\$(.*?\\$)");
	private static final Pattern rowPattern = Pattern
			.compile("<tr><td>(.*?</tr>)");

	/**
	 * Root method to access the service. It process template of reportBuilder
	 * and generate html from it. It also process fileName of report builder.
	 * 
	 * @param reportBuilder
	 *            ReportBuilder to process;
	 * @param recordId
	 *            Id of record to process.
	 * @return Generated html report and processed file name..
	 */
	public String[] getHtml(ReportBuilder reportBuilder, Long recordId) {

		try {
			String model = reportBuilder.getMetaModel().getFullName();
			@SuppressWarnings("unchecked")
			Model entity = JPA.find((Class<Model>) Class.forName(model),
					recordId);

			StringTemplates strTemplate = new StringTemplates('$', '$');
			String text = getTemplate(reportBuilder);
			if (text == null) {
				return new String[] { I18n.get("No template found") };
			}

			String fileName = reportBuilder.getFileName();
			fileName = fileName.replace("$TODAY$",
					LocalDate.now().toString("ddMMYYYY"));
			fileName = fileName.replace("$NOW$",
					LocalDateTime.now().toString("ddMMYYYYHHmm"));

			Template template = strTemplate.fromText(fileName);
			fileName = template.make(entity).render();

			template = strTemplate.fromText(text);

			return new String[] { fileName, template.make(entity).render() };

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return new String[] { I18n.get("Error in printing") };
	}

	/**
	 * Method find html template from given model. It also process o2m/m2m
	 * tables to match correct syntax of template.
	 * 
	 * @param model
	 *            Model fullName to search template.
	 * @return Html template.
	 */
	private String getTemplate(ReportBuilder reportBuilder) {

		String template = reportBuilder.getHtmlTemplate();

		if (template == null) {
			return null;
		}

		Matcher tableMatcher = tablePattern.matcher(template);

		log.debug("Get template : {}", template);

		while (tableMatcher.find()) {
			String table = tableMatcher.group();
			String tableGroup = tableMatcher.group(1);
			Matcher fieldMatcher = fieldPattern.matcher(tableGroup);

			if (fieldMatcher.find()) {
				String fieldGroup = fieldMatcher.group(1);
				String[] fieldName = fieldGroup.split("\\.");
				if (fieldName.length < 1) {
					continue;
				}
				log.debug("Field name: {}", fieldName[0]);

				Matcher rowMatcher = rowPattern.matcher(tableGroup);
				if (rowMatcher.find()) {
					String row = rowMatcher.group();
					log.debug("Row pattern found: {}", row);
					String rep = row
							.replace("$" + fieldName[0] + ".", "$item.");
					rep = "$" + fieldName[0] + ": {item | " + rep + "}$";
					String tableReplace = tableGroup.replace(row, rep);
					String modifiedTable = table.replace(tableGroup,
							tableReplace);
					template = template.replace(table, modifiedTable);
					log.debug("Modified template: {}", template);
				}
			}
		}

		template = "<div>" + template + "</div>";

		if (reportBuilder.getFooter() != null) {
			template = "<div class=\"footer\">" + reportBuilder.getFooter()
					+ "</div>" + template;
		}

		if (reportBuilder.getHeader() != null) {
			template = "<div class=\"header\">" + reportBuilder.getHeader()
					+ "</div>" + template;
		}

		return template;
	}

}
