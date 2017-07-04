package local.pharm.storage;


import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

public class GSContentReader extends AbstractContentReader {

    private static final Log logger = LogFactory.getLog(GSContentReader.class);

    private String key;
    private Storage storage;
    private String bucketName;
    private Blob fileObject;
    private Map<String, String> fileObjectMetadata;

    /**
     * @param storage
     * @param contentUrl the content URL - this should be relative to the root of the store
     * @param bucketName
     */
    protected GSContentReader(String key, String contentUrl, Storage storage, String bucketName) {
        super(contentUrl);
        this.key = key;
        this.storage = storage;
        this.bucketName = bucketName;
        this.fileObject = getObject();
        this.fileObjectMetadata = getObjectMetadata(this.fileObject);
    }

    @Override
    protected ContentReader createReader() throws ContentIOException {

        logger.debug("Called createReader for contentUrl -> " + getContentUrl() + ", Key: " + key);
        return new GSContentReader(key, getContentUrl(), storage, bucketName);
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException {

        if(!exists()) {
            throw new ContentIOException("Content object does not exist on S3");
        }

        try {
            return Channels.newChannel(new ByteArrayInputStream(fileObject.getContent()));
        } catch ( Exception e ) {
            throw new ContentIOException("Unable to retrieve content object from S3", e);
        }

    }

    @Override
    public boolean exists() {
        return fileObjectMetadata != null;
    }

    @Override
    public long getLastModified() {

        if(!exists()) {
            return 0L;
        }

        return fileObject.getUpdateTime();

    }

    @Override
    public long getSize() {

        if(!exists()) {
            return 0L;
        }

        return fileObject.getContent().length;
    }

    private Blob getObject() {

        Blob object = null;

        try {
            logger.debug("GETTING OBJECT - BUCKET: " + bucketName + " KEY: " + key);
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, key).build();
            object = storage.get(blobInfo.getBlobId());
        } catch (Exception e) {
            logger.error("Unable to fetch S3 Object", e);
        }

        return object;
    }

    private Map<String, String> getObjectMetadata(Blob object) {

        Map<String, String> metadata = null;

        if(object != null) {
            metadata = object.getMetadata();
        }

        return metadata;

    }
}
