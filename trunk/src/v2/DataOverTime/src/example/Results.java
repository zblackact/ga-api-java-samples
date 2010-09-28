// Copyright 2010 Google Inc. All Rights Reserved.

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import com.google.gdata.client.analytics.DataQuery;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a data object that represents a Table. The table has lists
 * to store column and row labels. It also has a flag to determine if
 * the data has been sampled. This table can be output to the console
 * or output to a file.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class Results {

  private List<String> rowNames;
  private List<String> originalDimensionValues;
  private List<List<Double>> table;
  private List<String> colNames;
  private String dimensionName = "";
  private boolean isSampled;

  /**
   * Initializes the table.
   * @param dataQuery The initial DataQuery object used to get the list of
   *     dimensions from the API.
   * @param dimensionValues A list of the dimension values returned from the
   *     dataQuery parameter.
   */
  public void initTable(DataQuery dataQuery, List<String> dimensionValues) {
    table = new ArrayList<List<Double>>(dimensionValues.size());
    originalDimensionValues = dimensionValues;
    rowNames = new ArrayList<String>(dimensionValues.size());
    dimensionName = dataQuery.getDimensions();
    setColNames(DataQueryUtil.getListOfDates(dataQuery));
    isSampled = false;
  }

  /**
   * Prints the table to the standard output.
   */
  public void printCsv() {
    outputCsv(System.out);
  }

  /**
   * Prints the table to a file.
   *
   * @param fileName The name of the file to print to.
   * @throws FileNotFoundException If the file is not found.
   */
  public void printCsvToFile(String fileName) throws FileNotFoundException {
    FileOutputStream out = new FileOutputStream(fileName);
    PrintStream fileOutput = new PrintStream(out);
    outputCsv(fileOutput);
    fileOutput.close();
  }

  /**
   * Prints the table as a CSV file. Prints if any sampling occurred, the name
   * of the dimension along with all the column names, and each row including
   * the row name and the data.
   *
   * @param output An object that implements PrintStream, like a file or
   *     standard output.
   */
  public void outputCsv(PrintStream output) {

    Double total = 0.0;

    // Print sampled disclaimer.
    if (isSampled) {
      output.println("These results are based on sampled data");
    }

    // Print header.
    output.print(dimensionName);
    for (String colName : colNames) {
      output.print(MessageFormat.format(",{0}", colName));
    }
    output.print(",Total\n");

    // Print main table.
    for (int i = 0; i<table.size(); i++) {
      output.print(rowNames.get(i));

      List<Double> row = table.get(i);
      Iterator<Double> valueIter = row.iterator();
      while (valueIter.hasNext()) {
        Double intValue = valueIter.next();
        total += intValue;
        output.print(MessageFormat.format(",{0}", intValue.toString()));
      }
      output.print(MessageFormat.format(",{0}\n", total.toString()));
      total = 0.0;
    }
  }

  /**
   * Adds a row and it's row name to the table.
   * @param rowName The name of a row.
   * @param row A list of Integers representing each value.
   */
  public void addRow(String rowName, List<Double> row) {
    rowNames.add(rowName);
    table.add(row);
  }

  /**
   * Adds a row to the table and uses the originalDimensionValue
   * as the row name.
   * @param row A list of Integers representing each value.
   */
  public void addRow(List<Double> row) {
    table.add(row);
    rowNames.add(originalDimensionValues.get(table.size()));
  }

  /**
   * @return The table.
   */
  public List<List<Double>> getTable() {
    return table;
  }

  /**
   * Sets the table.
   * @param table The table to set.
   */
  public void setTable(List<List<Double>> table) {
    this.table = table;
  }

  /**
   * @return The number of rows in the table.
   */
  public int getNumRows() {
    return originalDimensionValues.size();
  }

  /**
   * @return the number of columns in the table.
   */
  public int getNumCols() {
    return colNames.size();
  }

  /**
   * Sets the list of row names.
   * @param rowNames The list of row names.
   */
  public void setRowNames(List<String> rowNames) {
    this.rowNames = rowNames;
  }

  /**
   * @return The list of row names.
   */
  public List<String> getRowNames() {
    return rowNames;
  }

  /**
   * Sets the list of column names.
   * @param colNames The list of column names.
   */
  public void setColNames(List<String> colNames) {
    this.colNames = colNames;
  }

  /**
   * @return The list of column names.
   */
  public List<String> getColNames() {
    return colNames;
  }

  /**
   * Sets the dimension name.
   * @param dimensionName The dimension name.
   */
  public void setDimensionName(String dimensionName) {
    this.dimensionName = dimensionName;
  }

  /**
   * @return The dimension name.
   */
  public String getDimensionName() {
    return dimensionName;
  }

  /**
   * @return The list of original dimension values.
   */
  public List<String> getOriginalDimensionValues() {
    return originalDimensionValues;
  }

  /**
   * Sets whether this data has been sampled. Since the default is false,
   * only update isSampled if it's true.
   *
   * @param isSampled Whether this data is sampled.
   */
  public void setIsSampled(boolean isSampled) {
    if (isSampled) {
      this.isSampled = isSampled;
    }
  }

  /**
   * @return if the data was sampled.
   */
  public boolean getIsSampled() {
    return isSampled;
  }
}
