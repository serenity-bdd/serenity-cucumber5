package net.serenitybdd.cucumber.util;

import net.serenitybdd.cucumber.suiteslicing.ScenarioFilter;
import net.thucydides.core.annotations.Screenshots;
import net.thucydides.core.model.TakeScreenshots;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;

public class StepDefinitionAnnotationReader {
    private String stepDefinitionPath;

    private static final Logger LOGGER = LoggerFactory.getLogger(StepDefinitionAnnotationReader.class);

    public StepDefinitionAnnotationReader(String stepDefinitionPath) {
        this.stepDefinitionPath = stepDefinitionPath;
    }

    public static StepDefinitionAnnotationReader forStepDefinition(String stepDefinitionPath) {
        return new StepDefinitionAnnotationReader(stepDefinitionPath);
    }

    public TakeScreenshots getScreenshotPreferences() {
        if (stepDefinitionPath == null) { return TakeScreenshots.UNDEFINED; }
        List<Annotation> stepDefinitionAnnotations = annotationsIn(className(), methodName());
        return stepDefinitionAnnotations.stream()
                .filter(annotation -> annotation instanceof Screenshots)
                .map(annotation -> asEnum((Screenshots) annotation))
                .findFirst()
                .orElse(TakeScreenshots.UNDEFINED);
    }

    private TakeScreenshots asEnum(Screenshots screenshotAnnotation) {
        if (screenshotAnnotation.disabled()) {
            return TakeScreenshots.DISABLED;
        } else if (screenshotAnnotation.afterEachStep()) {
            return TakeScreenshots.AFTER_EACH_STEP;
        } else if (screenshotAnnotation.beforeAndAfterEachStep()) {
            return TakeScreenshots.BEFORE_AND_AFTER_EACH_STEP;
        } else if (screenshotAnnotation.forEachAction()) {
            return TakeScreenshots.FOR_EACH_ACTION;
        } else if (screenshotAnnotation.onlyOnFailures()) {
            return TakeScreenshots.FOR_FAILURES;
        } else {
            return TakeScreenshots.UNDEFINED;
        }
    }

    private String className() {
        stepDefinitionPath=stepDefinitionPath.replaceFirst("^([^\\s]*)(\\s)","");
        int lastOpeningParentheses = stepDefinitionPath.lastIndexOf("(");
        String qualifiedMethodName = stepDefinitionPath.substring(0, lastOpeningParentheses);
        int endOfClassName = qualifiedMethodName.lastIndexOf(".");
        return stepDefinitionPath.substring(0, endOfClassName);
    }

    private String methodName() {
        int lastOpeningParentheses = stepDefinitionPath.lastIndexOf("(");
        String qualifiedMethodName = stepDefinitionPath.substring(0, lastOpeningParentheses);
        int startOfMethodName = qualifiedMethodName.lastIndexOf(".") + 1;
        return stepDefinitionPath.substring(startOfMethodName, lastOpeningParentheses);
    }

    private List<Annotation> annotationsIn(String className, String methodName) {
        try {
            Optional<Method> matchingMethod
                    = stream(Class.forName(className).getMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .findFirst();

            return matchingMethod
                    .map(method -> Arrays.asList(method.getAnnotations()))
                    .orElseGet(ArrayList::new);

        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not analyse step definition method " + className + "." + methodName);
        }
        return new ArrayList<>();
    }
}
