package local.pharm.storage;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StorageStreamListener implements ContentStreamListener {
    private static final Log logger = LogFactory.getLog(StorageStreamListener.class);

    @Override
    public void contentStreamClosed() throws ContentIOException {

    }
}
