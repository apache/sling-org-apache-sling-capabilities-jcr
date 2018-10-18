/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.capabilities.it;

import javax.inject.Inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JcrCapabilitiesBundleIT extends JcrCapabilitiesTestSupport {
    
    @Inject
    private BundleContext bundleContext;
    
    private Bundle testBundle;

    @Before
    public void setup() {
        final String symbolicName = "org.apache.sling.capabilities.jcr";
        testBundle = null;
        for(Bundle b : bundleContext.getBundles()) {
            if(symbolicName.equals(b.getSymbolicName())) {
                testBundle = b;
                break;
            }
        }
        assertNotNull("Expecting test bundle to be found:" + symbolicName, testBundle);
    }
    
    @Test
    public void testBundleActive() {
        assertEquals("Expecting bundle to be active", Bundle.ACTIVE, testBundle.getState());
    }
}
