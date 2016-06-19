package org.overture.webide.processor;

import org.overture.ast.modules.AModuleModules;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMWarning;

import java.io.Serializable;
import java.util.List;

public class ProcessingResult implements Cloneable, Serializable {
    public List<AModuleModules> modules;
    public List<VDMWarning> parserWarnings;
    public List<VDMWarning> typeCheckerWarnings;
    public List<VDMError> parserErrors;
    public List<VDMError> typeCheckerErrors;
}
