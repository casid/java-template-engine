package org.jusecase.kte;

@SuppressWarnings("unused") // Methods are called by generated templates
public interface TemplateOutput {
    void writeSafeContent(String value);

    default void writeUnsafeContent(String value) {
        writeSafeContent(value);
    }

    default void writeSafe(Object value) {
        if (value == null) {
            writeSafeContent("null");
        } else {
            writeSafeContent(value.toString());
        }
    }

    default void writeSafe(boolean value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(byte value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(short value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(int value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(long value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(float value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeSafe(double value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(Object value) {
        if (value == null) {
            writeSafeContent("null");
        } else {
            writeUnsafeContent(value.toString());
        }
    }

    default void writeUnsafe(boolean value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(byte value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(short value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(int value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(long value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(float value) {
        writeSafeContent(String.valueOf(value));
    }

    default void writeUnsafe(double value) {
        writeSafeContent(String.valueOf(value));
    }
}
