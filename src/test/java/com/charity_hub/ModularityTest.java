package com.charity_hub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Test to verify the modularity structure of the application.
 * This ensures that module boundaries are properly defined and respected.
 */
class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(CharityHubApplication.class);

    @Test
    void verifiesModularStructure() {
        // This will fail if any module violates the defined boundaries
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        // Generate documentation for the module structure
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }

    @Test
    void printModuleStructure() {
        // Print the module structure for debugging
        modules.forEach(System.out::println);
    }
}
