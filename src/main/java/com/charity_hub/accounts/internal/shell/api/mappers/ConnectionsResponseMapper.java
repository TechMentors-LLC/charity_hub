package com.charity_hub.accounts.internal.shell.api.mappers;

import com.charity_hub.accounts.internal.core.model.account.ConnectionsInfo;
import com.charity_hub.accounts.internal.shell.api.dtos.ConnectionsResponse;
import com.charity_hub.accounts.internal.shell.api.dtos.MinimalAccountDTO;
import org.springframework.stereotype.Component;

@Component
public class ConnectionsResponseMapper {
    
    public ConnectionsResponse toResponse(ConnectionsInfo connectionsInfo) {
        if (connectionsInfo == null) {
            return new ConnectionsResponse(null, java.util.List.of());
        }
        
        MinimalAccountDTO parent = connectionsInfo.parent() != null 
            ? new MinimalAccountDTO(
                connectionsInfo.parent().id().value().toString(),
                connectionsInfo.parent().mobileNumber(),
                connectionsInfo.parent().fullName()
            ) : null;
        
        var children = connectionsInfo.children().stream()
            .map(child -> new MinimalAccountDTO(
                child.id().value().toString(),
                child.mobileNumber(),
                child.fullName()
            ))
            .toList();
        
        return new ConnectionsResponse(parent, children);
    }
}
