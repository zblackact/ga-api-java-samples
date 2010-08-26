// Copyright 2010 Google Inc. All Rights Reserved.
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * A class which converts a DataFeed object into a series of data rows in the
 * CSV format.
 *
 * @author api.alexl@google.com (Alexander Lucas)
 */
public class AnalyticsCsvPrinter {

  private PrintStream printStream = System.out;

  /**
   * Returns the output stream this object will write output to.
   *
   * @return The output stream this object will write output to.
   */
  public PrintStream getPrintStream() {
    return printStream;
  }

  /**
   * Sets the output stream this object will write output to.
   *
   * @param printStream The output stream this object will write output to.
   */
  public void setPrintStream(PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Prints the contents of a Data Feed in CSV format, to the output stream
   * specified using "setPrintStream".  Uses System.out by default.  This
   * method prints row headers.  To print multiple queries to one output, one
   * can also use "printEntries", which does not print row headers.
   *
   * @param feed The feed whose contents are printed in CSV format.
   */
  public void printFeedAsCsv(DataFeed feed) {
    printRowHeaders(feed);
    printBody(feed);
  }

  /**
   * Prints all the entries in a feed in CSV format.  Does *not* print
   * row headers.
   *
   * @param feed The feed whose contents are printed in CSV format.
   */
  public void printBody(DataFeed feed) {
    if(feed.getEntries().size() == 0) {
      return;
    }

    for (DataEntry entry : feed.getEntries()) {
      printEntry(entry);
    }
  }

  /**
   * Prints all the values in a data feed entry in CSV format.
   *
   * @param entry The data entry whose contents are printed in CSV format.
   */
  public void printEntry(DataEntry entry) {
    Iterator<Dimension> dimensions = entry.getDimensions().iterator();
    while (dimensions.hasNext()) {
      printStream.print(sanitizeForCsv(dimensions.next().getValue()));
      printStream.print(",");
    }

    Iterator<Metric> metrics = entry.getMetrics().iterator();
    while (metrics.hasNext()) {
      printStream.print(sanitizeForCsv(metrics.next().getValue()));
      if (metrics.hasNext()) {
        printStream.print(",");
      }
    }
    printStream.println();
  }

  /**
   * Prints the row headers (column names) of a feed.
   *
   * @param feed The feed whose row headers are to be printed.
   */
  public void printRowHeaders(DataFeed feed) {
    if(feed.getEntries().size() == 0) {
      return;
    }

    DataEntry firstEntry = feed.getEntries().get(0);

    Iterator<Dimension> dimensions = firstEntry.getDimensions().iterator();
    while (dimensions.hasNext()) {
      printStream.print(sanitizeForCsv(dimensions.next().getName()));
      printStream.print(",");
    }

    Iterator<Metric> metrics = firstEntry.getMetrics().iterator();
    while (metrics.hasNext()) {
      printStream.print(sanitizeForCsv(metrics.next().getName()));
      if (metrics.hasNext()) {
        printStream.print(",");
      }
    }
    printStream.println();

  }

  /**
   * Modifies a string so that it is a valid cell in a CSV document.
   * Note that this method works on a single value, not a whole row of data.
   *
   * @param cellData The string to be made CSV-compatible.
   * @return a version of the string which works as a single CSV cell.
   */
  private String sanitizeForCsv(String cellData) {
    // Since most sanitizing will involve wrapping the string in double quotes,
    // check and see if double quotes that are part of the string value already exist.
    // If they do, escape them.  Escaping a double quote is done by placing an
    // extra one right next to it, so " becomes "".
    StringBuilder resultBuilder = new StringBuilder(cellData);
    int lastIndex = 0;
    while (resultBuilder.indexOf("\"", lastIndex) >= 0) {
      int quoteIndex = resultBuilder.indexOf("\"", lastIndex);
      resultBuilder.replace(quoteIndex, quoteIndex + 1, "\"\"");
      lastIndex = quoteIndex + 2;
    }

    // Several conditions are solved by wrapping the value in double quotes.
    // Since this should only happen once per escaped string, we can check
    // for all conditions simultaneously.  The conditions are:
    // -Comma in the cellData value
    // -Line break in the cellData value
    // -Leading or trailing whitespace in the celldata value

    char firstChar = cellData.charAt(0);
    char lastChar = cellData.charAt(cellData.length() - 1);

    if (cellData.contains(",") ||
        cellData.contains("\n") ||
        Character.isWhitespace(firstChar) ||
        Character.isWhitespace(lastChar)) {
        resultBuilder.insert(0, "\"").append("\"");
    }
    return resultBuilder.toString();
  }
}
