package local.pharm.storage;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStoreCreatedEvent;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.surf.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;


public class GSContentStore extends AbstractContentStore
        implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private static final Log logger = LogFactory.getLog(GSContentStore.class);
    private ApplicationContext applicationContext;

    private Storage storage;
    private String accessKey;
    private String projectId;
    private String bucketName;
    private String rootDirectory;

    @Override
    public boolean isWriteSupported() {
        return true;
    }

    @Override
    public ContentReader getReader(String contentUrl) {

        String key = makeS3Key(contentUrl);
        return new GSContentReader(key, contentUrl, storage, bucketName);

    }

    public void init() {
        if(StringUtils.isNotBlank(this.accessKey)) {
            logger.debug("Found credentials in properties file: " + accessKey);
            byte[] decoded = Base64.decode(accessKey);
            ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
            try {
                storage = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(ServiceAccountCredentials.fromStream(bis)).build().getService();
                logger.debug("Google Create Storage Client Successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
                logger.debug("Google Credentials not specified in properties, will fallback to credentials provider");
        }
    }


    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    public void setRootDirectory(String rootDirectory) {

        String dir = rootDirectory;
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }

        this.rootDirectory = dir;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    protected ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl) {

        String contentUrl = newContentUrl;

        if(StringUtils.isBlank(contentUrl)) {
            contentUrl = createNewUrl();
        }

        String key = makeS3Key(contentUrl);

        return new GSContentWriter(bucketName, key, contentUrl, existingContentReader, storage);

    }

    public static String createNewUrl() {

        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL)
                .append(ContentStore.PROTOCOL_DELIMITER)
                .append(year).append('/')
                .append(month).append('/')
                .append(day).append('/')
                .append(hour).append('/')
                .append(minute).append('/')
                .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;

    }

    private String makeS3Key(String contentUrl)
    {
        // take just the part after the protocol
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String protocol = urlParts.getFirst();
        String relativePath = urlParts.getSecond();
        // Check the protocol
        if (!protocol.equals(FileContentStore.STORE_PROTOCOL))
        {
            throw new UnsupportedContentUrlException(this, protocol + PROTOCOL_DELIMITER + relativePath);
        }
        return rootDirectory + "/" + relativePath;
    }

    @Override
    public boolean delete(String contentUrl) {

        try {
            String key = makeS3Key(contentUrl);
            logger.debug("Deleting object from S3 with url: " + contentUrl + ", key: " + key);
            storage.delete(key);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting S3 Object", e);
        }

        return false;

    }

    /**
     * Publishes an event to the application context that will notify any interested parties of the existence of this
     * content store.
     *
     * @param context
     *            the application context
     * @param extendedEventParams
     */
    private void publishEvent(ApplicationContext context, Map<String, Serializable> extendedEventParams)
    {
        context.publishEvent(new ContentStoreCreatedEvent(this, extendedEventParams));
    }

    public void onApplicationEvent(ApplicationEvent event)
    {
        // Once the context has been refreshed, we tell other interested beans about the existence of this content store
        // (e.g. for monitoring purposes)
        if (event instanceof ContextRefreshedEvent && event.getSource() == this.applicationContext)
        {
            publishEvent(((ContextRefreshedEvent) event).getApplicationContext(), Collections.<String, Serializable> emptyMap());
        }
    }
}
