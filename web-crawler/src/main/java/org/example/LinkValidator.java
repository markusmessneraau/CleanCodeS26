package org.example;

import java.net.URI;
import java.net.URISyntaxException;


public class LinkValidator {
    public static boolean isValid(String url, java.util.List<String> allowedDomain) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            URI uri = new URI(url);
            return hasWebProtocol(uri) && hasAllowedDomain(uri, allowedDomain);
        } catch (URISyntaxException e) {
            return false;
        }

    }

    private static boolean hasAllowedDomain(URI uri, java.util.List<String> allowedDomain) {
        String domain = uri.getHost();
        if (domain == null) {
            return false;
        }

        for(String allowed : allowedDomain){

            if (domain.contains(allowed)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasWebProtocol(URI uri) {
        String scheme = uri.getScheme();
        if (scheme != null && (scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) {
            return true;
        }
        return false;
    }
}
