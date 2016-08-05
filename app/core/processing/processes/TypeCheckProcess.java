package core.processing.processes;

import core.ServerConfigurations;
import org.overture.webide.processing.ProcessingMain;
import org.overture.webide.processing.utils.Arguments;

import java.nio.file.Paths;

public class TypeCheckProcess extends AbstractProcess {
    public TypeCheckProcess(int port) {
        super(Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString(), ProcessingMain.class.getCanonicalName());

        // Program arguments
        args.add(Arguments.Actions.TypeCheck);
        args.add(Arguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(Arguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(Arguments.Identifiers.PrintInfo);
    }
}
