package com.nomesh.projects.lovable_clone.llm.parser;

public interface ParsedOutputListener {

    void onFileCompleted(String filePath, String fileContent);
    void onMessageText(String text);
    void onIncompleteFile(String filePath);
}
