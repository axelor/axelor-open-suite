package com.axelor.exception.service;

import org.hibernate.proxy.HibernateProxy;

import com.axelor.db.Model;
import com.google.inject.Singleton;

@Singleton
public class AppServiceImpl implements AppService {
	
	@SuppressWarnings("unchecked")
	public Class<? extends Model> getPersistentClass(Model model) {
		if (model instanceof HibernateProxy) {
			return ((HibernateProxy) model).getHibernateLazyInitializer().getPersistentClass();
		} else {
			return model.getClass();
		}
	}

}
