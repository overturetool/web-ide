package core.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.utilities.PathHelper;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.pog.pub.IProofObligation;
import org.overture.pog.pub.IProofObligationList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PogMapper {
    private IProofObligationList pog;

    public PogMapper(IProofObligationList pog) {
        this.pog = pog;
    }

    public List<ObjectNode> toJson() {
        if (this.pog != null)
            return this.pog.stream().map(this::mapObject).collect(Collectors.toList());
        else
            return new ArrayList<>();
    }

    private ObjectNode mapObject(IProofObligation po) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("rootNode", po.getNode().toString());
        node.put("name", po.getName());
        node.put("isaName", po.getIsaName());
        node.put("valuetree", po.getFullPredString());
        node.put("stitch", po.getDefPredString());
        node.put("status", po.getStatus().toString());
        node.put("kind", po.getKindString());
        node.put("number", po.getNumber());
        node.put("generator", po.getUniqueName());

        ObjectNode locationNode = mapper.createObjectNode();
        ILexLocation location = po.getLocation();

        locationNode.put("executable", location.getExecutable());
        locationNode.put("file", PathHelper.RemoveBase(location.getFile().getPath()));
        locationNode.put("module", location.getModule());
        locationNode.put("startLine", location.getStartLine());
        locationNode.put("endLine", location.getEndLine());
        locationNode.put("startOffset", location.getStartOffset());
        locationNode.put("endOffset", location.getEndOffset());
        locationNode.put("startPos", location.getStartPos());
        locationNode.put("endPos", location.getEndPos());
        locationNode.put("hits", location.getHits());

        node.putPOJO("location", locationNode);
        node.put("locale", po.getLocale());

        return node;
    }
}
