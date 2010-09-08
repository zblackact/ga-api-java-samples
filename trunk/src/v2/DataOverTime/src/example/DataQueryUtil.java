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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provides utility methods for handling DataQuery objects.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */
public class DataQueryUtil {

  private static final long millisInDay = 24 * 60 * 60 * 1000;

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static Calendar calendar = Calendar.getInstance();

  /**
   * Returns the number of days in the date range of the dataQuery object.
   * This includes the final day.
   * @param dataQuery A DataQuery object which has start and end date
   *     parameters set.
   * @return The number of days in the date range. Inclusive.
   */
  public static int getNumberOfDays(DataQuery dataQuery) {
    long startDay = 0;
    long endDay = 0;

    try {
      calendar.setTime(dateFormat.parse(dataQuery.getStartDate()));
      startDay = calendar.getTimeInMillis() / millisInDay;

      calendar.setTime(dateFormat.parse(dataQuery.getEndDate()));
      endDay = calendar.getTimeInMillis() / millisInDay;
    } catch (ParseException e) {
      System.err.println("Error parsing date: " + e.getMessage());
      System.exit(0);
    }

    return (int) (endDay - startDay + 1);
  }

  /**
   * Returns a list of strings that represent each date in the date range.
   * Inclusive.
   * @param dataQuery The DataQuery object with start and end date set.
   * @return A list of strings representing dates in the date range.
   */
  public static List<String> getListOfDates(DataQuery dataQuery) {
    Calendar cal = Calendar.getInstance();
    int numDays = getNumberOfDays(dataQuery);
    List<String> output = new ArrayList<String>(numDays);

    output.add(dataQuery.getStartDate());

    try {
      cal.setTime(dateFormat.parse(dataQuery.getStartDate()));
    } catch (ParseException e) {
      System.err.println("Error parsing date: " + e.getMessage());
      System.exit(0);
    }

    // Start from 1 since there is already a day in the list.
    for (int i = 1; i < numDays; i++) {
      cal.add(Calendar.DATE, 1);
      output.add(dateFormat.format(cal.getTime()));
    }
    return output;
  }

  /**
   * Removes all - characters from the date. To be used to transform the date
   * format used in data Analytics API Data Queries and the date format found
   * in the API response.
   * @param queryDate A date format used Data Export API queries..
   * @return A date format returned by the API.
   */
  public static String getResultDateFormat(String queryDate) {
    return queryDate.replaceAll("-", "");
  }
}
