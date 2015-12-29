package core.codecompletion.resources;

public interface ICompletionProposal {
    String getReplacementString();

    void setReplacementString(String replacementString);

    int getReplacementOffset();

    void setReplacementOffset(int replacementOffset);

    int getReplacementLength();

    void setReplacementLength(int replacementLength);

    int getCursorPosition();

    void setCursorPosition(int cursorPosition);
}
