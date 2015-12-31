package core.codecompletion.proposal.mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import core.codecompletion.resources.Document;
import core.codecompletion.resources.ICompletionProposal;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class ProposalToJsonMapper {
    public List<ObjectNode> toJson(List<ICompletionProposal> proposals, Document document) {
        List<ObjectNode> jsonList = new ArrayList<>();

        for (ICompletionProposal proposal : proposals) {
            jsonList.add(toJson(proposal, document));
        }

        return jsonList;
    }

    public ObjectNode toJson(ICompletionProposal proposal, Document document) {
        int offset = proposal.getReplacementOffset();
        int line = document.getLine(offset);
        int column = document.getColumn(offset);

        ObjectNode node = Json.newObject();
        node.put("replacementString", proposal.getReplacementString());
        node.put("replacementOffset", offset);
        node.put("replacementLine", line);
        node.put("replacementColumn", column);
        node.put("replacementLength", proposal.getReplacementLength());
        node.put("cursorPosition", proposal.getCursorPosition());

        return node;
    }
}
