package core.wrappers;

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
import org.overture.interpreter.messages.Console;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.values.Value;
import org.overture.parser.lex.LexTokenReader;
import org.overture.parser.syntax.ExpressionReader;

import java.util.LinkedList;

public class Evaluator {
    private final String methodPrefix = "&";
    private final String assignmentOperator = ":=";
    private final String setDefault = methodPrefix + "setDefault";
    private final String getDefault = methodPrefix + "getDefault";
    private final String help = methodPrefix + "help";
    private final String space = " ";

    private ModuleInterpreter interpreter;

    public Evaluator(ModuleInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    public synchronized String evaluate(String input) {
        input = input.trim();

        try {
            if (!input.startsWith(methodPrefix) && input.contains(assignmentOperator)) {
                return processAssignment(input);
            } else if (input.startsWith(setDefault, 0) && input.contains(assignmentOperator)) {
                return processSetDefault(input);
            } else if (input.startsWith(getDefault, 0)) {
                return this.interpreter.getDefaultName();
            } else if (input.equals(help)) {
                return processHelp();
            } else {
                return this.interpreter.evaluate(input, this.interpreter.initialContext).toString();
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    private String processAssignment(String input) throws Exception {
        String[] strings = input.replaceAll(space, "").split(assignmentOperator);

        if (strings.length != 2)
            return "Error: invalid assignment format";

        String var = strings[0];
        String exp = strings[1];
        create(var, exp);

        return this.interpreter.evaluate(input, this.interpreter.initialContext).toString();
    }

    private String processSetDefault(String input) {
        String[] strings = input.replaceAll(space, "").split(assignmentOperator);
        String defaultName = strings[1];
        String oldDefaultName = this.interpreter.getDefaultName();

        try {
            this.interpreter.setDefaultName(defaultName);
        } catch (Exception e) {
            return "Error: " + e.toString();
        }

        return "Default changed from " + oldDefaultName + " to " + defaultName;
    }

    private String processHelp() {
        StringBuilder helpString = new StringBuilder();
        helpString.append("These are the Overture webIDE REPL commands:")
                .append(System.lineSeparator());
        helpString.append("   Get default module name:        ").append(getDefault)
                .append(System.lineSeparator());
        helpString.append("   Set default module name:        ").append(setDefault).append(" ").append(assignmentOperator).append(" <module_name>")
                .append(System.lineSeparator());
        helpString.append("   Define variable:                <var_name> ")
                .append(assignmentOperator).append(" <value>");
        return helpString.toString();
    }

    private void create(String var, String exp) throws Exception {
        PExp pExp = parseExpression(exp);
        PType type = pExp.getType();

        Value value = this.interpreter.evaluate(exp, this.interpreter.initialContext);

        ILexLocation location = new LexLocation(
                this.interpreter.getDefaultFile().getPath(),
                this.interpreter.getDefaultName(), 0, 0, 0, 0, 0, 0);

        LexNameToken name = new LexNameToken(this.interpreter.getDefaultName(), var, location);

        this.interpreter.initialContext.put(name, value);

        AValueDefinition def = AstFactory.newAValueDefinition(
                AstFactory.newAIdentifierPattern(name),
                NameScope.GLOBAL, type, pExp);

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
}
