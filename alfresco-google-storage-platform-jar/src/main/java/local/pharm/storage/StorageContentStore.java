package local.pharm.storage;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStoreCreatedEvent;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

public class StorageContentStore extends AbstractContentStore
        implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private ApplicationContext applicationContext;

    private String projectId;
    private String bucketName;
    private String rootDirectory;

    private static final Log logger = LogFactory.getLog(StorageContentStore.class);

    @Override
    public boolean isWriteSupported() {
        return true;
    }

    @Override
    public ContentReader getReader(String contentUrl) {
        String key = makeStorageKey(contentUrl);
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // Once the context has been refreshed, we tell other interested beans about the existence of this content store
        // (e.g. for monitoring purposes)
        if (applicationEvent instanceof ContextRefreshedEvent && applicationEvent.getSource() == this.applicationContext)
        {
            publishEvent(((ContextRefreshedEvent) applicationEvent).getApplicationContext(), Collections.<String, Serializable> emptyMap());
        }
    }
    @Override
    protected ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl) {

        String contentUrl = newContentUrl;

        if(StringUtils.isBlank(contentUrl)) {
            contentUrl = createNewUrl();
        }

        String key = makeStorageKey(contentUrl);

        return null;

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

    public void init() {

    }

    private String makeStorageKey(String contentUrl)
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

}
