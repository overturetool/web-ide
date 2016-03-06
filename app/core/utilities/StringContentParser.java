package core.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

public class StringContentParser {
    public ObjectNode parseReadme(String content) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode entryPoints = mapper.createArrayNode();
        ObjectNode project = mapper.createObjectNode();

        String description = "";

        for (String line : content.split("\n")) {
            boolean containsColon = StringUtils.countMatches(line, ":") == 1;

            if (line.indexOf("Author") == 0 && containsColon)
                project.put("author", line.split(":")[1].trim());
            else if (line.indexOf("Language Version") == 0 && containsColon)
                project.put("languageVersion", line.split(":")[1].trim());
            else if (line.indexOf("Entry point") == 0 && containsColon)
                entryPoints.add(line.split(":")[1].trim());
            else
                description += line + "\n";
        }

        project.putPOJO("entryPoints", entryPoints);
        project.put("description", description.trim());

        return project;
    }
}
