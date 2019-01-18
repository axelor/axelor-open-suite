/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.studio.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.w3c.tidy.Tidy;

public class TestReportBuilder {

  @Test
  public void testJsoup() {
    try {
      byte[] encoded =
          Files.readAllBytes(
              Paths.get(
                  "/home/axelor/studio/studio-app/modules/axelor-studio/src/main/resources/css/studio.css"));
      String css = new String(encoded, "utf-8");

      String html =
          "<div><table class=\"table no-border\">"
              + "<tbody><tr><td><table><tbody><tr><td><h4><u>Panel</u></h4><p><u><br></u></p><p>"
              + "<span style=\"text-decoration: underline; background-color: rgb(63, 207, 255);\">"
              + "TESTING</span></p></td></tr><tr><td><b>Status</b> : Validate</td><td><b>Customer</b> :"
              + "test</td></tr><tr><td><b>Order no.</b> : </td><td><b>Order date</b> : </td></tr><tr><td "
              + "colspan=\"2\"><h4>Lines</h4></td></tr><tr><td colspan=\"2\">"
              + "<table class=\"table table-bordered table-header\"><tbody><tr><th>Sequence</th>"
              + "<th>Product</th><th>Descripiton</th><th>Qty</th><th>Total</th><th>Price</th></tr>"
              + "<tr><td>0</td><td>Product2</td><td>sdas</td><td>2</td><td>44.00</td><td>22.00</td></tr>"
              + "</tbody></table></td></tr><tr><td><b>Total</b> : 44.00</td></tr></tbody></table></td></tr>"
              + "</tbody></table></div>";
      html = "<div class=\"content\">" + html + "</div>";
      html =
          "<html><head><style type=\"text/css\">"
              + css
              + "</style></head><body>"
              + html
              + "</body></html>";

      html =
          "<table class=\"table no-border\"><tbody><tr><td><table><tbody><tr><td><h4><u>Panel</u>"
              + "</h4><p><u><br></u></p><p><span style=\"text-decoration: underline; background-color: rgb(63, 207, 255);\">"
              + "TESTING</span></p></td></tr><tr><td><b>Status</b> : Validate</td><td><b>Customer</b> : test</td>"
              + "</tr><tr><td><b>Order no.</b> : </td><td><b>Order date</b> : </td></tr><tr><td colspan=\"2\">"
              + "<h4><span style=\"font-size: 13px;\">Total</span><span style=\"font-size: 13px; font-weight: normal;\">"
              + "&nbsp;: 44.00</span><br></h4></td></tr><tr><td><br></td></tr></tbody></table></td></tr></tbody></table>";

      Tidy tidy = new Tidy();
      tidy.setXHTML(true);
      tidy.setShowWarnings(false);
      tidy.setTidyMark(false);
      tidy.setDocType("omit");
      ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      tidy.parse(in, out);

      System.out.println(new String(out.toByteArray()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
