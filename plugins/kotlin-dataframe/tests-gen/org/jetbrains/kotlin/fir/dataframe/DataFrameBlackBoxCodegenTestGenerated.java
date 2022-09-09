

package org.jetbrains.kotlin.fir.dataframe;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link GenerateNewCompilerTests.kt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("testData/box")
@TestDataPath("$PROJECT_ROOT")
public class DataFrameBlackBoxCodegenTestGenerated extends AbstractDataFrameBlackBoxCodegenTest {
    @Test
    public void testAllFilesPresentInBox() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/box"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JVM_IR, true);
    }

    @Test
    @TestMetadata("extensionPropertiesIrExample.kt")
    public void testExtensionPropertiesIrExample() throws Exception {
        runTest("testData/box/extensionPropertiesIrExample.kt");
    }

    @Test
    @TestMetadata("injectAlgebra.kt")
    public void testInjectAlgebra() throws Exception {
        runTest("testData/box/injectAlgebra.kt");
    }

    @Test
    @TestMetadata("lowerGeneratedImplicitReceiver.kt")
    public void testLowerGeneratedImplicitReceiver() throws Exception {
        runTest("testData/box/lowerGeneratedImplicitReceiver.kt");
    }

    @Test
    @TestMetadata("lowerManualImplicitReceiver.kt")
    public void testLowerManualImplicitReceiver() throws Exception {
        runTest("testData/box/lowerManualImplicitReceiver.kt");
    }

    @Test
    @TestMetadata("OuterClass.kt")
    public void testOuterClass() throws Exception {
        runTest("testData/box/OuterClass.kt");
    }

    @Test
    @TestMetadata("test.kt")
    public void testTest() throws Exception {
        runTest("testData/box/test.kt");
    }
}
