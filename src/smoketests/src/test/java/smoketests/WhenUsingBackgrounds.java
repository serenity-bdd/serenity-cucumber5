package smoketests;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/features/backgrounds")
public class WhenUsingBackgrounds {}
