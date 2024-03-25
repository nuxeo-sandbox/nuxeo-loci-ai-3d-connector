/*
 * (C) Copyright 2022 Nuxeo (http://nuxeo.com/) and others.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.labs;

import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.RunnerFeature;

@Features({PlatformFeature.class })
@Deploy({
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.default.config",
        "org.nuxeo.ai.ai-core",
        "org.nuxeo.ai.nuxeo-ai-pipes",
        "nuxeo-loci-ai-3d-connector-core"
})
public class TestFeature implements RunnerFeature {}
