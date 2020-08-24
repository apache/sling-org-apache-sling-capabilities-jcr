[<img src="https://sling.apache.org/res/logos/sling.png"/>](https://sling.apache.org)

 [![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-capabilities-jcr/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-capabilities-jcr/job/master/) [![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-capabilities-jcr/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-capabilities-jcr/job/master/test/?width=800&height=600) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-capabilities-jcr&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-capabilities-jcr) [![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-capabilities-jcr&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-capabilities-jcr) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.capabilities.jcr/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.capabilities.jcr%22) [![JavaDocs](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.capabilities.jcr.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.capabilities.jcr) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Sling Capabilities - JCR sources
=======================================

This module is part of the [Apache Sling](https://sling.apache.org) project.

It provides information about the JCR repository to the [Sling Capabilities](https://github.com/apache/sling-org-apache-sling-capabilities) module.

It is implemented separately to avoid making the core module dependent on JCR APIs.

Usage
-----
For now, this module's `SearchSource` provides just one capability that indicates whether the Oak similarity search feature is available. Here's typical JSON output of the `CapabilitiesServlet` when this source is active:

    {
      "org.apache.sling.capabilities": {
        "data": {
          "org.apache.sling.jcr.search": {
            "similarity.search.active": "false"
          }
        }
      }
    }

To compute this value, the `SearchSource` needs to make a JCR query to find out whether similarity search is available - that is the case if there is at least one Oak index configuration which has `@useInSimilarity = true`.

That query is configurable in the `SearchSource` component, with a default value that should work for common cases.

The cache lifetime of that value in the `SearchSource` component is also configurable, with a default of 60 seconds. The component caches the query result for that amount of time to avoid making too many queries.

To execute this query, the `SearchSource` uses a Service User that needs read access under `/oak:index`. 

The following feature model excerpt can be used to set that up. It also creates a `/var/capabilities/public` path that every user can read. Resources with the `sling/capabilities` resource type can be created under that path to provide access to the capabilities. See the Capabilities module documentation for more information.

    "configurations": {
        "org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~cq-capabilities-jcr": {
          "user.mapping": [
            "org.apache.sling.capabilities.jcr:search=[capabilities-search]"
          ]
        },
        "org.apache.sling.capabilities.internal.CapabilitiesServlet": {
          "resourcePathPatterns" : "/var/capabilities/.*"
        }
      },
      "repoinit:TEXT|true": [
        "create service user capabilities-search",
        "",
        "set ACL for capabilities-search",
        "allow jcr:read on /oak:index",
        "end",
        "",
        "create path /var/capabilities/public(nt:unstructured)",
        "",
        "set ACL on /var/capabilities/public",
        "allow jcr:read for everyone",
        "end"
    ]
