package com.elharo.docfix;

/**
 * Represents a Javadoc block tag (e.g., @param, @return, @throws, @deprecated, etc.).
 */
class BlockTag {
    final String type; // e.g., param, return, throws, deprecated
    final String argument; // e.g., parameter name for @param, exception type for @throws, null otherwise
    final String text; // The text of the tag

    BlockTag(String type, String argument, String text) {
        this.type = type;
        this.argument = argument;
        this.text = text;
    }
}
