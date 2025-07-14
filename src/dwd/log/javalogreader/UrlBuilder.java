package dwd.log.javalogreader;

public class UrlBuilder {
    private static final String HTTP_REQUEST_BEGIN = "http://";
    private static final char HTTP_REQUEST_SLASH = '/';

    public static String buildUrl(String host, String path, String file) {
        if (host == null || path == null || file == null) {
            throw new IllegalArgumentException("Host, path, and file cannot be null");
        }

        StringBuilder urlBuilder = new StringBuilder(HTTP_REQUEST_BEGIN);

        // Append host with trailing slash if needed
        urlBuilder.append(host);
        if (host.charAt(host.length() - 1) != HTTP_REQUEST_SLASH) {
            urlBuilder.append(HTTP_REQUEST_SLASH);
        }

        // Append path
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        urlBuilder.append(path);

        // Append file with proper slash handling
        if (!path.endsWith("/") && !file.startsWith("/")) {
            urlBuilder.append(HTTP_REQUEST_SLASH);
        } else if (path.endsWith("/") && file.startsWith("/")) {
            file = file.substring(1);
        }
        urlBuilder.append(file);

        return urlBuilder.toString();
    }
}
