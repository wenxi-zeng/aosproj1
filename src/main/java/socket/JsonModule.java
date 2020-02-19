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
            @JsonProperty("senderId") String senderId,
            @JsonProperty("receiverId") String receiverId,
            @JsonProperty("type") String type,
            @JsonProperty("attachment") String attachment,
            @JsonProperty("timestamp") long timestamp
    ) { }
    @JsonProperty("header") abstract String getHeader();
    @JsonProperty("sender") abstract String getSender();
    @JsonProperty("receiver") abstract String getReceiver();
    @JsonProperty("senderId") abstract String getSenderId();
    @JsonProperty("receiverId") abstract String getReceiverId();
    @JsonProperty("type") abstract String getType();
    @JsonProperty("attachment") abstract String getAttachment();
    @JsonProperty("timestamp") abstract long getTimestamp();
}

abstract class ResponseMixin {
    @JsonCreator
    ResponseMixin(
            @JsonProperty("header") String header,
            @JsonProperty("status") short status,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") long timestamp
    ) { }
    @JsonProperty("header") abstract String getHeader();
    @JsonProperty("status") abstract short getStatus();
    @JsonProperty("message") abstract String getMessage();
    @JsonProperty("timestamp") abstract long getTimestamp();
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
