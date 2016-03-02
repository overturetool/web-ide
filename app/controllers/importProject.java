package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CaseFormat;
import core.ServerConfigurations;
import core.StatusCode;
import core.auth.SessionStore;
import core.utilities.FileOperations;
import core.utilities.ServerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class importProject extends Application {
    public Result getFromGithubApi(String projectUrl) {
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

    public Result listFromGithubApi() {
        String url = "https://api.github.com/repos/overturetool/documentation/contents/examples/VDMSL?ref=editing";
        JsonNode jsonNodes = FileOperations.getContentAsJson(url);

        if (jsonNodes == null)
            return status(StatusCode.UnprocessableEntity, "Could not get content from url");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonArray = mapper.createArrayNode();

        for (JsonNode node : jsonNodes) {
            String exampleUrl = node.get("url").asText();
            JsonNode exampleNode = FileOperations.getContentAsJson(exampleUrl);

            if (exampleNode == null)
                continue;

            JsonNode exampleReadMeNode = exampleNode.get(0);
            //JsonNode exampleReadMeNode = mapper.createObjectNode();
            JsonNode readMeNode = FileOperations.getContentAsJson(exampleReadMeNode.get("url").asText());

            if (readMeNode == null)
                continue;

            String contentBase64 = readMeNode.get("content").asText();
            String[] split = contentBase64.split("\n");

            String content = "";
            for (String s : split) {
                byte[] decode = Base64.getDecoder().decode(s);
                content += new String(decode);
                //content += StringUtils.newStringUtf8(decode);
            }

            ObjectNode projectExampleNode = mapper.createObjectNode();
            projectExampleNode.put("name", node.get("name").asText());
            projectExampleNode.put("url", exampleUrl);
            projectExampleNode.put("description", content);

//            HashMap<String, String> map = extractKeywords(content, new String[]{"AUTHOR=", "ENTRY_POINT="});
//            Iterator it = map.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry)it.next();
//                projectExampleNode.put(pair.getKey().toString(), pair.getValue().toString());
//                it.remove(); // avoids a ConcurrentModificationException
//            }

            jsonArray.add(projectExampleNode);
        }

        return ok(jsonArray);
    }

    private HashMap<String, List<String>> extractKeywords(String content, String[] keywords) {
        HashMap<String, List<String>> map = new HashMap<>();
        int start;
        int end;
        int scan;

        for (String keyword : keywords) {
            int matches = StringUtils.countMatches(content, keyword);
            scan = 0;
            List<String> values = new ArrayList<>();
            String key = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, keyword.toUpperCase().replaceAll(" ", "_"));
            while (matches > 0) {
                start = content.indexOf(keyword, scan) + keyword.length();
                end = content.indexOf("\n", start);
                String value = content.substring(start, end).replaceAll(":", "").trim();
                values.add(value);
                matches = matches - 1;
                scan = end;
            }
            map.put(key, values);
        }
        return map;
    }

    public Result getFromLocalRepository(String projectName) {
        String accessToken = ServerUtils.extractAccessToken(request());
        String userId = SessionStore.getInstance().get(accessToken);

        // TODO : Remember to remove!
        //userId = userId == null ? "111425625270532893915" : userId;

        try {
            FileUtils.copyDirectory(
                    Paths.get("OvertureExamples", "VDMSL", projectName).toFile(),
                    Paths.get(ServerConfigurations.basePath, userId, projectName).toFile()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok();
    }

    public Result listFromLocalRepository() {
        File[] repository = new File(Paths.get("OvertureExamples", "VDMSL").toString())
                .listFiles((dir, name) -> !name.startsWith("."));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (File file : repository) {
            FileOperations.filterDirectoryContent(file, new String[]{"vdmsl", "txt"});
            File readme = file.listFiles((dir, name) -> name.equals("README.txt"))[0];
            String content = FileOperations.readFileContent(readme);

            ObjectNode projectExampleNode = mapper.createObjectNode();
            projectExampleNode.put("name", file.getName());
            projectExampleNode.put("description", content);

            HashMap<String, List<String>> map = extractKeywords(content, new String[]{"Author", "Entry point"});
            Set<Map.Entry<String, List<String>>> entries = map.entrySet();
            Iterator<Map.Entry<String, List<String>>> it = entries.iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<String>> next = it.next();
                ArrayNode valuesNode = mapper.createArrayNode();
                for (String value : next.getValue())
                    valuesNode.add(value);

                projectExampleNode.put(next.getKey(), valuesNode);
                it.remove(); // avoids a ConcurrentModificationException
            }

            arrayNode.add(projectExampleNode);
        }

        return ok(arrayNode);
    }
}
