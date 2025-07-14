package com.charity_hub.accounts.internal.shell.db;

import java.util.List;

public record ConnectionEntity(String parent, List<String> children) {
}
