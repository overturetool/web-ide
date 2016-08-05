package core.processing.processes;

import core.ServerConfigurations;
import org.overture.webide.processing.TypeCheckerMain;
import org.overture.webide.processing.utils.Arguments;

import java.nio.file.Paths;

public class TypeCheckProcess extends AbstractProcess {
    public TypeCheckProcess(int port) {
        super(Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString(), TypeCheckerMain.class.getCanonicalName());
        args.add(Arguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(Arguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(Arguments.Identifiers.PrintInfo);
    }
}
