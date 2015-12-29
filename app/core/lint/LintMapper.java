package core.lint;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.overture.parser.messages.VDMMessage;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class LintMapper {
    public <T extends VDMMessage> List<ObjectNode> messagesToJson(List<T> messages) {
        List<ObjectNode> jsonList = new ArrayList<>();

        for (VDMMessage message : messages) {
            jsonList.add(mapObject(message));
        }

        return jsonList;
    }

    private <T extends VDMMessage> ObjectNode mapObject(T object) {
        ObjectNode node = Json.newObject();

        node.put("number", object.number);
        node.put("message", object.message);

        int startLine = object.location.getStartLine();
        int endLine = object.location.getEndLine();
        int startOffset = object.location.getStartOffset();
        int endOffset = object.location.getEndOffset();
        int startPos = object.location.getStartPos();
        int endPos = object.location.getEndPos();
        long hits = object.location.getHits();

        ObjectNode locationNode = Json.newObject();
        locationNode.put("startLine", startLine);
        locationNode.put("endLine", endLine);
        locationNode.put("startOffset", startOffset);
        locationNode.put("endOffset", endOffset);
        locationNode.put("startPos", startPos);
        locationNode.put("endPos", endPos);
        locationNode.put("hits", hits);

        node.putPOJO("location", locationNode);

        return node;
    }
}
