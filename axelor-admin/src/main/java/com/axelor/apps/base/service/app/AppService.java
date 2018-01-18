/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.app;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.axelor.apps.base.db.App;
import com.axelor.exception.AxelorException;

public interface AppService {
	
	public String importDataDemo(App app);
	
	public App getApp(String type);
	
	public boolean isApp(String type);
	
	public List<App> getDepends(App app, Boolean active);
	
	public List<String> getNames(List<App> apps);
	
	public List<App> getChildren(App app, Boolean active);
	
	public App installApp(App app, Boolean importDemo);
	
	public App unInstallApp(App app) throws AxelorException;
	
	public List<App> sortApps(Collection<App> apps);

	public void refreshApp() throws IOException, ClassNotFoundException;
	
	public App updateLanguage(App app, String language);
}
