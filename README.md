<!-- -*- markdown -*- -->
# `h2unit`

## H2 Unit Testing Utilities

### September 24, 2013

### [Laird Nelson][1]

`h2unit` is a small project that provides JUnit utilities that make it
easy to use H2 in your unit tests.

The `H2Rule` class and the `H2Connection` annotation can combine to
produce in-memory H2 databases that are usable even when you are
running unit tests in parallel.

### Sample Code

    public class MyTest {
    
      @Rule
      public final H2Rule h2Rule = new H2Rule();
      
      @H2Connection(url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", threadSafe = true)
      private Connection connection;
      
      @Test
      public void testAll() throws Exception {

        // The connection is injected and torn down/closed
        // automatically at the conclusion of the test run.
        assertNotNull(this.connection);

        // The URL is munged to include the test method name, the test
        // class, the thread ID and the process ID so that different test
        // runs use different catalogs.
        assertTrue(this.connection.getMetaData().getURL().contains("testAll"));
        
      }
    
    }

[1]: http://about.me/lairdnelson
