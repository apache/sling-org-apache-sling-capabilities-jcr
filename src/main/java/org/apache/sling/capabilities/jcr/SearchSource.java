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
import org.apache.sling.serviceusermapping.ServiceUserMapped;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CapabilitiesSource that provides information on the JCR Repository's search features */
@Component(service = CapabilitiesSource.class)
@Designate(ocd = SearchSource.Config.class)
public class SearchSource implements CapabilitiesSource {

    public static final String NAMESPACE = "org.apache.sling.jcr.search";

    public static final String SSA_PROP_NAME = "similarity.search.active";

    public static final String SUBSERVICE_NAME = "search";

    public static final String DEFAULT_QUERY = "/jcr:root/oak:index//* [@useInSimilarity = true]";

    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private String similaritySearchActiveResult;
    private long similaritySearchCacheExpires;
    private int cacheLifetimeSeconds;
    private String similarityIndexQuery;

    @Reference
    private SlingRepository repository;
    
    @Reference(target="("+ServiceUserMapped.SUBSERVICENAME+"=" + SUBSERVICE_NAME + ")")
    private ServiceUserMapped scriptServiceUserMapped;

    @ObjectClassDefinition(
        name = "Apache Sling JCR Capabilities - Search Source",
        description = "Provides information JCR search features"
    )
    public static @interface Config {
        @AttributeDefinition(
            name = "Similarity Index Query",
            description = "A JCR XPAth query that returns at least 1 Node if similarity search is available."
                + " The service user that this component uses must have sufficient rights to read the corresponding nodes."
        )
        String similarityIndexQuery() default DEFAULT_QUERY;

        @AttributeDefinition(
            name = "Cache time-to-live in seconds",
            description = "The results of expensive operations like queries are cached for this amount of time"
        )
        int cacheLifetimeSeconds() default 60;
    }

    @Activate
    protected void activate(Config cfg, ComponentContext ctx) {
        similarityIndexQuery = cfg.similarityIndexQuery();
        cacheLifetimeSeconds = cfg.cacheLifetimeSeconds();
    }

    @Override
    public Map<String, Object> getCapabilities() throws Exception {
        refreshCachedValues();
        final Map<String, Object> result = new HashMap<>();
        result.put("similarity.search.active", similaritySearchActiveResult);
        return result;
    }

    /** Find out whether the Oak similarity search is active, by
     *  searching for any index definition that has useInSimilarity=true.
     *  Cache the result to avoid making too many searches.
     */
    private void refreshCachedValues() {
        if(System.currentTimeMillis() < similaritySearchCacheExpires) {
            log.debug("Using cached similaritySearchActive value");
            return;
        }

        similaritySearchCacheExpires = System.currentTimeMillis() + (cacheLifetimeSeconds * 1000L);

        synchronized(this) {
            Session session = null;
            try {
                session = repository.loginService(SUBSERVICE_NAME, null);
                final QueryManager qm = session.getWorkspace().getQueryManager();
                final QueryResult qr = qm.createQuery(similarityIndexQuery, Query.XPATH).execute();
                similaritySearchActiveResult = String.valueOf(qr.getNodes().hasNext());
            } catch(RepositoryException rex) {
                similaritySearchActiveResult = rex.toString();
            } finally {
                if(session != null) {
                    session.logout();
                }
            }
        }

        log.debug("Recomputed {}={} using query {}", SSA_PROP_NAME, similaritySearchActiveResult, similarityIndexQuery);
    }
    
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
    
}