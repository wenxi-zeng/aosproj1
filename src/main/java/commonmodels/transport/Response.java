package commonmodels.transport;

import commonmodels.Transportable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Response extends Transportable implements Serializable
{
    private String header;
    private short status;
    private String message;
    private String token;
    private long timestamp;
    private Object attachment;
    private final static long serialVersionUID = 7313299026043073913L;

    public final static short STATUS_SUCCESS = 1;
    public final static short STATUS_FAILED = 0;
    public final static short STATUS_INVALID_REQUEST = 2;

    /**
     * No args constructor for use in serialization
     *
     */
    public Response() {
        super();
        this.timestamp = System.currentTimeMillis();
    }

    public Response(Request request) {
        this();
        if (request != null) {
            this.header = request.getHeader();
            this.token = request.getToken();
        }
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Response withHeader(String header) {
        this.header = header;
        return this;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public Response withStatus(short status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Response withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Response withToken(String token) {
        this.token = token;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Response withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Response withAttachment(Object attachment) {
        this.attachment = attachment;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("        \n").append("header", header)
                .append("        \n").append("status", status)
                .append("        \n").append("message", message)
                .append("        \n").append("attachment", attachment)
                .append("        \n").toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(header).append(message).append(status).append(attachment).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Response) == false) {
            return false;
        }
        Response rhs = ((Response) other);
        return new EqualsBuilder().append(header, rhs.header).append(message, rhs.message).append(status, rhs.status).append(attachment, rhs.attachment).isEquals();
    }

}
