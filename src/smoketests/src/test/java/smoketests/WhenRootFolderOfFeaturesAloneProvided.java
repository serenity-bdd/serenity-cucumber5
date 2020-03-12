package smoketests;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Created by Ramanathan Raghunathan on 18/12/2014.
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/features",
glue= {"smoketests.stepdefinitions"},tags = {"@tag_test"})
public class WhenRootFolderOfFeaturesAloneProvided {}
