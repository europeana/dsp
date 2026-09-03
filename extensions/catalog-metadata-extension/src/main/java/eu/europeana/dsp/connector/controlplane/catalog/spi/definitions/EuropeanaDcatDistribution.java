package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;
import eu.europeana.dsp.connector.controlplane.catalog.spi.ResourceDescriptionProvider;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a DCAT distribution in the Europeana context by extending the base functionality
 * provided by the {@link Distribution} class and implementing the {@link ResourceDescriptionProvider} interface.
 *
 * This class provides mechanisms to handle and construct resource descriptions adhering to
 * metadata interoperability standards. It can retrieve and construct metadata details about
 * resources encapsulated in the distribution, specifically in compliance with DCAT specifications.
 *
 * @author  Srishti Singh
 * @since 2026-09-1
 */
public class EuropeanaDcatDistribution extends Distribution implements ResourceDescriptionProvider {

    public Map<String, Object> properties;

    @JsonIgnore
    private String distributionId;
    private String format;
    private DataService dataService;

    public EuropeanaDcatDistribution(
            String distributionId,
            Map<String, Object> properties) {

        this.distributionId = distributionId;
        this.properties = properties;
    }

    @Override
    public Resource getResourceInfo() {
        return buildResourceInfo(properties);
    }

    @Override
    public Resource buildResourceInfo(Map<String, Object> properties) {
        return new ResourceResolver(properties);
    }

    @Override
    public void setResourceInfo(Resource resourceInfo) {
        // left empty as we don't need ResourceInfo for DCAT distributions
    }

    public String getDistributionId() {
        return distributionId;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    // setters only for the fields of extended service
    public void setFormat(String format) {
       this.format = format;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
}
