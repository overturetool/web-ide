package org.overture.webide.processor;

import org.overture.ast.modules.AModuleModules;
import org.overture.parser.messages.VDMError;
import org.overture.parser.messages.VDMMessage;
import org.overture.parser.messages.VDMWarning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessingResult implements Serializable {
    private List<AModuleModules> modules;
    private List<VDMMessageSerializable> parserWarnings;
    private List<VDMMessageSerializable> parserErrors;
    private List<VDMMessageSerializable> typeCheckerWarnings;
    private List<VDMMessageSerializable> typeCheckerErrors;

    public ProcessingResult() {
        this.parserWarnings = new ArrayList<VDMMessageSerializable>();
        this.parserErrors = new ArrayList<VDMMessageSerializable>();
        this.typeCheckerWarnings = new ArrayList<VDMMessageSerializable>();
        this.typeCheckerErrors = new ArrayList<VDMMessageSerializable>();
    }

    public void setParserWarnings(List<VDMWarning> parserWarnings) {
        setVDMMessage(parserWarnings, this.parserWarnings);
    }

    public void setParserErrors(List<VDMError> parserErrors) {
        setVDMMessage(parserErrors, this.parserErrors);
    }

    public void setTypeCheckerWarnings(List<VDMWarning> typeCheckerWarnings) {
        setVDMMessage(typeCheckerWarnings, this.typeCheckerWarnings);
    }

    public void setTypeCheckerErrors(List<VDMError> typeCheckerErrors) {
        setVDMMessage(typeCheckerErrors, this.typeCheckerErrors);
    }

    public void setModules(List<AModuleModules> modules) {
        this.modules = modules;
    }

    private void setVDMMessage(List<? extends VDMMessage> source, List<VDMMessageSerializable> destination) {
        for (VDMMessage message : source) {
            destination.add(new VDMMessageSerializable(message.number, message.message, message.location));
        }
    }

    public List<VDMWarning> getParserWarnings() {
        return getWarnings(this.parserWarnings);
    }

    public List<VDMWarning> getTypeCheckerWarnings() {
        return getWarnings(this.typeCheckerWarnings);
    }

    public List<VDMError> getParserErrors() {
        return getErrors(this.parserErrors);
    }

    public List<VDMError> getTypeCheckerErrors() {
        return getErrors(this.typeCheckerErrors);
    }

    public List<AModuleModules> getModules() {
        return this.modules;
    }

    private List<VDMWarning> getWarnings(List<VDMMessageSerializable> list) {
        List<VDMWarning> vdmWarnings = new ArrayList<VDMWarning>();
        for (VDMMessageSerializable message : list) {
            vdmWarnings.add(new VDMWarning(message.number, message.message, message.location));
        }
        return vdmWarnings;
    }

    private List<VDMError> getErrors(List<VDMMessageSerializable> list) {
        List<VDMError> vdmErrors = new ArrayList<VDMError>();
        for (VDMMessageSerializable message : list) {
            vdmErrors.add(new VDMError(message.number, message.message, message.location));
        }
        return vdmErrors;
    }
}
