/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import com.axelor.web.StaticResourceProvider;
import java.util.List;

public class StudioStaticResources implements StaticResourceProvider {

  @Override
  public void register(List<String> resources) {
    resources.add("lib/bpmn-js/assets/bpmn-font/css/bpmn-embedded.css");
    resources.add("lib/bpmn-js/assets/diagram-js.css");
    resources.add("css/bpmn.css");
    resources.add("lib/bpmn-js/bpmn-modeler.js");
    resources.add("js/form/form.bpmn.js");
  }
}
