package org.example;

public class LinkValidator {
    public static boolean isValid(String url, String allowedDomain) {

        if(url == null || url.isEmpty()){
            return false;
        }
        return url.contains(allowedDomain);

    }
}
