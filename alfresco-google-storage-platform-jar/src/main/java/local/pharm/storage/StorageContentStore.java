package local.pharm.storage;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StorageContentStore extends AbstractContentStore
        implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private String projectId;
    private String bucketName;
    private String rootDirectory;

    private static final Log logger = LogFactory.getLog(StorageContentStore.class);

    @Override
    public boolean isWriteSupported() {
        return false;
    }

    @Override
    public ContentReader getReader(String contentUrl) {
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

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

}
