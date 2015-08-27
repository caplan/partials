package com.nytimes.tools.partials.processor;

import com.google.common.collect.Sets;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.*;
import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.*;

class BuilderGenerator {

    private static final String buildMethodName = "build";
    private static final String applyMethodName = "apply";
    private static final String otherParamName = "other";

    private static final String partialBuilderName = "Builder";
    private static final String applierName = "Apply";

    private static final TypeVariableName TVA = TypeVariableName.get("T");
    private static final TypeVariableName TVB = TypeVariableName.get("R");

    static final String partialName = "Partial";

    private BuilderGenerator() {}


    static void generate(Filer filer,
                         TypeElement typeElement,
                         List<? extends VariableElement> elements,
                         String declaredType) throws IOException {

        AtomicInteger paramCounter = new AtomicInteger(1);
        SortedSet<MetaParam> params = new TreeSet<>(elements.stream()
                                                            .map(e -> new MetaParam(e, paramCounter.getAndIncrement()))
                                                            .collect(toSet()));

        // com.thingy.impl.ThingyImpl
        ClassName targetClass = ClassName.get(typeElement);

        // com.thingy.api.Thingy
        ClassName targetTypeClass = isNullOrEmpty(declaredType) ? targetClass : bestGuess(declaredType);

        // com.thingy.impl.ThingyBuilder
        ClassName containerName = targetClass.peerClass(targetTypeClass.simpleName() + partialBuilderName);

        Namer namer = new SequencedNamer(partialName, containerName, params);

        // build the container class
        TypeSpec.Builder container = classBuilder(containerName.simpleName())
                .addModifiers(PUBLIC, FINAL)
                .addMethod(methodBuilder("create")
                                   .returns(namer.name())
                                   .addModifiers(STATIC, PUBLIC)
                                   .addStatement("return null")
                                   .build());

        // getters
        container.addTypes(params.stream()
                                 .map(p -> interfaceBuilder(p.getter())
                                              .addModifiers(PUBLIC)
                                              .addMethod(methodBuilder(p.name.toString())
                                                                 .returns(p.typeName)
                                                                 .addModifiers(ABSTRACT, PUBLIC)
                                                                 .build())
                                              .build()
                                     )
                                 .collect(toList())
                          );


        // setters
        container.addTypes(params.stream()
                                 .map(p -> interfaceBuilder(p.setter())
                                              .addModifiers(PUBLIC)
                                              .addTypeVariable(TVB)
                                              .addMethod(methodBuilder(p.name.toString())
                                                                 .returns(TVB)
                                                                 .addModifiers(ABSTRACT, PUBLIC)
                                                                 .addParameter(p.toBoxedSpec())
                                                                 .build())
                                              .build()
                                     )
                                 .collect(toList())
                          );



        // apply
        container.addType(interfaceBuilder(applierName)
                                  .addModifiers(PUBLIC)
                                  .addTypeVariable(TVA)
                                  .addTypeVariable(TVB)
                                  .addMethod(methodBuilder(applyMethodName)
                                                     .returns(TVB)
                                                     .addParameter(builder(TVA, otherParamName).build())
                                                     .addModifiers(ABSTRACT, PUBLIC)
                                                     .build())
                                  .build()
                         );

        Set<Set<MetaParam>> powerSet = Sets.powerSet(params);
        for (Set<MetaParam> partialSet : powerSet) {

            TypeSpec.Builder builder = interfaceBuilder(namer.get(partialSet)).addModifiers(PUBLIC);

            // getter interfaces
            builder.addSuperinterfaces(partialSet.stream()
                                                 .sorted()
                                                 .map(p -> containerName.nestedClass(p.getter()))
                                                 .collect(toList()));

            // setter interfaces
            builder.addSuperinterfaces(difference(params, partialSet)
                                               .stream()
                                               .sorted()
                                               .map(p -> ParameterizedTypeName.get(
                                                            containerName.nestedClass(p.setter()),
                                                            namer.name(union(partialSet, newHashSet(p))))
                                                   )
                                               .collect(toList()));

            // applier methods

            for (Set<MetaParam> applierSet : powerSet) {
                // only apply X to Y if Y has at least 2 parameters that X doesn't have
                if (difference(partialSet, applierSet).size() > 2) {
                    // and also if Y has at least 2 params
                    if (applierSet.size() > 1) {

                        builder.addMethod(methodBuilder(applyMethodName)
                                                  .addParameter(
                                                          builder(namer.name(applierSet),
                                                                  otherParamName).build())
                                                  .addModifiers(ABSTRACT, PUBLIC)
                                                  .returns(namer.name(union(partialSet, applierSet)))
                                                  .build());

                    }
                }
            }


            // final target builder (if we're ready)
            if (partialSet.size() == params.size()) {
                builder.addMethod(
                        methodBuilder(buildMethodName)
                                .addModifiers(ABSTRACT, PUBLIC)
                                .returns(targetTypeClass)
                                .build());

            }



            container.addType(builder.build());

        } // each parameter set

        //     System.err.println(container.build().toString());

        JavaFile.builder(containerName.packageName(), container.build())
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);

    }

}
