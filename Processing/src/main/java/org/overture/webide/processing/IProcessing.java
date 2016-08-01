package org.overture.webide.processing;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;

import java.io.File;
import java.util.List;

public interface IProcessing {
    Result getResult(List<File> fileList, Dialect dialect, Release release);
}
