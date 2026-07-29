package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.syncjob.model.SyncJob;

public interface ExternalSystemConnector {

    IntegrationType getSupportedType();

    SyncOperationResult sync(SyncJob syncJob);
}
