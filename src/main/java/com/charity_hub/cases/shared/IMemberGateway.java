package com.charity_hub.cases.shared;

import java.util.UUID;

public interface IMemberGateway {
    boolean isParent(UUID parentId, UUID childId);
}
