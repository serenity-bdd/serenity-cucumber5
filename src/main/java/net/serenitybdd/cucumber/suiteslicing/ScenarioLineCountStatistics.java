package net.serenitybdd.cucumber.suiteslicing;

import io.cucumber.core.internal.gherkin.ast.*;
import io.cucumber.core.plugin.FeatureFileLoader;
import net.thucydides.core.util.Inflector;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class ScenarioLineCountStatistics implements TestStatistics {

    private final List<URI> featurePaths;
    private final List<TestScenarioResult> results;

    private ScenarioLineCountStatistics(List<URI> featurePaths) {
        this.featurePaths = featurePaths;
        this.results = featurePaths.stream().map(featurePath->new FeatureFileLoader().getFeature(featurePath))
            .map(featureToScenarios())
            .flatMap(List::stream)
            .collect(toList());
    }

    public static ScenarioLineCountStatistics fromFeaturePath(URI featurePaths) {
        return fromFeaturePaths(asList(featurePaths));
    }

    public static ScenarioLineCountStatistics fromFeaturePaths(List<URI> featurePaths) {
        return new ScenarioLineCountStatistics(featurePaths);
    }

    private Function<Feature, List<TestScenarioResult>> featureToScenarios() {
        return cucumberFeature -> {
            try {
                return (cucumberFeature == null) ? Collections.emptyList() : cucumberFeature.getChildren()
                    .stream()
                    .filter(child -> asList(ScenarioOutline.class, Scenario.class).contains(child.getClass()))
                    .map(scenarioToResult(cucumberFeature))
                    .collect(toList());
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Could not extract scenarios from %s", cucumberFeature.getName()), e);
            }
        };
    }

    private Function<ScenarioDefinition, TestScenarioResult> scenarioToResult(Feature feature) {
        return scenarioDefinition -> {
            try {
                return new TestScenarioResult(
                    feature.getName(),
                    scenarioDefinition.getName(),
                    scenarioStepCountFor(backgroundStepCountFor(feature), scenarioDefinition));
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Could not determine step count for scenario '%s'", scenarioDefinition.getDescription()), e);
            }
        };
    }

    private BigDecimal scenarioStepCountFor(int backgroundStepCount, ScenarioDefinition scenarioDefinition) {
        final int stepCount;
        if (scenarioDefinition instanceof ScenarioOutline) {
            ScenarioOutline outline = (ScenarioOutline) scenarioDefinition;
            Integer exampleCount = outline.getExamples().stream().map(examples -> examples.getTableBody().size()).mapToInt(Integer::intValue).sum();
            stepCount = exampleCount * (backgroundStepCount + outline.getSteps().size());
        } else {
            stepCount = backgroundStepCount + scenarioDefinition.getSteps().size();
        }
        return BigDecimal.valueOf(stepCount);
    }

    private int backgroundStepCountFor(Feature feature) {
        ScenarioDefinition scenarioDefinition = feature.getChildren().get(0);
        if (scenarioDefinition instanceof Background) {
            return scenarioDefinition.getSteps().size();
        } else {
            return 0;
        }
    }

    @Override
    public BigDecimal scenarioWeightFor(String feature, String scenario) {
        return results.stream()
            .filter(record -> record.feature.equals(feature) && record.scenario.equals(scenario))
            .map(TestScenarioResult::duration)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("no result found for scenario '%s' in feature '%s'", scenario, feature)));
    }

    @Override
    public List<TestScenarioResult> records() {
        return results;
    }

    public String toString() {
        return Inflector.getInstance().kebabCase(this.getClass().getSimpleName());
    }
}
