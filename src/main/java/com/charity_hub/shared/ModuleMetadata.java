package com.charity_hub.shared;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.PackageInfo;

@PackageInfo
@ApplicationModule(
    type = ApplicationModule.Type.OPEN,
    allowedDependencies = {}
)
public class ModuleMetadata {
}