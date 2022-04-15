package io.entgra.ui.request.interceptor.util;

import org.apache.commons.lang.StringUtils;

import java.net.URI;

public class GrafanaHandlerUtil {
    public static String getGrafanaUri(URI req) {
        StringBuilder grafanaUriBuilder = new StringBuilder();
        grafanaUriBuilder.append(req.getPath().replace(" ", "%20"));
        if (StringUtils.isNotEmpty(req.getQuery())) {
            grafanaUriBuilder.append("?").append(req.getQuery());
        }
        return grafanaUriBuilder.toString();
    }

    public static String generateGrafanaUrl(URI request, String base) {
        return base + GrafanaHandlerUtil.getGrafanaUri(request);
    }
}
