package io.entgra.application.mgt.common;

import java.io.InputStream;

public class FileDataHolder {

    private String name;
    private InputStream file;

    public FileDataHolder(String name, InputStream file) {
        this.name = name;
        this.file = file;
    }
    public FileDataHolder() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }
}
