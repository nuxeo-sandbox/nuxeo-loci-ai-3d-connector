package org.nuxeo.labs.loci.ai.core.enricher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ai.enrichment.EnrichmentDescriptor;
import org.nuxeo.ai.enrichment.EnrichmentMetadata;
import org.nuxeo.ai.metadata.AIMetadata;
import org.nuxeo.ai.pipes.types.BlobTextFromDocument;
import org.nuxeo.ai.rest.RestEnrichmentProvider;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.blob.ManagedBlob;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.labs.loci.ai.core.model.LociResponse;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.nuxeo.ai.rest.RestClient.OPTION_URI;

public class LociAiEnrichmentProvider extends RestEnrichmentProvider {

    private static final Logger log = LogManager.getLogger(LociAiEnrichmentProvider.class);

    public static final String PARAM_API_SECRET = "apiKey";

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public String uri;

    public String method;

    public String apiKey;

    @Override
    public void init(EnrichmentDescriptor descriptor) {
        super.init(descriptor);
        this.apiKey = descriptor.options.get(PARAM_API_SECRET);
        this.uri = descriptor.options.get(OPTION_URI);
        this.method = descriptor.options.getOrDefault("methodName", "POST");
    }

    @Override
    public Collection<EnrichmentMetadata> enrich(BlobTextFromDocument blobTextFromDoc) {
        RequestBuilder requestBuilder = RequestBuilder.create(this.method);
        requestBuilder.setUri(this.uri);
        // Add request header
        requestBuilder.addHeader("accept", "application/json");
        requestBuilder.addHeader("x-api-key",apiKey);

        HttpUriRequest request = prepareRequest(requestBuilder,blobTextFromDoc);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return this.handleResponse(response, blobTextFromDoc);
                } else {
                    String error = getContent(response);
                    log.warn(String.format("Unsuccessful call to rest api %s, status is %d, error is %s", this.uri, statusCode, error));
                    return Collections.emptyList();
                }
            }
        } catch (IOException e) {
            throw new NuxeoException(e);
        }
    }

    @Override
    public HttpUriRequest prepareRequest(RequestBuilder requestBuilder, BlobTextFromDocument blobTextFromDocument) {
        if (blobTextFromDocument.getBlobs().size() != 1) {
            throw new NuxeoException("Loci.ai only supports one blob asset at a time.");
        }

        ManagedBlob blob = blobTextFromDocument.getBlobs().values().stream().findFirst().get();

        //get file extension from blob mime-type
        String mimetype = blob.getMimeType();
        MimetypeRegistry mimetypeRegistry = Framework.getService(MimetypeRegistry.class);
        String extension = mimetypeRegistry.getMimetypeEntryByMimeType(mimetype).getExtensions().get(0);

        try{
            CloseableFile closeableFile = blob.getCloseableFile();
            // Use the multipart builder
            MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
            multipartBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
            multipartBuilder.addBinaryBody("asset_file", closeableFile.getFile(),
                    ContentType.DEFAULT_BINARY, String.format("%s.%s",closeableFile.getFile().getName(),extension));
            requestBuilder.setEntity(multipartBuilder.build());
            // Build the request
            return requestBuilder.build();
        } catch (IOException e) {
            throw new NuxeoException(e);
        }
    }

    @Override
    public Collection<EnrichmentMetadata> handleResponse(HttpResponse httpResponse, BlobTextFromDocument blobTextFromDocument) {
        try {
            String json = getContent(httpResponse);
            String rawKey = saveJsonAsRawBlob(json);
            LociResponse response = objectMapper.readValue(json, LociResponse.class);
            return processResponseProperties(response.tags, rawKey, blobTextFromDocument);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the returned json tags into enrichment metadata.
     */
    protected Collection<EnrichmentMetadata> processResponseProperties(List<String> tags, String rawKey,
                                                                       BlobTextFromDocument blobTextFromDoc) {
        List<EnrichmentMetadata.Label> labels =
                tags.stream().map(tag -> new AIMetadata.Label(tag,1.0f)).toList();
        // Return the result
        return Collections.singletonList(
                new EnrichmentMetadata.Builder(kind, name, blobTextFromDoc).withLabels(asLabels(labels))
                        .withRawKey(rawKey)
                        .build());
    }

}
