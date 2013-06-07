package com.axelor.exception;

import net.sf.ehcache.CacheManager;

import com.axelor.auth.AuthModule;
import com.axelor.db.JpaModule;
import com.google.inject.AbstractModule;

public class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		// shutdown the cache manager if running : Breaking the test
		if (CacheManager.ALL_CACHE_MANAGERS.size() > 0) {
			CacheManager.getInstance().shutdown();
		}
        install(new JpaModule("testUnit", true, true));
        install(new AuthModule.Simple());
	}
}
