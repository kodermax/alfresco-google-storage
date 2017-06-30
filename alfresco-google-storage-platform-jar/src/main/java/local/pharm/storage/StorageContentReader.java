package local.pharm.storage;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.ReadableByteChannel;

public class StorageContentReader extends AbstractContentReader {
    private static final Log logger = LogFactory.getLog(StorageContentReader.class);

    /**
     * @param contentUrl the content URL - this should be relative to the root of the store
     *                   and not absolute: to enable moving of the stores
     */
    protected StorageContentReader(String contentUrl) {
        super(contentUrl);
    }

    @Override
    protected ContentReader createReader() throws ContentIOException {
        return null;
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException {
        return null;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public long getSize() {
        return 0;
    }
}
