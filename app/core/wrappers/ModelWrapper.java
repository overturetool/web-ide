package core.wrappers;

import core.utilities.ResourceCache;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.lex.Dialect;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.messages.Console;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.util.ExitStatus;
import org.overture.interpreter.values.Value;
import org.overture.parser.lex.LexTokenReader;
import org.overture.parser.syntax.ExpressionReader;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;
import play.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ModelWrapper {
    private ModuleInterpreter interpreter;

    private static final int MAX_AVAILABLE = 1;
    private static final Semaphore available = new Semaphore(MAX_AVAILABLE, true);

    public ModelWrapper(IVFS<FileObject> file) {
        available.acquireUninterruptibly();

        if (ResourceCache.getInstance().existsAndNotModified(file)) {
            this.interpreter = ResourceCache.getInstance().get(file).getInterpreter();
            //this.interpreter.init(null);
        } else {
            List<File> files = Collections.synchronizedList(new ArrayList<>());
            files.add(file.getIOFile()); // TODO : should not be done if file is a directory, but overture core takes care of it.

            List<File> siblings = file.getSiblings();
            if (siblings != null && !siblings.isEmpty())
                files.addAll(siblings);
            else if (file.isDirectory())
                files.addAll(file.readdirAsIOFile(-1));

            init(files);

            ResourceCache.getInstance().add(file, this.interpreter);
        }

        available.release();
    }

    public ModelWrapper(List<File> files) {
        Logger.debug("Here");
        available.acquireUninterruptibly();
        init(files);
        available.release();
    }

    public ModelWrapper() {
        try {
            if (this.interpreter == null) {
                this.interpreter = new ModuleInterpreter(new ModuleList());
                this.interpreter.init(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String evaluate(String input) {
        try {
            if (input.contains(":=")) {
                String[] strings = input.replaceAll(" ", "").split(":=");
                String var = strings[0];
                String exp = strings[1];
                create(var, exp);
                return this.interpreter.evaluate(input.trim(), this.interpreter.initialContext).toString();
            } else {
                return this.interpreter.evaluate(input.trim(), this.interpreter.initialContext).toString();
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    private void create(String var, String exp) throws Exception {
        PExp pExp = parseExpression(exp);
        PType type = pExp.getType();

        //PType type = this.interpreter.typeCheck(exp);//(pExp, created);
        Value value = this.interpreter.evaluate(exp, this.interpreter.initialContext);

        ILexLocation location = new LexLocation(
                this.interpreter.getDefaultFile().getPath(),
                this.getTargetModuleName(), 0, 0, 0, 0, 0, 0);

        LexNameToken name = new LexNameToken(getTargetModuleName(), var, location);

        this.interpreter.initialContext.put(name, value);

        AValueDefinition def = AstFactory.newAValueDefinition(AstFactory.newAIdentifierPattern(name), NameScope.GLOBAL, type, pExp);

        LinkedList<PDefinition> defs = this.interpreter.defaultModule.getDefs();
        defs.add(def);
        this.interpreter.defaultModule.setDefs(defs);
    }

    private PExp parseExpression(String exp) throws Exception {
        LexTokenReader ltr = new LexTokenReader(exp, Dialect.VDM_SL, Console.charset);
        ExpressionReader reader = new ExpressionReader(ltr);
        reader.setCurrentModule(this.interpreter.getDefaultName());
        return reader.readExpression();
    }

    public String getTargetModuleName() {
        return this.interpreter.getDefaultName();
    }

    public ModuleList getAst() {
        if (this.interpreter != null)
            return this.interpreter.getModules();
        else
            return new ModuleList();
    }

    public IProofObligationList getPog() {
        try {
            if (this.interpreter != null)
                return this.interpreter.getProofObligations();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
        return new ProofObligationList();
    }

    private synchronized void init(List<File> files) {
        // Look into using the VDMJ class instead
        VDMSL vdmsl = new VDMSL();
        vdmsl.setWarnings(false);
        vdmsl.setQuiet(true);

        ExitStatus parseStatus = vdmsl.parse(files);

        if (parseStatus == ExitStatus.EXIT_OK) {
            ExitStatus typeCheckStatus = vdmsl.typeCheck();

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.interpreter = vdmsl.getInterpreter();
                    this.interpreter.defaultModule.setTypeChecked(true);
                    this.interpreter.init(null);
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }

        // Safety-net to avoid NullPointerExceptions
        try {
            if (this.interpreter == null) {
                this.interpreter = new ModuleInterpreter(new ModuleList());
                this.interpreter.init(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
