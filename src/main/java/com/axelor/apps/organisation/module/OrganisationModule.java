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
package com.axelor.apps.organisation.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.organisation.service.invoice.InvoiceGeneratorOrganisation;
import com.axelor.apps.organisation.service.invoice.InvoiceLineGeneratorOrganisation;

@AxelorModuleInfo(name = "axelor-organisation")
public class OrganisationModule extends AxelorModule {

    @Override
    protected void configure() {
        
        bind(InvoiceGenerator.class).to(InvoiceGeneratorOrganisation.class);
        bind(InvoiceLineGenerator.class).to(InvoiceLineGeneratorOrganisation.class);
    }
}