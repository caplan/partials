package com.nytimes.tools.partials.processor;

import com.squareup.javapoet.ClassName;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.union;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;

public class SequencedNamer implements Namer {

    private final String prefix;
    private final Set<MetaParam> allParams;
    private final ClassName containerName;

    public SequencedNamer(String prefix,
                          ClassName containerName,
                          Set<MetaParam> allParams) {
        this.prefix = prefix;
        this.allParams = allParams;
        this.containerName = containerName;
    }

    @Override
    public String get(Set<MetaParam> params) {

        if (params.isEmpty()) {
            return prefix;
        }

        return prefix + parseInt(allParams.stream()
                                          .sorted()
                                          .map(p -> params.contains(p) ? 1 : 0)
                                          .map(i -> Integer.toString(i))
                                          .collect(joining()),
                                 2);
    }

    @Override
    public String get() {
        return get(emptySet());
    }

    @Override
    public ClassName name(Set<MetaParam> params) {
        return containerName.nestedClass(get(params));
    }

    @Override
    public ClassName name() {
        return name(emptySet());
    }

    @Override
    public String get(Set<MetaParam> params, MetaParam extra) {
        return get(union(params, newHashSet(extra)));
    }

    @Override
    public ClassName name(Set<MetaParam> params, MetaParam extra) {
        return containerName.nestedClass(get(params, extra));
    }
}
