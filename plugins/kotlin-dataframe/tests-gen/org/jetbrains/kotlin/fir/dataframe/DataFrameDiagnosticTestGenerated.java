

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
@TestMetadata("testData/diagnostics")
@TestDataPath("$PROJECT_ROOT")
public class DataFrameDiagnosticTestGenerated extends AbstractDataFrameDiagnosticTest {
    @Test
    @TestMetadata("A.kt")
    public void testA() throws Exception {
        runTest("testData/diagnostics/A.kt");
    }

    @Test
    public void testAllFilesPresentInDiagnostics() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/diagnostics"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("helloWorld.kt")
    public void testHelloWorld() throws Exception {
        runTest("testData/diagnostics/helloWorld.kt");
    }

    @Test
    @TestMetadata("injectAccessors.kt")
    public void testInjectAccessors() throws Exception {
        runTest("testData/diagnostics/injectAccessors.kt");
    }

    @Test
    @TestMetadata("injectAccessorsDsl.kt")
    public void testInjectAccessorsDsl() throws Exception {
        runTest("testData/diagnostics/injectAccessorsDsl.kt");
    }

    @Test
    @TestMetadata("insert.kt")
    public void testInsert() throws Exception {
        runTest("testData/diagnostics/insert.kt");
    }

    @Test
    @TestMetadata("OuterClass.kt")
    public void testOuterClass() throws Exception {
        runTest("testData/diagnostics/OuterClass.kt");
    }

    @Nested
    @TestMetadata("testData/diagnostics/schemaRender")
    @TestDataPath("$PROJECT_ROOT")
    public class SchemaRender {
        @Test
        public void testAllFilesPresentInSchemaRender() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/diagnostics/schemaRender"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("Schema1.kt")
        public void testSchema1() throws Exception {
            runTest("testData/diagnostics/schemaRender/Schema1.kt");
        }

        @Test
        @TestMetadata("Schema2.kt")
        public void testSchema2() throws Exception {
            runTest("testData/diagnostics/schemaRender/Schema2.kt");
        }
    }
}
