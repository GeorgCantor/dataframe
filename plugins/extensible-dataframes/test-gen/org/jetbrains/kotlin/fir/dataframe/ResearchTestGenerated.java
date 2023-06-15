

package org.jetbrains.kotlin.fir.dataframe;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link GenerateNewCompilerTests.kt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("testData/research")
@TestDataPath("$PROJECT_ROOT")
public class ResearchTestGenerated extends AbstractResearchTest {
    @Test
    public void testAllFilesPresentInResearch() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/research"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("calls.kt")
    public void testCalls() throws Exception {
        runTest("testData/research/calls.kt");
    }
}