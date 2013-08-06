package com.axelor.apps.base.service.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.TypedQuery;

import com.axelor.apps.base.db.TemplateContextLine;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.Strings;

public class TemplateContextLineService {

	public Object evaluate(TemplateContextLine line, Model bean) {
		
		String query = line.getQuery();
		if(Strings.isNullOrEmpty(query)) {
			return null;
		}
		
		return this.evaluate(query, bean);
	}
	
	public Object evaluate(String query, Model bean) {
		try {
			Class<?> klass = this.extractClass(query);
			System.err.println(klass);
			StringBuilder sb = new StringBuilder(query);
			int n = 0, i = sb.indexOf("?");
			while (i > -1) {
				sb.replace(i, i + 1, "?" + (++n));
				i = sb.indexOf("?", i + 1);
			}

			TypedQuery<?> querie = JPA.em().createQuery(sb.toString(), klass);
			for (int j = 1; j <= n; j++) {
				querie.setParameter(n, bean);
			}
			
			return querie.getResultList();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private Class<?> extractClass(String query) {
		Pattern pattern = Pattern.compile("(from|FROM)(\\s*)(.+?)(\\s)");
		Matcher matcher = pattern.matcher(query);
		
		String klassName = "";
		if (matcher.find()) {
			klassName = matcher.group(3).trim();
		}
		
		MetaModel model = MetaModel.findByName(klassName);
		try {
			return Class.forName(model.getFullName());
		}
		catch(Exception ex){
		}
		return null;
	}
	
	
}
