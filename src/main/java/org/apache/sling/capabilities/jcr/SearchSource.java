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

import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.sling.capabilities.CapabilitiesSource;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CapabilitiesSource that provides information on the JCR Repository's search features */
@Component(service = CapabilitiesSource.class)
public class SearchSource implements CapabilitiesSource {

    public static final String NAMESPACE = "org.apache.sling.jcr.search";

    public static final String SSA_PROP_NAME = "similarity.search.active";

    public static final int SIMILARITY_SEARCH_CACHE_LIFETIME_SECONDS = 60;

    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private boolean similaritySearchActive;
    private long similaritySearchCacheExpires;

    @Reference
    private SlingRepository repository;
    
    @Override
    public Map<String, Object> getCapabilities() throws Exception {
        refreshCachedValues();
        final Map<String, Object> result = new HashMap<>();
        result.put("similarity.search.active", String.valueOf(similaritySearchActive));
        return result;
    }

    /** Find out whether the Oak similarity search is active, by
     *  searching for any index definition that has useInSimilarity=true.
     *  Cache the result to avoid making too many searches.
     */
    private void refreshCachedValues() throws RepositoryException {
        if(System.currentTimeMillis() < similaritySearchCacheExpires) {
            log.debug("Using cached similaritySearchActive value");
            return;
        }

        similaritySearchCacheExpires = System.currentTimeMillis() + (SIMILARITY_SEARCH_CACHE_LIFETIME_SECONDS * 1000L);

        synchronized(this) {
            // TODO use a service user
            final Session session = repository.loginAdministrative(null);
            try {
                    String query = "/jcr:root/oak:index//properties//* [@useInSimilarity = \"true\"]";
                    final QueryManager qm = session.getWorkspace().getQueryManager();
                    final QueryResult qr = qm.createQuery(query, Query.XPATH).execute();
                    similaritySearchActive = qr.getNodes().hasNext();
                    log.debug("Recomputed {}={} using query {}", SSA_PROP_NAME, similaritySearchActive, query);
            } finally {
                session.logout();
            }
        }
    }
    
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
    
}