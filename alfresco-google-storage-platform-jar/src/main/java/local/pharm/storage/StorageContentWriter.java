package local.pharm.storage;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.channels.WritableByteChannel;

public class StorageContentWriter extends AbstractContentWriter {
    private static final Log logger = LogFactory.getLog(StorageContentWriter.class);

    /**
     * @param contentUrl            the content URL
     * @param existingContentReader a reader of a previous version of this content
     */
    protected StorageContentWriter(String contentUrl, ContentReader existingContentReader) {
        super(contentUrl, existingContentReader);
    }


    @Override
    protected ContentReader createReader() throws ContentIOException {
        return null;
    }

    @Override
    protected WritableByteChannel getDirectWritableChannel() throws ContentIOException {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }
}
