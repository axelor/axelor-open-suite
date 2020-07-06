/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatement.file.ofx;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.inject.Beans;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OfxParserService {

  public List<String> openTags = new LinkedList<>();
  public List<Map<String, Object>> transactions = new LinkedList<>();
  public Map<String, Object> innerFields;
  public Currency currency;

  public List<Map<String, Object>> parse(String content) throws IOException {
    processInput(content, 0, transactions);
    return transactions;
  }

  private void processInput(String content, int position, List<Map<String, Object>> collector) {
    if (content.length() == position) {
      end();

    } else if (content.startsWith("</", position)) {
      int close = content.indexOf(">", position);
      closeTag(content.substring(position + 2, close));
      processInput(content, close + 1, collector);

    } else if (content.startsWith("<", position)) {
      int close = content.indexOf(">", position);
      openTag(content.substring(position + 1, close));
      processInput(content, close + 1, collector);

    } else {
      int next = content.indexOf("<", position);

      if (next == -1) {
        next = content.length();
      }

      String text = content.substring(position, next).trim();
      if (!text.isEmpty()) {
        text(text);
      }

      processInput(content, next, collector);
    }
  }

  private void end() {}

  private void openTag(String tag) {
    openTags.add(tag);
    if (tag.equals("STMTTRN")) {
      innerFields = new HashMap<String, Object>();
      transactions.add(innerFields);
    }
  }

  private void closeTag(String tag) {
    while (!lastOpenTag().equals(tag)) {
      closeTag(lastOpenTag());
    }
    openTags.remove(openTags.size() - 1);
  }

  private void text(String text) {
    if (lastOpenTag().equals("TRNAMT")) {
      if (text.startsWith("-")) {
        lastTransaction().put("debit", new BigDecimal(text));
      } else if (text.startsWith("+")) {
        lastTransaction().put("credit", new BigDecimal(text));
      } else {
        throw new UnsupportedOperationException();
      }
    } else if (lastOpenTag().equals("DTPOSTED")) {
      lastTransaction()
          .put(
              "operationDate",
              LocalDate.of(
                  Integer.parseInt(text.substring(0, 4)),
                  Integer.parseInt(text.substring(4, 6)),
                  Integer.parseInt(text.substring(6, 8))));
    } else if (lastOpenTag().equals("NAME")) {
      lastTransaction().put("description", text);
      lastTransaction().put("currency", currency);
    } else if (lastOpenTag().equals("FITID")) {
      lastTransaction().put("reference", text);
    } else if (lastOpenTag().equals("CURDEF")) {
      currency = Beans.get(CurrencyRepository.class).findByCode(text);
    }
  }

  private String lastOpenTag() {
    return openTags.size() == 0 ? "" : openTags.get(openTags.size() - 1);
  }

  private Map<String, Object> lastTransaction() {
    return transactions.size() == 0 ? null : transactions.get(transactions.size() - 1);
  }
}
