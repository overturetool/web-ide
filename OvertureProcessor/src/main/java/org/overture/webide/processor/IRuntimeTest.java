package org.overture.webide.processor;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;

import java.io.File;
import java.util.List;

public interface IRuntimeTest {
    int getTest();
    ProcessingResult getProcessingResultNonStatic(List<File> fileList, Dialect dialect, Release release);
}
