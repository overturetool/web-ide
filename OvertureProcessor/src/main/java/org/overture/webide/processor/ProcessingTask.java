package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class ProcessingTask implements Serializable {
    private List<File> fileList;
    private Dialect dialect;
    private Release release;

    public ProcessingTask(List<File> fileList, Dialect dialect, Release release) {
        this.fileList = fileList;
        this.dialect = dialect;
        this.release = release;
    }

    public List<File> getFileList() {
        return this.fileList;
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public Release getRelease() {
        return this.release;
    }
}
