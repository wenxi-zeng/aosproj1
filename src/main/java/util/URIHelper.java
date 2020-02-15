package util;

import commonmodels.transport.InvalidRequestException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIHelper {

    public static URI getGossipURI(String ip, int port) throws URISyntaxException {
        return new URI("udp://" + ip + ":" + port);
    }

    public static URI getGossipURI(String address) throws URISyntaxException {
        return new URI("udp://" + address);
    }

    public static void verifyAddress(String[] args) throws InvalidRequestException {
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        for (String arg : args) {
            Matcher matcher = pattern.matcher(arg);
            if (matcher.find() && !arg.contains(":")) {
                throw new InvalidRequestException("Invalid IP address");
            }
        }
    }
}
