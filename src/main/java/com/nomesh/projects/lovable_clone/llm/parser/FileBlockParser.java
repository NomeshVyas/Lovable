package com.nomesh.projects.lovable_clone.llm.parser;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class FileBlockParser {

    final ParsedOutputListener parsedOutputListener;
    final StringBuilder unparsedText = new StringBuilder();
    ParseState state = ParseState.SCANNING;
    String currentFilePath;

    private static final String FILE_TAG_START = "<file path=\"";
    private static final String FILE_TAG_END = "</file>";
    private static final int MAX_PARTIAL_TAG = FILE_TAG_START.length() - 1;

    public void parse(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        unparsedText.append(chunk);
        boolean progressed = true;
        while (progressed) {
            progressed = (state == ParseState.SCANNING)
                    ? scanForFileStart()
                    : readUntilFileEnd();
        }
    }

    public void finish() {
        if (state == ParseState.READING_FILE) {
            unparsedText.setLength(0);
            String incompletePath = currentFilePath;
            currentFilePath = null;
            state = ParseState.SCANNING;

            parsedOutputListener.onIncompleteFile(incompletePath);
            return;
        }

        emitMessageText(unparsedText.length());
    }

    private boolean scanForFileStart() {
        int tagStart = unparsedText.indexOf(FILE_TAG_START);

        if (tagStart < 0) {
            emitMessageText(Math.max(0, unparsedText.length() - MAX_PARTIAL_TAG));
            return false;
        }

        int pathStart = tagStart + FILE_TAG_START.length();
        int pathEnd = unparsedText.indexOf("\"", pathStart);
        if (pathEnd < 0) {
            emitMessageText(tagStart);
            return false;
        }

        int tagEnd = unparsedText.indexOf(">", pathEnd);
        if (tagEnd < 0) {
            emitMessageText(tagStart);
            return false;
        }

        currentFilePath = unparsedText.substring(pathStart, pathEnd);
        String messageText = unparsedText.substring(0, tagStart);

        unparsedText.delete(0, tagEnd + 1);
        state = ParseState.READING_FILE;

        if (!messageText.isEmpty())
            parsedOutputListener.onMessageText(messageText);

        return true;
    }

    private boolean readUntilFileEnd() {
        int endTag = unparsedText.indexOf(FILE_TAG_END);
        if (endTag < 0) return false;

        String fileContent = unparsedText.substring(0, endTag).trim();
        String filePath = currentFilePath;

        unparsedText.delete(0, endTag + FILE_TAG_END.length());
        currentFilePath = null;
        state = ParseState.SCANNING;

        parsedOutputListener.onFileCompleted(filePath, fileContent);
        return true;
    }

    private void emitMessageText(int upTo) {
        if (upTo <= 0) return;

        String text = unparsedText.substring(0, upTo);
        unparsedText.delete(0, upTo);
        parsedOutputListener.onMessageText(text);
    }
}
