package org.overture.webide.interpreter_util;

import org.overture.ast.intf.lex.ILexLocation;

import java.io.Serializable;

public class VDMMessageSerializable implements Serializable {
    //private static final long serialVersionUID = 2L;

    public final int number;
    public final String message;
    public final ILexLocation location;

    public VDMMessageSerializable(int number, String message, ILexLocation location) {
        this.number = number;
        this.message = message;
        this.location = location;
    }
}
