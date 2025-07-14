package com.charity_hub.accounts.internal.shell.db;

import org.springframework.data.annotation.Id;

import java.util.List;

public record AccountEntity(
  @Id String accountId,
  String mobileNumber,
  String fullName,
  String photoUrl,
  boolean blocked,
  long joinedDate,
  long lastUpdated,
  List<String> permissions,
  List<DeviceEntity> devices,
  ConnectionEntity connections
  ) {

}