/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.erp.data;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.axelor.db.JPA;
import com.axelor.db.Widget;
import com.csvreader.CsvWriter;

//Generate csv file with mapping of object fields to csv column"

public class ModelInfo {
	
	public static void main(String[] args) throws IOException {
		CsvWriter cw = new CsvWriter("/home/axelor/Desktop/ModelFields.csv");
		String[] record =  new String[]{"Object","Field Name","Label","Type","Ref","ImportFile","ColumnMapped"};
		cw.writeRecord(record);
		ArrayList<String> models = new ArrayList<String>();
		for(Class<?> model : JPA.models())
			models.add(model.getSimpleName());
		try{
			CSVReader cr = new CSVReader(new FileReader("/home/axelor/Desktop/Rubriques_KOALA_20120516.csv"));
			List<String[]> csv = cr.readAll();
			for(Class<?> model : JPA.models()){
				String name = model.getSimpleName().toString();
				for(Field field : model.getDeclaredFields()){
					String fieldName = field.getName().toString();
					if(!fieldName.contains("$") && !fieldName.equals("id")){
						String fieldType = field.getType().getSimpleName().toString();
						String ref = "";
						if(fieldType.equals("List") || fieldType.equals("Set")) {
							Class<?> obj = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
							ref = obj.getSimpleName();
						}
						fieldType = fieldType.replace("List", "OneToMany").replace("Set", "ManyToMany");
						if(models.contains(fieldType)) {
							ref = fieldType;
							fieldType = "ManyToOne";
						}
						Widget wd = field.getAnnotation(Widget.class);
						String title = "";
						if(wd != null)
							title = wd.title().toString();
						String fileName = "";
						String columnName = "";
						for(String[] line : csv) {
							if(line[6].equals(name) && line[7].equals(fieldName)) {
								fileName = fileName+"\n"+line[0];
								columnName = columnName+"\n"+line[1];
							}
								
						}
						String rec = name+','+fieldName+','+title+','+fieldType+','+ref+','+fileName+','+columnName;
						cw.writeRecord(rec.split(","));
					}
				}
			}
			cr.close();
		}catch (Exception e) {
			e.printStackTrace();
			cw.close();
		}
		cw.close();
		
		
	}

}
