package local.pharm.storage;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class GSStreamListener implements ContentStreamListener {

    private static final Log logger = LogFactory.getLog(GSStreamListener.class);

    private GSContentWriter writer;

    public GSStreamListener(GSContentWriter writer) {
        this.writer = writer;
    }

    @Override
    public void contentStreamClosed() throws ContentIOException {
        File file = writer.getTempFile();
        long size = file.length();
        writer.setSize(size);
        try {
            logger.debug("Writing to gs://" + writer.getBucketName() + "/" + writer.getKey());
            List<Acl> acls = new ArrayList<>();
            acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            BlobInfo blobInfo = BlobInfo.newBuilder(writer.getBucketName(), writer.getKey()).setAcl(acls).build();
            Storage storage = writer.getStorage();
            FileInputStream fis = new FileInputStream(file);
            Blob blob = storage.create(blobInfo, fis);
            fis.close();
            logger.debug("GSStreamListener uploaded file: " + blob.getMediaLink());

        } catch (Exception e) {
            logger.error("GSStreamListener Failed to Upload File", e);
        }

    }
}
