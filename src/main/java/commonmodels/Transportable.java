package commonmodels;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import commonmodels.transport.Request;
import commonmodels.transport.Response;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Request.class, name = "Request"),
        @JsonSubTypes.Type(value = Response.class, name = "Response"),
        @JsonSubTypes.Type(value = TransportableString.class, name = "TransportableString")
})
public class Transportable {
}
