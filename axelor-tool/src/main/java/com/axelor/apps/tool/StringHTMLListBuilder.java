package com.axelor.apps.tool;

import java.util.ArrayList;
import java.util.List;

/** Class to create a html list as string */
public class StringHTMLListBuilder {

  private List<String> listElements;

  public StringHTMLListBuilder() {
    listElements = new ArrayList<>();
  }

  public void append(String s) {
    listElements.add(s);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<ul>");
    for (String s : listElements) {
      sb.append("<li>");
      sb.append(s);
      sb.append("</li>");
    }
    sb.append("</ul>");
    return sb.toString();
  }
}
