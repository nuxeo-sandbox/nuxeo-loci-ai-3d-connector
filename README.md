# Description
A plugin that provides a nuxeo-ai extension for the 3D asset tagging service [loci.ai](https://www.loci.ai/)

# How to build
```bash
git clone https://github.com/nuxeo-sandbox/nuxeo-loci-ai-3d-connector
cd nuxeo-loci-ai-3d-connector
mvn clean install
```

# Features
## Enricher
This plugin implements the nuxeo-ai enrichment extension point with a [custom class](https://github.com/nuxeo-sandbox/nuxeo-loci-ai-3d-connector/blob/master/nuxeo-loci-ai-3d-connector-core/src/main/resources/OSGI-INF/enrichment-provider-contrib.xml) that can be used in enrichment pipelines.

## Sample enrichment pipeline
The marketplace package contains a [sample enrichment pipeline](https://github.com/nuxeo-sandbox/nuxeo-loci-ai-3d-connector/blob/master/nuxeo-loci-ai-3d-connector-package/src/main/resources/install/templates/nuxeo-loci-ai-3d-connector/config/loci-pipeline-config.xml) which is deployed with the `nuxeo-loci-ai-3d-connector` configuration template.
On top of saving enrichment data as tags and as a document facet, the `3dEnrichementMetadataAvailable` event is also raised.

## Default configuration limitation
**The default configuration only supports GLB files.**

# How to run
## Configuration
The following nuxeo.conf properties are available to configure the plugin

| Property name                 | description                                                   |
|-------------------------------|---------------------------------------------------------------|
| nuxeo.ai.threed.enabled       | Enable or disable the default 3D asset enrichment pipeline    |
| nuxeo.ai.loci.apiKey          | The API key to use when calling the loci.ai REST API          |
| nuxeo.enrichment.save.tags    | Set to true to automatically save tags                        |
| nuxeo.enrichment.save.facets  | Set to true to automatically save enrichment data on document |

# Support
**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning
resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be
useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# Nuxeo Marketplace
This plugin is published on
the [marketplace](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-loci-ai-3d-connector)

# License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo
Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL
databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions
for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/),
and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses
schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).