package com.charity_hub.accounts.internal.shell.api.dtos;

import java.util.List;

public record ConnectionsResponse(MinimalAccountDTO parent, List<MinimalAccountDTO> children) {}