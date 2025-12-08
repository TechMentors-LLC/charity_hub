package com.charity_hub.shared.observability.health;

import com.google.firebase.FirebaseApp;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Health indicator for Firebase connectivity.
 * Checks if Firebase is properly initialized and accessible.
 */
@Component
public class FirebaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
            
            if (firebaseApps.isEmpty()) {
                return Health.down()
                    .withDetail("reason", "No Firebase app instances found")
                    .build();
            }
            
            // Check if default app is initialized
            boolean defaultAppExists = firebaseApps.stream()
                .anyMatch(app -> FirebaseApp.DEFAULT_APP_NAME.equals(app.getName()));
            
            if (defaultAppExists) {
                return Health.up()
                    .withDetail("appCount", firebaseApps.size())
                    .withDetail("status", "Firebase is initialized and ready")
                    .build();
            } else {
                return Health.down()
                    .withDetail("reason", "Default Firebase app not found")
                    .withDetail("appCount", firebaseApps.size())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
