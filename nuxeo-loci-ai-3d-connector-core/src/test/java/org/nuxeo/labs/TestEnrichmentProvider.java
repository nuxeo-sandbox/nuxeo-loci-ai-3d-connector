package org.nuxeo.labs;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ai.services.AIComponent;
import org.nuxeo.labs.loci.ai.core.enricher.LociAiEnrichmentProvider;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class })
public class TestEnrichmentProvider {

    @Inject
    public AIComponent aiComponent;

    @Test
    public void testEnrichmentProviderIsRegistered() {
        Assert.assertNotNull(aiComponent.getEnrichmentProvider("ai.loci"));
    }

    @Test
    public void testEnrichmentProviderSupportsMimeType() {
        LociAiEnrichmentProvider ep = (LociAiEnrichmentProvider) aiComponent.getEnrichmentProvider("ai.loci");
        Assert.assertTrue(ep.supportsMimeType("model/gltf-binary"));
    }

    @Test
    public void testEnrichmentProviderSupportsSize() {
        LociAiEnrichmentProvider ep = (LociAiEnrichmentProvider) aiComponent.getEnrichmentProvider("ai.loci");
        Assert.assertTrue(ep.supportsSize(100*1000*1000));
    }

}
