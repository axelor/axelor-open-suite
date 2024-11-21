/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportBankStatementFileFormat {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MetaFiles metaFiles;

  @Inject
  public ImportBankStatementFileFormat(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  public Object importBankStatementFileFormat(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatementFileFormat;
    BankStatementFileFormat bankStatementFileFormat = (BankStatementFileFormat) bean;

    String fileName = (String) values.get("binding_file");

    if (!StringUtils.isEmpty(fileName)) {
      try {
        InputStream stream =
            this.getClass()
                .getResourceAsStream(
                    "/data-init/bank-statement-file-format-binding-file/" + fileName);
        if (stream != null) {
          final MetaFile metaFile = metaFiles.upload(stream, fileName);
          bankStatementFileFormat.setBindingFile(metaFile);
        }
      } catch (Exception e) {
        LOG.error("Error when importing demo bank statement : {0}", e);
      }
    }
    return bankStatementFileFormat;
  }
}
