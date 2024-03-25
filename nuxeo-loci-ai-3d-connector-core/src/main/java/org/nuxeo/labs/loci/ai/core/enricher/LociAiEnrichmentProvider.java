package org.nuxeo.labs.loci.ai.core.enricher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.nuxeo.ai.enrichment.EnrichmentDescriptor;
import org.nuxeo.ai.enrichment.EnrichmentMetadata;
import org.nuxeo.ai.metadata.AIMetadata;
import org.nuxeo.ai.pipes.types.BlobTextFromDocument;
import org.nuxeo.ai.rest.RestEnrichmentProvider;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.labs.loci.ai.core.model.LociResponse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.nuxeo.ai.enrichment.EnrichmentUtils.getBlobFromProvider;

public class LociAiEnrichmentProvider extends RestEnrichmentProvider {

    public static final String PARAM_API_SECRET = "apiKey";

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public String apiKey;


    @Override
    public void init(EnrichmentDescriptor descriptor) {
        super.init(descriptor);
        this.apiKey = descriptor.options.get(PARAM_API_SECRET);
    }

    @Override
    public HttpUriRequest prepareRequest(RequestBuilder requestBuilder, BlobTextFromDocument blobTextFromDocument) {
        if (blobTextFromDocument.getBlobs().size() != 1) {
            throw new NuxeoException("Loci.ai only supports one blob asset at a time.");
        }

        Blob blob = blobTextFromDocument.getBlobs().values().stream().findFirst().get();

        CloseableFile closeableFile;
        try {
            closeableFile = blob.getCloseableFile();
        } catch (IOException e) {
            throw new NuxeoException(e);
        }

        // Add request header
        requestBuilder.addHeader("accept", "application/json");
        requestBuilder.addHeader("x-api-key",apiKey);

        // Use the multipart builder
        MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
        multipartBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        multipartBuilder.addBinaryBody("asset_file", closeableFile.getFile(), ContentType.DEFAULT_BINARY, blob.getFilename());
        requestBuilder.setEntity(multipartBuilder.build());

        // Build the request
        return requestBuilder.build();
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

        List<EnrichmentMetadata.Label> labels = tags.stream().map(tag -> new AIMetadata.Label(tag,1.0f)).toList();

        // Return the result
        return Collections.singletonList(
                new EnrichmentMetadata.Builder(kind, name, blobTextFromDoc).withLabels(asLabels(labels))
                        .withRawKey(rawKey)
                        .build());
    }

}
