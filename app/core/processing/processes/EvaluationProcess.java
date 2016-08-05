package core.processing.processes;

import core.ServerConfigurations;
import org.overture.webide.processing.ProcessingMain;
import org.overture.webide.processing.utils.Arguments;

import java.nio.file.Paths;

public class EvaluationProcess extends AbstractProcess {
    public EvaluationProcess(int port, String absolutePath) {
        super(Paths.get("lib", "processing-1.0-SNAPSHOT-jar-with-dependencies.jar").toString(), ProcessingMain.class.getCanonicalName());

        args.add(Arguments.Actions.Evaluate);
        args.add(Arguments.Identifiers.Host);
        args.add(ServerConfigurations.localhostByName);
        args.add(Arguments.Identifiers.Port);
        args.add(Integer.toString(port));
        args.add(Arguments.Dialects.VDM_SL);
        args.add(Arguments.Release.VDM_10);
        args.add(Arguments.Identifiers.PrintInfo);
        args.add(absolutePath);
    }
}
