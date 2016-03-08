package core.outline;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IAnalysis;
import org.overture.ast.analysis.intf.IAnswer;
import org.overture.ast.analysis.intf.IQuestion;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleImports;
import org.overture.ast.node.INode;

import java.util.List;
import java.util.Map;

public class ImportsContainer implements INode {
    private AModuleImports imports = null;
    private List<PDefinition> importDefs = null;

    public ImportsContainer(AModuleImports imports, List<PDefinition> importDefs){
        this.imports = imports;
        this.importDefs = importDefs;
    }

    public List<PDefinition> getImportDefs() {
        return this.importDefs;
    }

    public AModuleImports getImports() {
        return this.imports;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public INode clone(Map<INode, INode> oldToNewMap) {
        return null;
    }

    @Override
    public INode parent() {
        return null;
    }

    @Override
    public void parent(INode parent) {

    }

    @Override
    public void removeChild(INode child) {

    }

    @Override
    public void replaceChild(INode oldChild, INode newChild) {

    }

    @Override
    public <T extends INode> T getAncestor(Class<T> classType) {
        return null;
    }

    @Override
    public void apply(IAnalysis analysis) throws AnalysisException {

    }

    @Override
    public <A> A apply(IAnswer<A> caller) throws AnalysisException {
        return null;
    }

    @Override
    public <Q> void apply(IQuestion<Q> caller, Q question) throws AnalysisException {

    }

    @Override
    public <Q, A> A apply(IQuestionAnswer<Q, A> caller, Q question) throws AnalysisException {
        return null;
    }

    @Override
    public Map<String, Object> getChildren(Boolean includeInheritedFields) {
        return null;
    }
}
