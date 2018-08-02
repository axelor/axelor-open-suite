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
package com.axelor.apps.stock.declaration_of_exchanges;

import com.axelor.apps.stock.db.DeclarationOfExchanges;
import java.util.ResourceBundle;

public class DeclarationOfExchangesExporterServices extends DeclarationOfExchangesExporter {
  private static final String NAME_SERVICES = /*$$(*/ "European declaration of services" /*)*/;

  private enum Column implements DeclarationOfExchangesColumnHeader {
    LINE_NUM(/*$$(*/ "Line number" /*$$(*/);

    private final String title;

    private Column(String title) {
      this.title = title;
    }

    @Override
    public String getTitle() {
      return title;
    }
  }

  public DeclarationOfExchangesExporterServices(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(declarationOfExchanges, bundle, NAME_SERVICES, Column.values());
  }

  @Override
  protected String exportToCSV() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected String exportToPDF() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
