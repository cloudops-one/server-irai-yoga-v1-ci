package yoga.irai.server.app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class StorageConfigTest {

    private StorageConfig storageConfig;

    @BeforeEach
    void setUp() throws Exception {
        storageConfig = new StorageConfig();
        setField("endpoint", "https://blr1.digitaloceanspaces.com");
        setField("accessKey", "DO801A94RKXJVJ8YTQN9");
        setField("secretKey", "nlNSG/TEbH073yZcCLiqMp+TA3xp+uriFC+2jBbD3mc");
    }

    private void setField(String fieldName, String value) throws Exception {
        Field field = StorageConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(storageConfig, value);
    }

    @Test
    void testS3ClientBean() {
        S3Client client = storageConfig.s3Client();
        assertNotNull(client);
    }
}