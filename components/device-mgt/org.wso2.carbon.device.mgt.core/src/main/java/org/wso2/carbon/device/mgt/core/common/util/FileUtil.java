/*
 * Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.common.util;

import org.apache.commons.io.FileUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class FileUtil {

    /**
     * Useful to remove path separator string "," from base64 string
     *
     * @param base64String base64 string
     * @return base64 string without path separator
     */
    public static String removePathSeparatorFromBase64String(String base64String) {
        String partSeparator = ",";
        if (base64String.contains(partSeparator)) {
            return base64String.split(partSeparator)[1];
        }
        return base64String;
    }

    /**
     * Useful to convert base64 string to input stream
     *
     * @param base64 base64 string to be converted
     * @return {@link InputStream} of the provided base64 string
     */
    public static InputStream base64ToInputStream(String base64) {
        base64 = FileUtil.removePathSeparatorFromBase64String(base64);
        byte[] base64Bytes = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(base64Bytes);
    }

    /**
     * Useful to convert input stream to base64 string
     *
     * @param file stream to be converted
     * @return base64 string of the provided input stream
     */
    public static String fileToBase64String(File file) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * This generates file name with a suffix depending on the duplicate name count, useful when saving
     * files with the same name
     *
     * @param fileNameCount File count
     * @return generated file name with suffix
     */
    public static String generateDuplicateFileName(String fileName, int fileNameCount) {
        String suffix = generateDuplicateFileNameSuffix(fileNameCount);
        String fileNameWithoutExtension = extractFileNameWithoutExtension(fileName);
        String fileNameWithSuffix = fileNameWithoutExtension + suffix;
        fileNameWithSuffix = fileNameWithSuffix + '.' + extractFileExtension(fileName);
        return fileNameWithSuffix;
    }

    /**
     * This generates file name suffix for duplicate file names. For example,
     * if it's the first file, the count is 1 in which case the name doesn't have any suffix
     * else it adds suffix with (count - 1)
     *
     * @param fileNameCount File count
     * @return generated file name suffix
     */
    private static String generateDuplicateFileNameSuffix(int fileNameCount) {
        String suffix = "";
        if (fileNameCount < 1) {
            throw new IllegalArgumentException("file name count must be above 0");
        }
        if (fileNameCount > 1) {
            suffix = "(" + (fileNameCount - 1) + ")";
        }
        return suffix;
    }

    /**
     * Use to extract file extension from file name
     *
     * @param fileName name of the file
     * @return extension of the file
     */
    private static String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * Use to extract the file name without the extension
     * For example if you provide "main.java" as the fileName this will return main
     *
     * @param fileName name of the file
     * @return file name without file extension
     */
    private static String extractFileNameWithoutExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot != -1) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }
}
