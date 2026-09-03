package eu.europeana.dsp.connector.controlplane.catalog.spi;

import java.util.Map;

public interface ResourceDescriptionProvider {

    Resource getResourceInfo();

    Resource buildResourceInfo(Map<String, Object> properties);

    void setResourceInfo(Resource resourceInfo);

}
