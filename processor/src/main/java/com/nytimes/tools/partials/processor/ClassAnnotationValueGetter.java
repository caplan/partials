package com.nytimes.tools.partials.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor7;
import java.util.List;

public class ClassAnnotationValueGetter extends AbstractAnnotationValueVisitor7<String, Void> {

    @Override
    public String visitType(TypeMirror value, Void nothing) {
        return value.toString();
    }

    /* ************* unused stuff follows ************* */

    @Override
    public String visitBoolean(boolean value, Void nothing) {
        return null;
    }

    @Override
    public String visitByte(byte value, Void nothing) {
        return null;
    }

    @Override
    public String visitChar(char value, Void nothing) {
        return null;
    }

    @Override
    public String visitDouble(double value, Void nothing) {
        return null;
    }

    @Override
    public String visitFloat(float value, Void nothing) {
        return null;
    }

    @Override
    public String visitInt(int value, Void nothing) {
        return null;
    }

    @Override
    public String visitLong(long value, Void nothing) {
        return null;
    }

    @Override
    public String visitShort(short value, Void nothing) {
        return null;
    }

    @Override
    public String visitString(String value, Void nothing) {
        return null;
    }


    @Override
    public String visitEnumConstant(VariableElement value, Void nothing) {
        return null;
    }

    @Override
    public String visitAnnotation(AnnotationMirror value, Void nothing) {
        return null;
    }

    @Override
    public String visitArray(List<? extends AnnotationValue> value, Void nothing) {
        return null;
    }
}
