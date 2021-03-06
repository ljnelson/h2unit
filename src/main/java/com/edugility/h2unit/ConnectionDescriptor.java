/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2011-2013 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.h2unit;

import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import org.junit.runner.Description;

public class ConnectionDescriptor extends Properties implements DataSource, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String CATALOG = "com.edugility.h2unit.catalog";

  public static final String CONNECTION_URL = "javax.persistence.jdbc.url";

  public static final String DATA_SOURCE = "com.edugility.h2unit.datasource";

  public static final String DESCRIPTION = "org.junit.runner.Description";

  public static final String DRIVER_CLASS_NAME = "javax.persistence.jdbc.driver";

  public static final String PASSWORD = "javax.persistence.jdbc.password";

  public static final String SCHEMA = "com.edugility.h2unit.schema";

  public static final String USERNAME = "javax.persistence.jdbc.user";


  /*
   * Constructors.
   */


  public ConnectionDescriptor() {
    this(System.getProperties());
  }

  public ConnectionDescriptor(final Properties properties) {
    super(properties);
  }

  public ConnectionDescriptor(final DataSource dataSource) {
    this();
    if (dataSource != null) {
      this.put(DATA_SOURCE, dataSource);
    }
  }

  public ConnectionDescriptor(final String connectionUrlOrLookupString) throws NamingException {
    this();
    if (connectionUrlOrLookupString != null) {
      if (connectionUrlOrLookupString.startsWith("jdbc:")) {
        this.setProperty(CONNECTION_URL, connectionUrlOrLookupString);
      } else {
        final Context context = new InitialContext();
        try {
          final Object o = context.lookup(connectionUrlOrLookupString);
          if (o instanceof DataSource) {
            this.put(DATA_SOURCE, o);
          }
        } finally {
          context.close();
        }
      }
    }
  }


  public ConnectionDescriptor(final String connectionURL, final String username, final String password) {
    this(connectionURL, null, null, username, password);
  }

  public ConnectionDescriptor(Context context, final String lookupString) throws NamingException {
    this();
    if (lookupString != null) {
      if (context == null) {
        context = new InitialContext();
      }
      try {
        final Object o = context.lookup(lookupString);
        if (o instanceof DataSource) {
          this.put(DATA_SOURCE, o);
        }
      } finally {
        context.close();
      }
    }
  }

  public ConnectionDescriptor(final String connectionURL, final String catalog, final String schema, final String username, final String password) {
    this();
    if (connectionURL != null) {
      this.setProperty(CONNECTION_URL, connectionURL);
    }
    if (catalog != null) {
      this.setProperty(CATALOG, catalog);
    }
    if (schema != null) {
      this.setProperty(SCHEMA, schema);
    }
    if (username != null) {
      this.setProperty(USERNAME, username);
    }
    if (password != null) {
      this.setProperty(PASSWORD, password);
    }
  }

  public ConnectionDescriptor(final DataSource dataSource, final String catalog, final String schema, final String username, final String password) {
    this(dataSource);
    if (catalog != null) {
      this.setProperty(CATALOG, catalog);
    }
    if (schema != null) {
      this.setProperty(SCHEMA, schema);
    }
    if (username != null) {
      this.setProperty(USERNAME, username);
    }
    if (password != null) {
      this.setProperty(PASSWORD, password);
    }    
  }


  /*
   * Instance methods.
   */


  public String getCatalog() {
    return this.getProperty(CATALOG);
  }

  public String getSchema() {
    return this.getProperty(SCHEMA);
  }

  public String getConnectionURL() {
    return this.getProperty(CONNECTION_URL);
  }

  public Description getDescription() {
    return (Description)this.get(DESCRIPTION);
  }

  public String getDriverClassName() {
    return this.getProperty(DRIVER_CLASS_NAME);
  }

  public String getUsername() {
    return this.getProperty(USERNAME);
  }

  public String getPassword() {
    return this.getProperty(PASSWORD);
  }

  public final DataSource getDataSource() {
    DataSource dataSource = (DataSource)this.get(DATA_SOURCE);
    if (dataSource == null) {
      final String connectionURL = this.getConnectionURL();
      if (connectionURL != null) {
        dataSource = new DriverManagerDataSource(this);
        this.put(DATA_SOURCE, dataSource);
      }
    }
    return dataSource;
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    int timeout = 0;
    final DataSource dataSource = this.getDataSource();
    if (dataSource != null) {
      timeout = dataSource.getLoginTimeout();
    }
    return timeout;
  }

  @Override
  public void setLoginTimeout(final int timeout) throws SQLException {
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      ds.setLoginTimeout(timeout);
    }
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    PrintWriter writer = null;
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      writer = ds.getLogWriter();
    } else {
      writer = new PrintWriter(System.out);
    }
    return writer;
  }

  @Override
  public void setLogWriter(final PrintWriter writer) throws SQLException {
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      ds.setLogWriter(writer);
    }
  }

  @Override
  public boolean isWrapperFor(final Class<?> cls) {
    return cls != null && cls.isInstance(this);
  }

  @Override
  public <T> T unwrap(final Class<T> cls) {
    return cls.cast(this);
  }

  @Override
  public final Connection getConnection() throws SQLException {
    return this.getConnection(this.getDataSource(), this.getUsername(), this.getPassword());
  }

  @Override
  public final Connection getConnection(final String username, final String password) throws SQLException {
    return this.getConnection(this.getDataSource(), username, password);
  }

  public final Connection getConnection(final DataSource dataSource) throws SQLException {
    return this.getConnection(dataSource, this.getUsername(), this.getPassword());
  }

  public Connection getConnection(final DataSource dataSource, String username, String password) throws SQLException {
    Connection connection = null;
    if (dataSource != null) {
      if (username == null) {
        connection = dataSource.getConnection();
      } else {
        connection = dataSource.getConnection(username, password);
      }
    }
    return connection;
  }

}
