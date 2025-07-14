package com.charity_hub.accounts.internal.core.model.account;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Connection {
    @Getter
    @Setter
    private AccountId parent;
    private final List<AccountId> children;

    public Connection(AccountId parent, List<AccountId> children) {
        this.parent = parent;
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    public List<AccountId> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(AccountId childId) {
        if (!children.contains(childId)) {
            children.add(childId);
        }
    }
}
