package com.hotelbooking;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/** Keeps the module and layer boundaries described in the README from eroding. */
class ArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.hotelbooking");

    @Test
    void domainModelIsPlainJava() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "jakarta.validation..",
                        "com.fasterxml.jackson..", "tools.jackson..", "io.swagger..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void dependenciesPointInwardTowardsTheDomain() {
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..api..", "..infrastructure..")
                .check(PRODUCTION_CLASSES);
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void featureModulesHaveNoDependencyCycles() {
        slices().matching("com.hotelbooking.(*)..").should().beFreeOfCycles().check(PRODUCTION_CLASSES);
    }

    @Test
    void persistenceModelIsHiddenInsideInfrastructure() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..infrastructure.persistence..")
                .andShould().notBePublic()
                .check(PRODUCTION_CLASSES);
        noClasses().that().resideOutsideOfPackage("..infrastructure.persistence..")
                .should().dependOnClassesThat().areAssignableTo(Repository.class)
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void restControllersLiveInTheApiLayer() {
        classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..api..")
                .check(PRODUCTION_CLASSES);
    }
}
