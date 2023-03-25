package dev.lukebemish.modularmetals.util

import groovy.transform.CompileStatic

@CompileStatic
class InlineGroovyAdaptedInputStream extends InputStream {
    private final InputStream wrapped
    private boolean inner = false
    private int buffered = -1
    private int[] buffer = new int[]{}
    private int charBuffer = 0
    private boolean inSingle = false
    private boolean inDouble = false
    private boolean escaped = false
    private boolean triggerInner = false

    private static final char ESCAPE = '\\'
    private static final char TAB = '\t'
    private static final char TAB_CHAR = 't'
    private static final char NEWLINE = '\n'
    private static final char NEWLINE_CHAR = 'n'
    private static final char CARRIAGE_RETURN = '\r'
    private static final char CARRIAGE_RETURN_CHAR = 'r'
    private static final char QUOTE = '"'
    private static final char SINGLE = '\''
    private static final char FENCE = '`'
    private static final int[] END = '"}'.chars().toArray()
    private static final int[] START = '{__value__:"'.chars().toArray()

    InlineGroovyAdaptedInputStream(InputStream wrapped) {
        this.wrapped = wrapped
    }

    int charTransform(int c) {
        if (c === TAB) {
            charBuffer = (int) TAB_CHAR
            return (int) ESCAPE
        } else if (c === NEWLINE) {
            charBuffer = (int) NEWLINE_CHAR
            return (int) ESCAPE
        } else if (c === CARRIAGE_RETURN) {
            charBuffer = (int) CARRIAGE_RETURN_CHAR
            return (int) ESCAPE
        } else if (c === QUOTE) {
            charBuffer = (int) QUOTE
            return (int) ESCAPE
        } else if (c === ESCAPE) {
            charBuffer = (int) ESCAPE
            return (int) ESCAPE
        } else {
            return c
        }
    }

    @Override
    int read() throws IOException {
        if (charBuffer != 0) {
            int result = charBuffer
            charBuffer = 0
            return result
        }
        if (buffered >= 0) {
            int c = buffer[buffer.length - buffered - 1]
            buffered -= 1
            c = inner ? charTransform(c) : c
            if (triggerInner && buffered < 0) {
                triggerInner = false
                inner = true
            }
            return c
        }
        int val = wrapped.read()
        if (val === -1) {
            return -1
        }
        char result = (char) val
        if (inSingle || inDouble) {
            if (escaped) {
                escaped = false
                return (int) result
            }
            if (result === ESCAPE) {
                escaped = true
                return (int) result
            }
            if (inSingle && result === SINGLE) {
                inSingle = false
                return (int) result
            }
            if (inDouble && result === QUOTE) {
                inDouble = false
                return (int) result
            }
        }
        if (inner) {
            if (result === FENCE) {
                int next = wrapped.read()
                if (next === FENCE) {
                    int next2 = wrapped.read()
                    if (next2 === FENCE) {
                        buffer = END
                        buffered = END.length - 1
                        inner = false
                        return read()
                    } else {
                        buffered = 1
                        buffer = new int[]{(int) FENCE, next2}
                        return charTransform((int) result)
                    }
                } else {
                    buffered = 0
                    buffer = new int[]{next}
                    return charTransform((int) result)
                }
            }
            return charTransform((int) result)
        }
        if (result === FENCE) {
            int next = wrapped.read()
            if (next === FENCE) {
                int next2 = wrapped.read()
                if (next2 === FENCE) {
                    buffer = START
                    buffered = START.length - 1
                    triggerInner = true
                    return read()
                } else {
                    buffered = 1
                    buffer = new int[]{(int) FENCE, next2}
                    return (int) result
                }
            } else {
                buffered = 0
                buffer = new int[]{next}
                return (int) result
            }
        }
        if (result === SINGLE) {
            inSingle = true
        }
        if (result === QUOTE) {
            inDouble = true
        }
        return (int) result
    }
}
