/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Licensed to the Apache Software Foundation (ASF) under one
 ~ or more contributor license agreements.  See the NOTICE file
 ~ distributed with this work for additional information
 ~ regarding copyright ownership.  The ASF licenses this file
 ~ to you under the Apache License, Version 2.0 (the
 ~ "License"); you may not use this file except in compliance
 ~ with the License.  You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package org.apache.sling.capabilities.jcr;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Session;
import org.apache.sling.capabilities.CapabilitiesSource;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.serviceusermapping.ServiceUserMapped;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class SearchSourceTest {

    @Rule
    public final SlingContext context = new SlingContext(ResourceResolverType.JCR_OAK);

    private static final String SIMILARITY_ACTIVE_CAP = "similarity.search.active";

    private CapabilitiesSource searchSource;

    private Dictionary<String, Object> props(Object ... nameValuePairs) {
        final Dictionary<String, Object> props = new Hashtable<>();
        for(int i=0 ; i < nameValuePairs.length; i+=2) {
            props.put(nameValuePairs[i].toString(), nameValuePairs[i+1]);
        }
        return props;
    }

    private void registerSearchSource(Object ... configNameValuePairs) throws IOException {
        final ConfigurationAdmin ca = context.getService(ConfigurationAdmin.class);
        assertNotNull("Expecting a ConfigurationAdmin service", ca);
        final Configuration cfg = ca.getConfiguration(SearchSource.class.getName());
        cfg.update(props(configNameValuePairs));

        final SearchSource ss = new SearchSource();
        context.registerInjectActivateService(ss);

        searchSource = context.getService(CapabilitiesSource.class);
        assertNotNull("Expecting our SearchSource to be registered", searchSource);
        assertEquals("Expecting the SearchSource namespace", SearchSource.NAMESPACE, searchSource.getNamespace());
    }

    private void createMockIndexNode(String parentPath, String path, String propertyName, boolean value) throws Exception {
        final SlingRepository repository = context.getService(SlingRepository.class);
        assertNotNull("Expecting a SlingRepository", repository);
        final Session s = repository.loginAdministrative(null);
        try {
            final Node n = s.getNode(parentPath).addNode(path);
            n.setProperty(propertyName, value);
            s.save();
        } finally {
            s.logout();
        }
    }

    @Before
    public void setup() throws IOException {

        final ServiceUserMapped sum = new ServiceUserMapped() {};
        context.registerService(ServiceUserMapped.class, sum, 
                props(ServiceUserMapped.SUBSERVICENAME, SearchSource.SUBSERVICE_NAME));
    }

    @Test
    public void testNoSimilarity() throws Exception {
        registerSearchSource();
        assertNotNull(searchSource.getCapabilities());
        assertEquals("false", searchSource.getCapabilities().get(SIMILARITY_ACTIVE_CAP));
    }

    @Test
    public void testHasSimilarity() throws Exception {
        registerSearchSource();
        createMockIndexNode("/oak:index", "foo", "useInSimilarity", true);

        assertNotNull(searchSource.getCapabilities());
        assertEquals("true", searchSource.getCapabilities().get(SIMILARITY_ACTIVE_CAP));
    }

    @Test
    public void testCustomQueryAndCacheLifetime() throws Exception {
        final int lifetimeSeconds = 2;
        final String uniquePath = "testCustomQuery_" + UUID.randomUUID();
        registerSearchSource(
                "similarityIndexQuery", "/jcr:root/" + uniquePath,
                "cacheLifetimeSeconds", lifetimeSeconds);

        // With our custom query we get false first
        assertNotNull(searchSource.getCapabilities());
        assertEquals("false", searchSource.getCapabilities().get(SIMILARITY_ACTIVE_CAP));

        // Create a node that causes the query to return something
        // The capability value will change after some time, once its cache expires
        createMockIndexNode("/", uniquePath, "someProperty", true);

        // Must get a few false results and then true, once the cache expires
        // (might fail if the box running this test is very very slow)
        int nFalse = 0;
        int nTrue = 0;
        final long testEnd = System.currentTimeMillis() + lifetimeSeconds * 1000L + 1500L;
        while(System.currentTimeMillis() < testEnd) {
            final Object value = searchSource.getCapabilities().get(SIMILARITY_ACTIVE_CAP);
            if("false".equals(value)) {
                assertEquals("Expecting true value to come after all false values", 0, nTrue);
                nFalse++;
            } else {
                nTrue++;
            }
            Thread.sleep(100L);
        }

        assertTrue("Expecting a few true values", nTrue > 0);
        assertTrue("Expecting a few false values", nTrue > 0);
    }
}