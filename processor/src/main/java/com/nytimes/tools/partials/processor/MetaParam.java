package com.nytimes.tools.partials.processor;

import com.google.common.base.Strings;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.Objects;

public class MetaParam implements Comparable<MetaParam> {

    static final String SET = "Set";
    static final String GET = "Get";

    final TypeName typeName;
    final Name name;
    final int order;

    public MetaParam(VariableElement element, int order) {
        this.typeName = TypeName.get(element.asType());
        this.name = element.getSimpleName();
        this.order = order;
    }

    public ParameterSpec toSpec(boolean boxed) {
        return ParameterSpec.builder(boxed ? typeName.box() : typeName, name.toString()).build();
    }

    public String setter() {
        return SET + upperCaseFirst(name.toString());
    }

    public String getter() {
        return GET + upperCaseFirst(name.toString());
    }

    private static String upperCaseFirst(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public ParameterSpec toSpec() {
        return toSpec(false);
    }

    public ParameterSpec toBoxedSpec() {
        return toSpec(true);
    }

    @Override
    public int compareTo(@NotNull MetaParam other) {
        return Integer.compare(order, other.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetaParam)) {
            return false;
        }
        MetaParam metaParam = (MetaParam) o;
        return Objects.equals(typeName, metaParam.typeName) &&
                Objects.equals(name, metaParam.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, name);
    }

}
