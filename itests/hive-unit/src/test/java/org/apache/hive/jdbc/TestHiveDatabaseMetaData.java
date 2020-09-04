/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.jdbc;

import org.apache.hive.jdbc.HiveConnection;
import org.apache.hive.jdbc.Utils;
import org.apache.hive.jdbc.Utils.JdbcConnectionParams;

import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TestHiveDatabaseMetaData.
 *
 */
public class TestHiveDatabaseMetaData {

  private static Map<String, String> map = new LinkedHashMap<String, String>();
  private JdbcConnectionParams jdbcConnectionParams = new JdbcConnectionParams();
  private static HiveDatabaseMetaData hiveDatabaseMetaData;
  private static HiveConnection connection = new HiveConnection();

  @Before
  public void setup() throws Exception {
    jdbcConnectionParams.setHiveConfs(map);
    connection.setConnParams(jdbcConnectionParams);
    hiveDatabaseMetaData = new HiveDatabaseMetaData(connection, null, null);
  }


  @Test
  public void test() throws SQLException {
    // We need to invoke all the tests inside a single method, since JdbcConnectionParams is a static class
    // and that can affect the results, especially when tests are run in parallel.
    testHiveConnectionUdateServerHiveConf();
    testGetHiveDefaultNullsLastNullConfig();
    testGetHiveDefaultNullsLast();
    testNullsAreSortedHigh();
    testNullsAreSortedLow();
  }

  public static void testGetHiveDefaultNullsLastNullConfig() {
    map.remove(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY);
    try {
      hiveDatabaseMetaData.nullsAreSortedLow();
      fail("SQLException is expected");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("HIVE_DEFAULT_NULLS_LAST is not available"));
    }
  }

  public static void testGetHiveDefaultNullsLast() throws SQLException {
    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "true");
    assertTrue(hiveDatabaseMetaData.getHiveDefaultNullsLast(map));

    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "false");
    assertFalse(hiveDatabaseMetaData.getHiveDefaultNullsLast(map));

  }

  public static void testNullsAreSortedHigh() throws SQLException {
    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "false");
    assertFalse(hiveDatabaseMetaData.nullsAreSortedHigh());
    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "true");
    assertTrue(hiveDatabaseMetaData.nullsAreSortedHigh());
  }

  public static void testNullsAreSortedLow() throws SQLException {
    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "false");
    assertTrue(hiveDatabaseMetaData.nullsAreSortedLow());
    map.put(Utils.JdbcConnectionParams.HIVE_DEFAULT_NULLS_LAST_KEY, "true");
    assertFalse(hiveDatabaseMetaData.nullsAreSortedLow());
  }

  public static void testHiveConnectionUdateServerHiveConf() {
    Map<String, String> serverHiveConf = new HashMap<>();
    map.put("hive.server2.thrift.resultset.default.fetch.size", Integer.toString(87));
    map.put("hive.default.nulls.last", "false");

    connection.getConnParams().getHiveConfs().put(Utils.JdbcConnectionParams.HIVE_CONF_PREFIX
        + "hive.server2.thrift.resultset.default.fetch.size", "1534");
    connection.updateServerHiveConf(serverHiveConf);

    // Server configuration should be updated, since its not provided by the client.
    assertEquals("false", connection.getConnParams().getHiveConfs()
        .get(Utils.JdbcConnectionParams.HIVE_CONF_PREFIX + "hive.default.nulls.last"));

    // Client configuration should not be overridden by the server configuration.
    assertEquals("1534", connection.getConnParams().getHiveConfs().get(Utils.JdbcConnectionParams.HIVE_CONF_PREFIX
        + "hive.server2.thrift.resultset.default.fetch.size"));
  }
}