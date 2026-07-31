package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class IntegrationConnectorRegistry {

    private final Map<IntegrationType, ExternalSystemConnector> connectors;

    public IntegrationConnectorRegistry(List<ExternalSystemConnector> connectorList) {
        this.connectors = new EnumMap<>(IntegrationType.class);

        connectorList.forEach(connector ->
                this.connectors.put(
                        connector.getSupportedType(),
                        connector
                )
        );
    }

    public ExternalSystemConnector getConnector(IntegrationType integrationType) {
        ExternalSystemConnector connector = connectors.get(integrationType);

        if (connector == null) {
            throw new IllegalStateException(
                    "No connector found for integration type: " + integrationType
            );
        }

        return connector;
    }
}
