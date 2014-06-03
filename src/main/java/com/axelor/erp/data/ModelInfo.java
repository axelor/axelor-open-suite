/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.erp.data;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.axelor.db.JPA;
import com.axelor.db.annotations.Widget;
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
