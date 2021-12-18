package org.wso2.carbon.device.mgt.core.common.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class FileUtil {

    public static String removePathSeparatorFromBase64String(String base64String) {
        String partSeparator = ",";
        if (base64String.contains(partSeparator)) {
            return base64String.split(partSeparator)[1];
        }
        return base64String;
    }

    public static InputStream base64ToInputStream(String base64) {
        base64 = FileUtil.removePathSeparatorFromBase64String(base64);
        byte[] base64Bytes = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(base64Bytes);
    }


}
