package org.overture.webide.processing;

import org.overture.ast.lex.Dialect;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.webide.processing.utils.Arguments;
import org.overture.webide.processing.utils.ProcessingUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class ProcessingMain {
    public static String host = null;
    public static String baseDir = null;
    public static int port = -1;
    public static boolean printInfo = false;
    public static File baseDirFile = null;
    public static List<File> fileList = new ArrayList<>();

    protected static Class<? extends ProcessingMain> instanceClass;
    protected abstract void execute() throws Exception;

    public static void main(String[] args) throws Exception {
        Settings.dialect = Dialect.VDM_PP;
        Settings.release = Release.VDM_10;

        Iterator<String> i = Arrays.asList(args).iterator();

        while(i.hasNext()) {
            String arg = i.next();
            if (arg.equals(Arguments.Identifiers.Host) && i.hasNext()) {
                host = i.next();
            } else if (arg.equals(Arguments.Identifiers.Port) && i.hasNext()) {
                port = Integer.parseInt(i.next());
            } else if (arg.equals(Arguments.Identifiers.Release) && i.hasNext()) {
                Settings.release = Release.lookup(i.next());
            } else if (arg.equals(Arguments.Identifiers.BaseDir) && i.hasNext()) {
                baseDir = i.next();
                baseDirFile = new File(baseDir);
                ProcessingUtils.validateBaseDir(baseDirFile);
            } else if (arg.equals(Arguments.Identifiers.PrintInfo)) {
                printInfo = true;
            } else if (arg.equals(Arguments.Dialects.VDM_PP)) {
                Settings.dialect = Dialect.VDM_PP;
            } else if (arg.equals(Arguments.Dialects.VDM_RT)) {
                Settings.dialect = Dialect.VDM_RT;
            } else if (arg.equals(Arguments.Dialects.VDM_SL)) {
                Settings.dialect = Dialect.VDM_SL;
            } else {
                fileList.addAll(ProcessingUtils.handleFiles(arg));
            }
        }

        if (printInfo)
            System.out.println("process " + ProcessingUtils.getPID() + " ready");

        ProcessingMain p = instanceClass.newInstance();
        p.execute();
    }
}
