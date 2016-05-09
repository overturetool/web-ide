package core.lint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.utilities.PathHelper;
import org.overture.parser.messages.VDMMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class LintMapper {
    private final Logger logger = LoggerFactory.getLogger(LintMapper.class);

    public <T extends VDMMessage> List<ObjectNode> messagesToJson(List<T> messages) {
        return messagesToJson(messages, null);
    }

    public synchronized  <T extends VDMMessage> List<ObjectNode> messagesToJson(List<T> messages, String targetModuleName) {
        List<ObjectNode> jsonList = new ArrayList<>();

        try {
            for (VDMMessage message : messages) {
                // TODO : wat?
                if (targetModuleName != null && message.location.getModule().equals(targetModuleName))
                    jsonList.add(mapObject(message));
                else if (targetModuleName == null)
                    jsonList.add(mapObject(message));
            }
        } catch (ConcurrentModificationException e) {
            logger.error(e.getMessage(), e);
        }

        return jsonList;
    }

    private <T extends VDMMessage> ObjectNode mapObject(T object) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("number", object.number);
        node.put("message", object.message);

        int startLine = object.location.getStartLine();
        int endLine = object.location.getEndLine();
        int startOffset = object.location.getStartOffset();
        int endOffset = object.location.getEndOffset();
        int startPos = object.location.getStartPos();
        int endPos = object.location.getEndPos();
        long hits = object.location.getHits();

        ObjectNode locationNode = mapper.createObjectNode();
        locationNode.put("executable", object.location.getExecutable());
        locationNode.put("file", PathHelper.RemoveBase(object.location.getFile().getPath()));
        locationNode.put("module", object.location.getModule());
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
