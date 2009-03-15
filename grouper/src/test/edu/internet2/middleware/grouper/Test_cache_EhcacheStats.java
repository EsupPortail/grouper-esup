/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import edu.internet2.middleware.grouper.cache.EhcacheStats;


/**
 * Test {@link EhcacheStats}.
 * @author  blair christensen.
 * @version $Id: Test_cache_EhcacheStats.java,v 1.3 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_cache_EhcacheStats extends GrouperTest {


  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_constructor_nullStatistics() {
    try {
      new EhcacheStats(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

