/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.Document;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BankStatementLineCreateCAMT53ServiceImplTest {

  private static final String CAMT53_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.02";

  private final BankStatementLineCreateCAMT53ServiceImpl service =
      new BankStatementLineCreateCAMT53ServiceImpl(null, null, null, null);

  @TempDir Path tempDir;

  @Test
  void readDocument_validFile_isUnmarshalled() throws Exception {
    Path file =
        write(
            "valid.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + camt53Document("<MsgId>MSG-1</MsgId>"));

    Document document = service.readDocument(file);

    Assertions.assertEquals("MSG-1", document.getBkToCstmrStmt().getGrpHdr().getMsgId());
  }

  @Test
  void readDocument_externalEntity_isRejected() throws Exception {
    Path secret = write("secret.txt", "SECRET");
    Path file =
        write(
            "xxe.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Document [<!ENTITY xxe SYSTEM \""
                + secret.toUri()
                + "\">]>"
                + camt53Document("<MsgId>&xxe;</MsgId>"));

    Assertions.assertThrows(Exception.class, () -> service.readDocument(file));
  }

  protected String camt53Document(String grpHdrContent) {
    return "<Document xmlns=\""
        + CAMT53_NAMESPACE
        + "\"><BkToCstmrStmt><GrpHdr>"
        + grpHdrContent
        + "</GrpHdr></BkToCstmrStmt></Document>";
  }

  protected Path write(String fileName, String content) throws Exception {
    return Files.write(tempDir.resolve(fileName), List.of(content));
  }
}
