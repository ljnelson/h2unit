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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;

import org.junit.runner.Description;

import static com.edugility.h2unit.ConnectionDescriptor.CATALOG;
import static com.edugility.h2unit.ConnectionDescriptor.CONNECTION_URL;
import static com.edugility.h2unit.ConnectionDescriptor.DESCRIPTION;

import static org.junit.Assert.*;

public class TestCaseH2ConnectionDescriptor {

  private Description description;

  @Rule
  public final TestRule descriptionRule = new TestWatcher() {
      @Override
      protected final void starting(final Description description) {
        TestCaseH2ConnectionDescriptor.this.description = description;
      }
    };

  public TestCaseH2ConnectionDescriptor() {
    super();
  }

  @Test
  public void testBasics() throws SQLException {
    final H2ConnectionDescriptor cd = new H2ConnectionDescriptor();
    assertNull(cd.getConnectionURL());
    cd.setProperty(CONNECTION_URL, "jdbc:h2:mem:test");
    assertEquals("jdbc:h2:mem:test", cd.getConnectionURL());
    assertEquals("test", cd.getCatalog());
    final Connection c = cd.getConnection();
    assertNotNull(c);
    final DatabaseMetaData dmd = c.getMetaData();
    assertNotNull(dmd);
    assertEquals("jdbc:h2:mem:test", dmd.getURL());
    c.close();
    assertNotNull(this.description);
    cd.put(DESCRIPTION, this.description);
    cd.replaceCatalog();
    final String catalog = cd.getCatalog();
    assertNotNull(catalog);
    assertTrue(catalog.contains("testBasics"));
  }

}
