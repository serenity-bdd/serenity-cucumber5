package net.serenitybdd.cucumber.suiteslicing;

import com.google.common.collect.FluentIterable;
import io.cucumber.core.internal.gherkin.ast.*;
import io.cucumber.core.plugin.FeatureFileLoader;
import net.serenitybdd.cucumber.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Reads cucumber feature files and breaks them down into a collection of scenarios (WeightedCucumberScenarios).
 */
public class CucumberScenarioLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberScenarioLoader.class);
    private final List<URI> featurePaths;
    private final TestStatistics statistics;
    private Map<Feature, URI> mapsForFeatures = new HashMap<>();

    public CucumberScenarioLoader(List<URI> featurePaths, TestStatistics statistics) {
        this.featurePaths = featurePaths;
        this.statistics = statistics;
    }

    public WeightedCucumberScenarios load() {
        LOGGER.debug("Feature paths are {}", featurePaths);
        List<WeightedCucumberScenario> weightedCucumberScenarios =
                featurePaths.stream().map(featurePath->new FeatureFileLoader().getFeature(featurePath))
            .map(getScenarios())
            .flatMap(List::stream)
            .collect(toList());
        featurePaths.stream().collect(Collectors.toMap(f->f, featurePath->new FeatureFileLoader().getFeature(featurePath)));
        return new WeightedCucumberScenarios(weightedCucumberScenarios);
    }

    private Function<Feature, List<WeightedCucumberScenario>> getScenarios() {
        return cucumberFeature -> {
            try {
                return (cucumberFeature == null) ? Collections.emptyList() : cucumberFeature.getChildren()
                    .stream()
                    .filter(child -> asList(ScenarioOutline.class, Scenario.class).contains(child.getClass()))
                    .map(scenarioDefinition -> new WeightedCucumberScenario(
                        PathUtils.getAsFile(mapsForFeatures.get(cucumberFeature)).getName(),
                        cucumberFeature.getName(),
                        scenarioDefinition.getName(),
                        scenarioWeightFor(cucumberFeature, scenarioDefinition),
                        tagsFor(cucumberFeature, scenarioDefinition),
                        scenarioCountFor(scenarioDefinition)))
                    .collect(toList());
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Could not extract scenarios from %s", mapsForFeatures.get(cucumberFeature)), e);
            }
        };
    }

    private int scenarioCountFor(ScenarioDefinition scenarioDefinition) {
        if (scenarioDefinition instanceof ScenarioOutline) {
            return ((ScenarioOutline) scenarioDefinition).getExamples().stream().map(examples -> examples.getTableBody().size()).mapToInt(Integer::intValue).sum();
        } else {
            return 1;
        }
    }

    private Set<String> tagsFor(Feature feature, ScenarioDefinition scenarioDefinition) {
        return FluentIterable.concat(feature.getTags(), scenarioTags(scenarioDefinition)).stream().map(Tag::getName).collect(toSet());
    }

    private List<Tag> scenarioTags(ScenarioDefinition scenario) {
        if (Scenario.class.isAssignableFrom(scenario.getClass())) {
            return ((Scenario) scenario).getTags();
        } else {
            return ((ScenarioOutline) scenario).getTags();
        }
    }

    private BigDecimal scenarioWeightFor(Feature feature, ScenarioDefinition scenarioDefinition) {
        return statistics.scenarioWeightFor(feature.getName(), scenarioDefinition.getName());
    }

}
