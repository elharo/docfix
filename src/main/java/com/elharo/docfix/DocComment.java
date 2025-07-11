package com.elharo.docfix;

import java.util.List;

/**
 * Represents a Javadoc comment, including its kind, description, and block tags.
 */
class DocComment {
    enum Kind { CLASS, METHOD, FIELD }
    final Kind kind;
    final String description; // Main description (before block tags)
    final List<BlockTag> blockTags;

    DocComment(Kind kind, String description, List<BlockTag> blockTags) {
        this.kind = kind;
        this.description = description;
        this.blockTags = blockTags;
    }
}
