package org.overture.webide.processing.features;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;
import org.overture.webide.processing.models.Result;

import java.io.File;
import java.util.List;

public interface ITypeChecker {
    Result getResult(List<File> fileList, Dialect dialect, Release release);
}
