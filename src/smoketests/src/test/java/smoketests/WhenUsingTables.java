package smoketests;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/features/when_using_tables")
public class WhenUsingTables {}
