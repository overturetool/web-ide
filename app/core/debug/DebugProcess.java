package core.debug;

import core.ServerConfigurations;
import core.processing.processes.AbstractProcess;
import core.vfs.IVFS;
import org.overture.interpreter.VDMJ;
import org.overture.interpreter.debug.DBGPReaderV2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class DebugProcess extends AbstractProcess {
    public DebugProcess(int port, String type, String entry, String defaultName, IVFS file, boolean coverage) throws IOException {
        super(Paths.get("lib", "Overture-2.3.6.jar").toString(), DBGPReaderV2.class.getCanonicalName());

        if (type != null) {
            args.add("-" + type);
        }

        args.add("-h");
        args.add(ServerConfigurations.localhostByName);
        args.add("-p");
        args.add(Integer.toString(port));
        args.add("-k");
        args.add(ServerConfigurations.dbgpKey);

        if (entry != null) {
            args.add("-e");
            args.add(entry);
        }

        if (defaultName != null) {
            args.add("-default64");
            args.add(defaultName);
        }

        if (coverage) {
            args.add("-coverage");
            args.add("file:" + mkOutputDir(file).getAbsolutePath());
        }

        args.add("-w"); // turn off warnings
        args.add("-q"); // quiet
        args.add("-c");
        args.add(VDMJ.filecharset);
        args.add(file.getAbsoluteUrl());
    }

    private File mkOutputDir(IVFS file) {
        LocalDateTime dateTime = LocalDateTime.now();
        String time = dateTime.toLocalDate().toString().replaceAll("-", "_") + "_";
        time += dateTime.toLocalTime().toString().substring(0, 8).replaceAll(":", "_");
        File outputDir = Paths.get(file.getAbsolutePath(), "generated", time).toFile();

        if (!outputDir.exists())
            outputDir.mkdirs();

        return outputDir;
    }
}
