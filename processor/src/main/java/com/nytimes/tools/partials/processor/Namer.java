package com.nytimes.tools.partials.processor;

import com.squareup.javapoet.ClassName;

import java.util.Set;

public interface Namer {
    String get(Set<MetaParam> params);
    String get();

    ClassName name(Set<MetaParam> params);
    ClassName name();

    String get(Set<MetaParam> partialSet, MetaParam extra);
    ClassName name(Set<MetaParam> params, MetaParam extra);

}
