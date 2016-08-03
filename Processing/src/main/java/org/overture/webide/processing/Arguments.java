package org.overture.webide.processing;

public class Arguments {
    public static class Actions {
        public static final String TypeCheck = "-typeCheck";
        public static final String Evaluate = "-evaluate";
    }

    public static class Dialects {
        public static final String VDM_SL = "-vdmsl";
        public static final String VDM_PP = "-vdmpp";
        public static final String VDM_RT = "-vdmrt";
    }

    public static class Identifiers {
        public static final String Host = "-h";
        public static final String Port = "-p";
        public static final String Timeout = "-timeout";
        public static final String PrintInfo = "-printInfo";
        public static final String Dir = "-dir";
    }

    public static class Release {
        public static final String VDM_10 = "-vdm10";
        public static final String CLASSIC = "-classic";
    }
}
