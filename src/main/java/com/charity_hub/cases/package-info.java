/**
 * Cases Module - Manages charity cases and contributions.
 * 
 * This module follows Clean Architecture with:
 * - api: Controllers and DTOs
 * - application: Commands, queries, and use cases
 * - domain: Entities, events, and domain logic
 * - infrastructure: Database and external service implementations
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "shared",
        "shared :: auth",
        "shared :: abstractions",
        "shared :: domain",
        "shared :: domain.model",
        "shared :: domain.extension",
        "shared :: exceptions",
        "accounts :: shared",
        "ledger",
        "notifications",
        "notifications :: shared"
})
package com.charity_hub.cases;
