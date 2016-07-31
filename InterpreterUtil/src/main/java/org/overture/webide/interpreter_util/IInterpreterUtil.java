package org.overture.webide.interpreter_util;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;

import java.io.File;
import java.util.List;

public interface IInterpreterUtil {
    Result getResult(List<File> fileList, Dialect dialect, Release release);
}
