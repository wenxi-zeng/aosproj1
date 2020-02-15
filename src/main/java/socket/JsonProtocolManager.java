package socket;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import commonmodels.Transportable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JsonProtocolManager {

    private final ObjectMapper objectMapper;

    private static JsonProtocolManager instance;

    private JsonProtocolManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();
        objectMapper.registerModule(new JsonModule());
        objectMapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(
                objectMapper.getSerializationConfig().
                        getDefaultVisibilityChecker().
                        withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                        withGetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public static JsonProtocolManager getInstance() {
        if (instance == null) {
            synchronized(JsonProtocolManager.class) {
                if (instance == null) {
                    instance = new JsonProtocolManager();
                }
            }
        }

        return instance;
    }

    public static void deleteInstance() {
        instance = null;
    }

    public byte[] write(Transportable message) throws IOException {
        return objectMapper.writeValueAsBytes(message);
    }

    public Transportable read(byte[] buf) throws IOException {
        return objectMapper.readValue(buf, Transportable.class);
    }

    public ByteBuffer writeGzip(Transportable message) {
        try {
            byte[] bytes = write(message);
            byte[] compressedBytes = compress(bytes);
            return ByteBuffer.wrap(compressedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ByteBuffer.allocate(0);
    }

    public Transportable readGzip(byte[] bytes) {
        try {
            byte[] decompressedBytes = decompress(bytes);
            return read(decompressedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public long sizeOf(Transportable message) {
        try {
            byte[] bytes = write(message);
            byte[] compressedBytes = compress(bytes);
            return compressedBytes.length;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private byte[] compress(byte[] bytes) throws IOException  {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(bytes);
        gzip.close();
        return out.toByteArray();
    }

    private byte[] decompress(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        gunzip.close();
        return out.toByteArray();
    }
}
