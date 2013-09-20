/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2013 Edugility LLC.
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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import java.util.Properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;

import javax.sql.DataSource;

import org.junit.runner.Description;

public class H2ConnectionDescriptor extends ConnectionDescriptor {

  private static final long serialVersionUID = 1L;

  private static final Pattern catalogPattern = Pattern.compile("^(jdbc:h2:(?:(?:mem|file|tcp|ssl|zip):)?)([^;]*)");

  public H2ConnectionDescriptor() {
    super();
  }

  public H2ConnectionDescriptor(final H2Connection h2ConnectionAnnotation, final Description description) {
    super();
    final boolean threadSafe;
    if (h2ConnectionAnnotation != null) {
      final String catalog = h2ConnectionAnnotation.catalog();
      if (catalog != null && !catalog.equalsIgnoreCase("null")) {
        this.setProperty(CATALOG, catalog);
      }
      final String url = h2ConnectionAnnotation.url();
      if (url != null && !url.equalsIgnoreCase("null")) {
        this.setProperty(CONNECTION_URL, url);
      }
      final String password = h2ConnectionAnnotation.password();
      if (password != null && !password.equalsIgnoreCase("null")) {
        this.setProperty(PASSWORD, password);
      }
      final String schema = h2ConnectionAnnotation.schema();
      if (schema != null && !schema.equalsIgnoreCase("null")) {
        this.setProperty(SCHEMA, schema);
      }
      final String user = h2ConnectionAnnotation.user();
      if (user != null && !user.equalsIgnoreCase("null")) {
        this.setProperty(USERNAME, user);
      }
      threadSafe = h2ConnectionAnnotation.threadSafe();
    } else {
      threadSafe = false;
    }
    if (description != null) {
      this.put(DESCRIPTION, description);
    }
    if (threadSafe) {
      this.replaceCatalog();
    }
  }

  public H2ConnectionDescriptor(final Properties properties) {
    super(properties);
  }

  public H2ConnectionDescriptor(final String connectionUrlOrLookupString) throws NamingException {
    super(connectionUrlOrLookupString);
  }

  public H2ConnectionDescriptor(final String connectionURL, final String username, final String password) {
    super(connectionURL, username, password);
  }

  public H2ConnectionDescriptor(final Context context, final String lookupString) throws NamingException {
    super(context, lookupString);
  }

  public H2ConnectionDescriptor(final String connectionURL, final String catalog, final String schema, final String username, final String password) {
    super(connectionURL, catalog, schema, username, password);
  }

  public H2ConnectionDescriptor(final DataSource dataSource, final String catalog, final String schema, final String username, final String password) {
    super(dataSource, catalog, schema, username, password);
  }

  @Override
  public String getCatalog() {
    String catalog = super.getCatalog();
    if (catalog == null) {
      final String connectionUrl = this.getConnectionURL();
      if (connectionUrl != null) {
        catalog = extractCatalogFromConnectionUrl(connectionUrl);
      }
    }
    return catalog;
  }

  @Override
  public final String getDriverClassName() {
    return org.h2.Driver.class.getName();
  }

  public final String alterCatalog() {
    String returnValue = null;
    final Description description = this.getDescription();
    if (description != null) {
      final String catalog = this.getCatalog();
      final String testClassName = description.getClassName();
      final String testMethodName = description.getMethodName();
      returnValue = this.alterCatalog(String.format("%s-%s", testClassName, testMethodName), catalog);
    }
    return returnValue;
  }

  public void replaceCatalog() {
    final String catalogReplacement = this.alterCatalog();
    if (catalogReplacement != null) {
      this.setProperty("_originalCatalog", this.getCatalog());
      this.setProperty("_originalURL", this.getConnectionURL());
      this.setProperty(CATALOG, catalogReplacement);
      this.setProperty(CONNECTION_URL, replaceCatalog(this.getConnectionURL(), catalogReplacement));
    }
  }

  public void restoreCatalog() {
    final String originalCatalog = this.getProperty("_originalCatalog");
    if (originalCatalog != null) {
      this.setProperty(CATALOG, originalCatalog);
      this.remove("_originalCatalog");
    }
    final String originalURL = this.getProperty("_originalURL");
    if (originalURL != null) {
      this.setProperty(CONNECTION_URL, originalURL);
      this.remove("_originalURL");
    }
  }


  /*
   * Static methods.
   */


  public static final String replaceCatalog(final String url, final String catalogReplacement) {
    String returnValue = url;
    if (url != null) {
      final Matcher m = catalogPattern.matcher(url);
      assert m != null;
      m.reset();
      final StringBuffer sb = new StringBuffer();
      if (m.lookingAt()) {
        m.appendReplacement(sb, String.format("$1%s", catalogReplacement == null ? "" : catalogReplacement));
      }
      m.appendTail(sb);
      returnValue = sb.toString();
    }
    return returnValue;
  }

  public static final Object getProcessId() {
    Object returnValue = null;
    final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    if (runtimeMXBean != null) {
      final String name = runtimeMXBean.getName();
      if (name != null && !name.isEmpty()) {
        final int atIndex = name.indexOf('@');
        if (atIndex >= 0) {
          returnValue = name.substring(0, atIndex);
        }
      }
    }
    return returnValue;
  }

  public static final String alterCatalog(final String testName, final String originalCatalog) {
    String returnValue = originalCatalog;
    if (originalCatalog != null && testName != null) {
      
      final Thread currentThread = Thread.currentThread();
      assert currentThread != null;
      final long id = currentThread.getId();
      
      final StringBuilder sb = new StringBuilder();      
      if (!originalCatalog.isEmpty()) {
        sb.append(originalCatalog).append("-");
      }
      sb.append(testName).append("-");
      final Object pid = getProcessId();
      if (pid != null) {
        sb.append(pid).append("-");
      }
      sb.append(id);
      returnValue = sb.toString();
    }
    return returnValue;
  }
  
  private static final String extractCatalogFromConnectionUrl(final String url) {
    String catalog = null;
    if (url != null && url.startsWith("jdbc:h2:")) {
      final Matcher m = catalogPattern.matcher(url);
      assert m != null;
      if (m.lookingAt()) {
        catalog = m.group(2);
      } else {
        catalog = null;
      }
    }
    return catalog;
  }


}
