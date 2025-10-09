package yoga.irai.server.app.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseConfigTest {

    private FirebaseConfig firebaseConfig;

    @BeforeEach
    void setUp() {
        firebaseConfig = new FirebaseConfig();
    }

    @Test
    void testFirebaseAppBean_Success() throws Exception {
        String base64 = "ew0KICAidHlwZSI6ICJzZXJ2aWNlX2FjY291bnQiLA0KICAicHJvamVjdF9pZCI6ICJwcmV2aWV3LWlyYWkteW9nYS12MSIsDQogICJwcml2YXRlX2tleV9pZCI6ICIzYmJmOGFmMTJkMDk1OWYzNGI3NTg1M2M0OTkxZjVjY2M0M2QwYzE4IiwNCiAgInByaXZhdGVfa2V5IjogIi0tLS0tQkVHSU4gUFJJVkFURSBLRVktLS0tLVxuTUlJRXZnSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS2d3Z2dTa0FnRUFBb0lCQVFEcEQyaktOWTd0a3hBM1xuRHRhS2lTRWVUN1krK3JydXJBbDNXUHhXbC9hcnN3d1RyRUZQajVOMFJ4TUMvK05DQU9RTDJNdlJ3a3k5U2t4TVxudy9JUjJBdE95U3hsNzg0SXRLRDlzTTlXdHpOVkxscmpSSWczTTFLL2lDeWk2dHlqUUFVSnFkaHUvMUtzcFF0RVxuYmxGVmdQc3JOOG5IRmU0VDVub1dxdkd2dlJ0VllJRjNxT29rTXVKTi8wVlhiOUl5eFQwRko5VTJhaHZwSU9NQ1xuZFQ4c01LeGhGZzMzdmpXQy9sbTV6eWFmVCtscmZ5S29obVpoSXA3QVhKSmV3RzFKTGRJMzZFK3AvUm9QY3Y4ZVxuSzJHU3BrZWk4NG1kMnp0VG5UbTE2ZFBYajZxYTIrcHJYTHpuYjl0NVVRT3JoRE4wNWNUMUJLUzlsOXRFOEFyK1xuYmFVMDRtMmRBZ01CQUFFQ2dnRUFPRE9iRllnczIyV25kMStBcFEvRWlNYTRNQi9PakZlNFFLdVl6bGovQnhDNlxuVE5ReE83cG4ySlo3SXpJSjM1VFBhWHZ5U3JjTVFtbDBWTjY3dFVuZ0VETzdIU25qQ1g1bGhFZnErQUpkd3FEWFxuRWc4b3VnY0tZVjI1UmVCVkxKajNuZ2p6UkhFVndzUmFIenByS3E5YjRtUWxxSGpGWE04cmllUFhQSVhoM1loZVxuVTNWTHl5dHJJbTNPdlZ1N0wrK0M1ZmIxcGhuTFl2NzdNYTBTNEk2SlpiRUVEMldSRk9qcWk0ZkY2cm11dnpQM1xuU1M4WGFwRnJsSmhIVHUyR1ZWQ0hLa3FpelBRUjdCZlpoUURyM3l3VWVQR096NnFxektlQ3hNMHg1RWtWV2lnSVxua1ZaVjhOaFM2NSs0SWxPQ2tOclZhQjdZNXA2aUYwaHhkOE9lWEd5aEFRS0JnUUQ3M1VRR3Q1L0ExNmdXUHJ4WVxuZ3BDcEhzOS9zSXlTOHE5OHQ4aUtxNmNlTndJSW94MEMyMzIwRXNWbUJ2cmV5SjZuK1AwWTV6eFRUaHFqbTBGaVxuRVVIYjJJaFJrcEhaR2lnME93bVJpbUVQTWo4UjVRbmlhREZlZkJSalE5eCtyb1BKQVAySXg0dXZpVTZFenRKTlxuTGhuUXJTckh0VERURnBxTENjZEt2dklnVVFLQmdRRHM0eGxKUmJZVS9mQkZ5YTF3MFNSRXcwTDFXaExtcEQ4RVxuOWZ3aUcwNS9JT2M0VVFvbjdGaU1jTVNvcFV3a0ZmaXIrWjh4ZFZtNDZrSC93S0hBdVhaN3pVejlnOVpQcWVpS1xuQTlBNUlFOUU3Y25yR1lmbFowVDlUV2JzUG9ORE1reU5tVXpnOFZVcytaOEtoak40OXJCaFdHOGhzTDU5OGVlNlxuSXhiZG1qMVJqUUtCZ0Y4Y3p1eXVlVkRNMy9nT2x2Zmh6cGxmUGtMZFBDamVKdkUxUWpGRDQyZGdwSTc0Y2lNN1xuR2p4ZmZsWXFPcURaS0RrTXFSKzNheXVXYWk3d21YOWNQdit6eEw3eFY4eWh1UjhJVUhPNnl5NSs3aHhtM3hXMVxuL1RiT0tyRFYxcldzVjY0WGhxY1puQ0djKzU3aHI3OWZzbllBZnhCTjEzTlByT3JtdFhEL3JOWVJBb0dCQUk1aVxuY25CNFZsbUh5VVFwVlBiYXZ0SVdDOGt0dXU2bkNyb1FvTnhmVFc5Y2Q1NkNGM2ludHhHek5vR24yWHhYT0lFbVxuS1dtdy81MEdNV1ZvZzlFenFPUUJJcVcrK0xJcGpueG1qbUhINmQya2tCbWYzdThrZzNNOWN2L05FMWp3Z3RNWlxuc2Z3REV3OVZleUw0UnpnT0R6WnowVmlPdy9FdjFma3IzaktVNHdhQkFvR0JBTHg3OUhIOWVoU0NCY0JuSUhHMVxuR0NNOGFEbG83aXRoQW1uY25FaERTbi94andZQXF4NkhBUUc2a1BwWU8zT2FTTVZJOTIvN0dRU0U5bW94TG5KWlxuckEzZTU0ZTdZaVdxdmREU2lxSDJsbC9DR05Ja2pxUEhCSDN2UnVoT2dMb2JiZndYMXJlTFBWYnZoRGFGRkloV1xuVHZFMU0vVzlPN0c0V1hhK0ZpK3hCRXJhXG4tLS0tLUVORCBQUklWQVRFIEtFWS0tLS0tXG4iLA0KICAiY2xpZW50X2VtYWlsIjogImZpcmViYXNlLWFkbWluc2RrLWZic3ZjQHByZXZpZXctaXJhaS15b2dhLXYxLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwNCiAgImNsaWVudF9pZCI6ICIxMDA5NDEyNDc4Mjg1MDY4MTA2MzgiLA0KICAiYXV0aF91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL2F1dGgiLA0KICAidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwNCiAgImF1dGhfcHJvdmlkZXJfeDUwOV9jZXJ0X3VybCI6ICJodHRwczovL3d3dy5nb29nbGVhcGlzLmNvbS9vYXV0aDIvdjEvY2VydHMiLA0KICAiY2xpZW50X3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vcm9ib3QvdjEvbWV0YWRhdGEveDUwOS9maXJlYmFzZS1hZG1pbnNkay1mYnN2YyU0MHByZXZpZXctaXJhaS15b2dhLXYxLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwNCiAgInVuaXZlcnNlX2RvbWFpbiI6ICJnb29nbGVhcGlzLmNvbSINCn0NCg";

        ReflectionTestUtils.setField(firebaseConfig, "accountJson", base64);

        try (MockedStatic<yoga.irai.server.app.AppUtils> utils = org.mockito.Mockito.mockStatic(yoga.irai.server.app.AppUtils.class)) {
            utils.when(() -> yoga.irai.server.app.AppUtils.decodeBase64ToString(base64)).thenReturn(new String(Base64.getDecoder().decode(base64)));

            FirebaseApp app = firebaseConfig.firebaseApp();
            assertNotNull(app);

            FirebaseMessaging messaging = firebaseConfig.firebaseMessaging(app);
            assertNotNull(messaging);
        }
    }

    @Test
    void testFirebaseAppBean_InvalidJson_ThrowsException() {
        String invalidBase64 = "invalid_base64";
        ReflectionTestUtils.setField(firebaseConfig, "accountJson", invalidBase64);

        try (MockedStatic<yoga.irai.server.app.AppUtils> utils = org.mockito.Mockito.mockStatic(yoga.irai.server.app.AppUtils.class)) {
            utils.when(() -> yoga.irai.server.app.AppUtils.decodeBase64ToString(invalidBase64)).thenReturn("not_json");

            assertThrows(Exception.class, () -> firebaseConfig.firebaseApp());
        }
    }
}