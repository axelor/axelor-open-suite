/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportBankStatement {

  protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyy");

  protected static final String DATE_PATTERN =
      "(TODAY|\\d{4}-\\d{2}-\\d{2})(\\[((?:[\\+\\-=]?\\d{1,4}y)?(?:[\\+\\-=]?\\d{1,2}M)?(?:[\\+\\-=]?\\d{1,2}d)?)\\])?";

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankStatementImportService bankStatementImportService;
  protected MetaFiles metaFiles;
  protected BankStatementRepository bankStatementRepository;
  protected BankStatementCreateService bankStatementCreateService;
  protected ImportDateTime importDateTime;

  @Inject
  public ImportBankStatement(
      BankStatementImportService bankStatementImportService,
      MetaFiles metaFiles,
      BankStatementRepository bankStatementRepository,
      BankStatementCreateService bankStatementCreateService,
      ImportDateTime importDateTime) {
    this.bankStatementImportService = bankStatementImportService;
    this.metaFiles = metaFiles;
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementCreateService = bankStatementCreateService;
    this.importDateTime = importDateTime;
  }

  public Object importBankStatement(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatement;
    BankStatement bankStatement = (BankStatement) bean;

    String fileName = (String) values.get("bank_statement_demo");

    if (!StringUtils.isEmpty(fileName)) {
      try {
        InputStream stream =
            this.getClass().getResourceAsStream("/apps/demo-data/demo-bank-statement/" + fileName);
        if (stream != null) {
          String rawContent =
              new BufferedReader(new InputStreamReader(stream))
                  .lines()
                  .collect(Collectors.joining("\n"));
          String processedContent = preprocessAfb120Content(rawContent);
          stream = new ByteArrayInputStream(processedContent.getBytes(StandardCharsets.UTF_8));

          final MetaFile metaFile = metaFiles.upload(stream, fileName);
          bankStatement.setBankStatementFile(metaFile);
          bankStatementRepository.save(bankStatement);
          bankStatementImportService.runImport(bankStatement, true);
        }
      } catch (Exception e) {
        LOG.error("Error when importing demo bank statement : {0}", e);
      }
    }
    return bankStatementRepository.find(bankStatement.getId());
  }

  protected String preprocessAfb120Content(String content) {
    Pattern pattern = Pattern.compile(DATE_PATTERN);
    Matcher matcher = pattern.matcher(content);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String expression = matcher.group();
      String isoDate = importDateTime.importDate(expression);
      String formattedDate = LocalDate.parse(isoDate.substring(0, 10)).format(DATE_FORMAT);

      matcher.appendReplacement(sb, formattedDate);
    }

    matcher.appendTail(sb);
    return sb.toString();
  }
}
