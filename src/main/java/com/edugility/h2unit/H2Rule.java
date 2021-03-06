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

import java.lang.reflect.Field;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;

import org.junit.internal.runners.statements.ExpectException;

import org.junit.rules.ExternalResource;

import org.junit.runner.Description;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class H2Rule extends ExternalResource {

  private static final Map<Class<?>, TestClass> testClasses = new HashMap<Class<?>, TestClass>();

  private final Object testInstance;

  private Description description;

  private Collection<Connection> connections;

  public H2Rule(final Object testInstance) {
    super();
    this.testInstance = testInstance;
    Assert.assertNotNull(testInstance);
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    this.description = description;
    final Statement s = super.apply(base, description);
    return s;
  }

  @Override
  protected void before() throws Throwable {
    Assert.assertNotNull(this.testInstance);
    final TestClass testClass = this.getTestClass();
    Assert.assertNotNull(testClass);
    final Collection<FrameworkField> annotatedFields = testClass.getAnnotatedFields(H2Connection.class);
    if (annotatedFields != null && !annotatedFields.isEmpty()) {
      for (final FrameworkField ff : annotatedFields) {
        if (ff != null) {
          final Field f = ff.getField();
          if (f != null && Connection.class.isAssignableFrom(f.getType())) {
            final H2Connection h2Connection = f.getAnnotation(H2Connection.class);
            Assert.assertNotNull(h2Connection);
            final Connection connection = new H2ConnectionDescriptor(h2Connection, this.description).getConnection();
            if (this.connections == null) {
              this.connections = new ArrayList<Connection>(annotatedFields.size());
            }
            this.connections.add(connection);
            final boolean accessible = f.isAccessible();
            f.setAccessible(true);
            try {
              f.set(this.testInstance, connection);
            } finally {
              f.setAccessible(accessible);
            }
          }
        }
      }
    }
  }

  @Override
  protected void after() {
    this.description = null;
    if (this.connections != null && !this.connections.isEmpty()) {
      final Iterator<Connection> connectionIterator = this.connections.iterator();
      if (connectionIterator != null) {
        while (connectionIterator.hasNext()) {
          final Connection connection = connectionIterator.next();
          if (connection != null) {
            try {
              connection.close();
            } catch (final SQLException okToIgnore) {
              // ignore
            }
            connectionIterator.remove();
          }
        }
      }
    }
    this.connections = null;
  }

  private final TestClass getTestClass() {
    TestClass testClass = null;
    final Description description = this.description;
    if (description != null) {
      final Class<?> c = description.getTestClass();
      if (c != null) {
        synchronized (testClasses) {
          testClass = testClasses.get(c);
          if (testClass == null) {
            testClass = new TestClass(c);
            testClasses.put(c, testClass);
          }
        }
      }
    }
    return testClass;
  }

}
