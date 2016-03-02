package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import core.ServerConfigurations;
import core.StatusCode;
import core.auth.SessionStore;
import core.utilities.FileOperations;
import core.utilities.ServerUtils;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class importProject extends Application {
    public Result get(String projectUrl) {
        String accessToken = ServerUtils.extractAccessToken(request());
        String userId = SessionStore.getInstance().get(accessToken);

        // TODO : Remember to remove!
        userId = userId == null ? "111425625270532893915" : userId;

        Path destinationAccount = Paths.get(ServerConfigurations.basePath, userId);

        Path zipFilePath = FileOperations.downloadFile(projectUrl, destinationAccount.toString());
        if (zipFilePath == null)
            return status(StatusCode.UnprocessableEntity, "Exception occurred while downloading file");

        String baseDirectoryName;
        try {
            baseDirectoryName = FileOperations.unzip(zipFilePath.toFile(), destinationAccount.toString());
            zipFilePath.toFile().delete();
        } catch (IOException e) {
            return status(StatusCode.UnprocessableEntity, e.toString());
        }

        Path destinationDirectory = Paths.get(destinationAccount.toString(), baseDirectoryName);
        FileOperations.filterDirectoryContent(destinationDirectory.toFile(), new String[]{"vdmsl", "txt"});

        return ok(baseDirectoryName);
    }

    public Result list() {
        // https://api.github.com/repos/overturetool/documentation/contents/examples/VDMSL?ref=editing
        ArrayNode node = Json.newArray();
        node.add("http://overturetool.org/download/examples/VDMSL/AbstractPacemakerSL/AbstractPacemaker.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/AccountSysSL/AccountSys.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ACSSL/ACS.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ADTSL/ADT.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/AlarmErrSL/AlarmErr.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/AlarmSL/Alarm.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ATCSL/ATC.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/barSL/Bar.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ATCSL/ATC.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/BOMSL/BOM.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/cashdispenserSL/Cashdispenser.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/CMSL/CM.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ConwayGameLifeSL/ConwayGameLife.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/CountryColouringSL/CountryColouring.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/crosswordSL/Crossword.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/DFDexampleSL/DFDexample.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/DigraphSL/Digraph.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/dwarfSL/Dwarf.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/EngineSL/Engine.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/expressSL/Express.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/gatewaySL/Gateway.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/graph-edSL/Graph-ed.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/HASLSL/HASL.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/hotelSL/Hotel.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/librarySL/Library.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/looseSL/Loose.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/LUHNSL/LUHN.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/LUPSL/LUP.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/MAASL/MAA.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/metroSL/Metro.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/monitorSL/Monitor.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/NDBSL/NDB.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/newspeakSL/Newspeak.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/pacemakerSL/Pacemaker.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/PlannerSL/Planner.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/ProgLangSL/ProgLang.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/raildirSL/Raildir.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/realmSL/Realm.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/recursiveSL/Recursive.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/SAFERSL/SAFER.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/shmemSL/Shmem.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/simulatorSL/Simulator.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/soccerSL/Soccer.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/STVSL/STV.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/telephoneSL/Telephone.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/Tic-tac-toeSL/Tic-tac-toe.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/TrackerSL/Tracker.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/trafficSL/Traffic.zip");
        node.add("http://overturetool.org/download/examples/VDMSL/VCParser-masterSL/VCParser-master.zip");
        return ok(node);
    }
}
