

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
@TestMetadata("testData/interpretation")
@TestDataPath("$PROJECT_ROOT")
public class DataFrameInterpretationTestsGenerated extends AbstractDataFrameInterpretationTests {
    @Test
    @TestMetadata("add.kt")
    public void testAdd() throws Exception {
        runTest("testData/interpretation/add.kt");
    }

    @Test
    public void testAllFilesPresentInInterpretation() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/interpretation"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Nested
    @TestMetadata("testData/interpretation/atoms")
    @TestDataPath("$PROJECT_ROOT")
    public class Atoms {
        @Test
        @TestMetadata("addExpression.kt")
        public void testAddExpression() throws Exception {
            runTest("testData/interpretation/atoms/addExpression.kt");
        }

        @Test
        public void testAllFilesPresentInAtoms() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/interpretation/atoms"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("columnsSelector.kt")
        public void testColumnsSelector() throws Exception {
            runTest("testData/interpretation/atoms/columnsSelector.kt");
        }

        @Test
        @TestMetadata("dataFrame.kt")
        public void testDataFrame() throws Exception {
            runTest("testData/interpretation/atoms/dataFrame.kt");
        }

        @Test
        @TestMetadata("enum.kt")
        public void testEnum() throws Exception {
            runTest("testData/interpretation/atoms/enum.kt");
        }

        @Test
        @TestMetadata("insertClause.kt")
        public void testInsertClause() throws Exception {
            runTest("testData/interpretation/atoms/insertClause.kt");
        }

        @Test
        @TestMetadata("interpretationError.kt")
        public void testInterpretationError() throws Exception {
            runTest("testData/interpretation/atoms/interpretationError.kt");
        }

        @Test
        @TestMetadata("KProperties.kt")
        public void testKProperties() throws Exception {
            runTest("testData/interpretation/atoms/KProperties.kt");
        }

        @Test
        @TestMetadata("kproperty.kt")
        public void testKproperty() throws Exception {
            runTest("testData/interpretation/atoms/kproperty.kt");
        }

        @Test
        @TestMetadata("memberFunction.kt")
        public void testMemberFunction() throws Exception {
            runTest("testData/interpretation/atoms/memberFunction.kt");
        }

        @Test
        @TestMetadata("rowValueExpression.kt")
        public void testRowValueExpression() throws Exception {
            runTest("testData/interpretation/atoms/rowValueExpression.kt");
        }

        @Test
        @TestMetadata("string.kt")
        public void testString() throws Exception {
            runTest("testData/interpretation/atoms/string.kt");
        }

        @Test
        @TestMetadata("type.kt")
        public void testType() throws Exception {
            runTest("testData/interpretation/atoms/type.kt");
        }

        @Test
        @TestMetadata("typeParameter.kt")
        public void testTypeParameter() throws Exception {
            runTest("testData/interpretation/atoms/typeParameter.kt");
        }
    }

    @Nested
    @TestMetadata("testData/interpretation/convert")
    @TestDataPath("$PROJECT_ROOT")
    public class Convert {
        @Test
        public void testAllFilesPresentInConvert() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/interpretation/convert"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("Convert0.kt")
        public void testConvert0() throws Exception {
            runTest("testData/interpretation/convert/Convert0.kt");
        }

        @Test
        @TestMetadata("Convert1.kt")
        public void testConvert1() throws Exception {
            runTest("testData/interpretation/convert/Convert1.kt");
        }

        @Test
        @TestMetadata("Convert2.kt")
        public void testConvert2() throws Exception {
            runTest("testData/interpretation/convert/Convert2.kt");
        }

        @Test
        @TestMetadata("Convert3.kt")
        public void testConvert3() throws Exception {
            runTest("testData/interpretation/convert/Convert3.kt");
        }

        @Test
        @TestMetadata("Convert4.kt")
        public void testConvert4() throws Exception {
            runTest("testData/interpretation/convert/Convert4.kt");
        }

        @Test
        @TestMetadata("Convert5.kt")
        public void testConvert5() throws Exception {
            runTest("testData/interpretation/convert/Convert5.kt");
        }

        @Test
        @TestMetadata("Convert6.kt")
        public void testConvert6() throws Exception {
            runTest("testData/interpretation/convert/Convert6.kt");
        }

        @Test
        @TestMetadata("Convert7.kt")
        public void testConvert7() throws Exception {
            runTest("testData/interpretation/convert/Convert7.kt");
        }
    }
}
