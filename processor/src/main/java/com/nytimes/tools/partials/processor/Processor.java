package com.nytimes.tools.partials.processor;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.nytimes.tools.partials.api.GeneratePartials;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(javax.annotation.processing.Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {

        List<ProcessingStep> steps = new ArrayList<>();

        steps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.<Class<? extends Annotation>>builder()
                                   .add(GeneratePartials.class)
                                   .build();
            }

            @Override
            public void process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

                for (Element element : elementsByAnnotation.values()) {

                    if (element.getModifiers().contains(Modifier.PRIVATE)) {
                        // TODO error
                        continue;
                    }


                    // TODO(caplan) this doesn't properly handle multiple @GeneratePartials annotations on the same element

                    GeneratePartials annotation = element.getAnnotation(GeneratePartials.class);


                    // can't extract Class from annotation when running in a processor; need to use *Mirrors

                    String declaredType =
                            Optional.fromNullable(
                                    Iterables.getFirst(
                                            Maps.filterKeys(
                                                    MoreElements.getAnnotationMirror(element, GeneratePartials.class)
                                                                .get()
                                                                .getElementValues(),
                                                    input -> input.getSimpleName()
                                                                  .contentEquals(GeneratePartials.DECLARED_TYPE_PARAMETER_NAME)
                                                           )
                                                .values(),
                                            null))
                                    .transform(value -> value.accept(new ClassAnnotationValueGetter(), null))
                                    .orNull();


                    try {
                        BuilderGenerator.generate(processingEnv.getFiler(),
                                                  (TypeElement)(element.getEnclosingElement()),
                                                  ((ExecutableElement)element).getParameters(),
                                                  declaredType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

            }
        });

        return steps;
    }
}
