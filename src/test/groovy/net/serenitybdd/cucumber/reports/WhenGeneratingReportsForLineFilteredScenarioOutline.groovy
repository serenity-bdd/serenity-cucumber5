package net.serenitybdd.cucumber.reports

import io.cucumber.junit.CucumberRunner
import io.cucumber.plugin.event.Status
import net.serenitybdd.cucumber.integration.SimpleTableScenarioWithLineFilters
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class WhenGeneratingReportsForLineFilteredScenarioOutline extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    File outputDirectory

    def setup() {
        outputDirectory = temporaryFolder.newFolder()
    }

    def "should generate a Thucydides report for each executed Cucumber scenario"() {
        given:
        def runtime = CucumberRunner.serenityRunnerForCucumberTestRunner(SimpleTableScenarioWithLineFilters.class, outputDirectory)

        when:
        runtime.run()
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory)

        then:
        runtime.exitStatus.results[0].getStatus().is(Status.PASSED)

        and:
        !recordedTestOutcomes.isEmpty()
    }


}