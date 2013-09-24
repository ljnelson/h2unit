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

import javax.naming.NamingException;

import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;

import org.junit.runner.Description;

import static com.edugility.h2unit.ConnectionDescriptor.CATALOG;
import static com.edugility.h2unit.ConnectionDescriptor.CONNECTION_URL;
import static com.edugility.h2unit.ConnectionDescriptor.DESCRIPTION;

import static org.junit.Assert.*;

public class TestCaseH2Rule {

  @H2Connection(url = "jdbc:h2:mem:test1", user = "SA", password = "", threadSafe = true)
  private Connection connection1;

  @H2Connection(url = "jdbc:h2:mem:test2", user = "SA", password = "", threadSafe = false)
  private Connection connection2;

  @javax.annotation.Generated(comments = "Test", value = "Test")
  private Connection unannotatedConnection;

  @Rule
  public final H2Rule h2Rule = new H2Rule(this);

  public TestCaseH2Rule() throws Exception {
    super();
    // this.h2Rule = new H2Rule(new H2ConnectionDescriptor("jdbc:h2:mem:test", "sa", ""));
  }

  @Test
  public void testBasics() throws SQLException {
    assertNotNull(this.connection1);
    assertTrue(this.connection1.getMetaData().getURL().contains("testBasics"));
    assertNotNull(this.connection2);
    assertEquals("jdbc:h2:mem:test2", this.connection2.getMetaData().getURL());
    assertNotSame(this.connection1, this.connection2);
  }

}
