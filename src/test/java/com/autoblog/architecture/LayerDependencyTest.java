package com.autoblog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.autoblog",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class LayerDependencyTest {

    @ArchTest
    static final ArchRule domainHasNoFrameworkOrOuterLayerDependencies = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.autoblog..api..",
                    "com.autoblog..application..",
                    "com.autoblog..infrastructure..",
                    "org.springframework..",
                    "jakarta.persistence.."
            );

    @ArchTest
    static final ArchRule apiDoesNotReachIntoPersistence = noClasses()
            .that().resideInAPackage("com.autoblog..api..")
            .should().dependOnClassesThat().resideInAPackage("com.autoblog..infrastructure..");

    @ArchTest
    static final ArchRule persistenceDoesNotDependOnApi = noClasses()
            .that().resideInAPackage("com.autoblog..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("com.autoblog..api..");
}
