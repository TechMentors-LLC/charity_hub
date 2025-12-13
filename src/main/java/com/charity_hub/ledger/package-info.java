/**
 * Ledger Module - Manages financial records and member network.
 * 
 * This module follows Clean Architecture with:
 * - api: Controllers and DTOs
 * - application: Commands, queries, and use cases
 * - domain: Entities and domain logic
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
        "cases :: shared",
        "cases :: dtos",
        "notifications",
        "notifications :: shared"
})
package com.charity_hub.ledger;
