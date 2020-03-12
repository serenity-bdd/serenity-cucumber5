package net.serenitybdd.cucumber.outcomes

import net.serenitybdd.cucumber.integration.*
import net.thucydides.core.model.TestOutcome
import net.thucydides.core.model.TestResult
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static io.cucumber.junit.CucumberRunner.serenityRunnerForCucumberTestRunner
import static net.thucydides.core.model.TestResult.*

class WhenWorkingWithTaggedExampleTables extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    File outputDirectory

    def setup() {
        outputDirectory = temporaryFolder.newFolder()
    }

    /*
          Scenario Outline: Buying lots of widgets
            Given I want to purchase <amount> widgets
            And a widget costs $<cost>
            When I buy the widgets
            Then I should be billed $<total>
          Examples:
          | amount | cost | total |
          | 0      | 10   | 0     |
          | 1      | 10   | 10    |
          | 2      | 10   | 20    |
          | 2      | 0    | 0     |
     */
    def "should run table-driven scenarios with tagged example tables"() {
        given:
            def runtime = serenityRunnerForCucumberTestRunner(TaggedExampleTablesScenarios.class, outputDirectory);

        when:
            runtime.run();
            def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);

        then: "there should a test outcome for each scenario"
            recordedTestOutcomes.size() == 1
        and: "line numbers should be recorded for each executed example"
            def testOutcome = recordedTestOutcomes[0]
            testOutcome.testSteps[0].lineNumber == 12 && testOutcome.testSteps[1].lineNumber == 13
    }


}