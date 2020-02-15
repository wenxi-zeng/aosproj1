package socket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;
import commonmodels.*;
import commonmodels.transport.Request;
import commonmodels.transport.Response;

import java.util.HashMap;
import java.util.List;

abstract class RequestMixin {
    @JsonCreator
    RequestMixin(
            @JsonProperty("header") String header,
            @JsonProperty("sender") String sender,
            @JsonProperty("receiver") String receiver,
            @JsonProperty("followup") String followup,
            @JsonProperty("attachment") String attachment,
            @JsonProperty("epoch") long epoch,
            @JsonProperty("token") String token,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("largeAttachment") Object largeAttachment
    ) { }
    @JsonProperty("header") abstract String getHeader();
    @JsonProperty("sender") abstract String getSender();
    @JsonProperty("receiver") abstract String getReceiver();
    @JsonProperty("followup") abstract String getFollowup();
    @JsonProperty("attachment") abstract String getAttachment();
    @JsonProperty("epoch") abstract long getEpoch();
    @JsonProperty("token") abstract String getToken();
    @JsonProperty("timestamp") abstract long getTimestamp();
    @JsonProperty("largeAttachment") abstract Object getLargeAttachment();
}

abstract class ResponseMixin {
    @JsonCreator
    ResponseMixin(
            @JsonProperty("header") String header,
            @JsonProperty("status") short status,
            @JsonProperty("message") String message,
            @JsonProperty("token") String token,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("attachment") Object attachment
    ) { }
    @JsonProperty("header") abstract String getHeader();
    @JsonProperty("status") abstract short getStatus();
    @JsonProperty("message") abstract String getMessage();
    @JsonProperty("token") abstract String getToken();
    @JsonProperty("timestamp") abstract long getTimestamp();
    @JsonProperty("attachment") abstract Object getAttachment();
}

abstract class TransportableStringMixin {
    @JsonCreator
    TransportableStringMixin(@JsonProperty("value") String value) { }
    @JsonProperty("value") abstract String getValue();
}

public class JsonModule extends SimpleModule {

    private static final long serialVersionUID = 6134836523275023419L;

    public JsonModule() {
        super("JsonModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(Request.class, RequestMixin.class);
        context.setMixInAnnotations(Response.class, ResponseMixin.class);
        context.setMixInAnnotations(TransportableString.class, TransportableStringMixin.class);
    }

}
