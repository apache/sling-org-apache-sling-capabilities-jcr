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
import org.apache.sling.capabilities.CapabilitiesSource;
import org.osgi.service.component.annotations.Component;

/** CapabilitiesSource that provides information on the JCR Repository's search features */
@Component(service = CapabilitiesSource.class)
public class SearchSource implements CapabilitiesSource {

    public static final String NAMESPACE = "org.apache.sling.jcr.search";
    
    @Override
    public Map<String, Object> getCapabilities() throws Exception {
        final Map<String, Object> result = new HashMap<>();
        return result;
    }
    
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }
    
}