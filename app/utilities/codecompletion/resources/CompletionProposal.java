package utilities.codecompletion.resources;

public class CompletionProposal implements ICompletionProposal {
    private String replacementString;
    private int replacementOffset;
    private int replacementLength;
    private int cursorPosition;

    /*
     * Parameters:
     *
     * replacementString - the actual string to be inserted into the document
     * replacementOffset - the offset of the text to be replaced
     * replacementLength - the length of the text to be replaced
     * cursorPosition - the position of the cursor following the insert relative to replacementOffset
     */
    public CompletionProposal(String repString, int repOffset, int repLength, int curPosition) {
        replacementString = repString;
        replacementOffset = repOffset;
        replacementLength = repLength;
        cursorPosition = curPosition;
    }

    @Override
    public String getReplacementString() {
        return replacementString;
    }

    @Override
    public void setReplacementString(String replacementString) {
        this.replacementString = replacementString;
    }

    @Override
    public int getReplacementOffset() {
        return replacementOffset;
    }

    @Override
    public void setReplacementOffset(int replacementOffset) {
        this.replacementOffset = replacementOffset;
    }

    @Override
    public int getReplacementLength() {
        return replacementLength;
    }

    @Override
    public void setReplacementLength(int replacementLength) {
        this.replacementLength = replacementLength;
    }

    @Override
    public int getCursorPosition() {
        return cursorPosition;
    }

    @Override
    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }
}
