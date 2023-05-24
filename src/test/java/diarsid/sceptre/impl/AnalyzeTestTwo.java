package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.sceptre.api.Analyze;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Outputs;
import diarsid.support.objects.GuardedPool;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.junit.jupiter.api.Assertions.fail;

import static diarsid.sceptre.api.LogType.BASE;
import static diarsid.sceptre.api.LogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.api.LogType.POSITIONS_SEARCH;
import static diarsid.sceptre.impl.AnalyzeImpl.stringsToInputs;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;

public class AnalyzeTestTwo {
    
    private static Analyze analyzeInstance;
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;
    
    private Analyze analyze;
    private boolean expectedToFail;
    private String pattern;
    private List<String> inputs;
    private List<String> expected;
    private Outputs weightedOutputs;
    
    public AnalyzeTestTwo() {
    }

    @BeforeAll
    public static void setUpClass() {

        analyzeInstance = new AnalyzeBuilder()
                .withLogEnabled(true)
                .withLogTypeEnabled(BASE, true)
                .withLogTypeEnabled(POSITIONS_SEARCH, true)
                .withLogTypeEnabled(POSITIONS_CLUSTERS, true)
                .build();

        start = currentTimeMillis();
    }
    
    @AfterAll
    public static void tearDownClass() {
        stop = currentTimeMillis();
        Logger logger = LoggerFactory.getLogger(AnalyzeTest.class);
        String report = 
                "\n ======================================" +
                "\n ====== Total AnalyzeTest results =====" +
                "\n ======================================" +
                "\n  total time     : %s " + 
                "\n  total variants : %s \n";
        logger.info(format(report, stop - start, totalVariantsQuantity));
        Optional<GuardedPool<AnalyzeUnit>> pool = pools().poolOf(AnalyzeUnit.class);
        if ( pool.isPresent() ) {
            GuardedPool<AnalyzeUnit> c = pool.get();
            AnalyzeUnit analyzeData = c.give();
        }
    }
    
    @BeforeEach
    public void setUp() {
        this.analyze = analyzeInstance;
    }
    
    @AfterEach
    public void tearDown() {

    }
    
    @Test
    public void test_projectsUkrPoshta_ukrposapi() {
        pattern = "ukrposapi";
        
        inputs = asList(
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukropsapi() {
        pattern = "ukropsapi";
        
        inputs = asList(
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    
    
    private void weightVariantsAndCheckMatching() {
        boolean failed;
        try {
            totalVariantsQuantity = totalVariantsQuantity + inputs.size();
            weightVariantsAndCheckMatchingInternally();
            failed = false;
        } catch (AssertionError e) {
            failed = true;
            if ( ! this.expectedToFail ) {
                throw e;
            }
        }        
        if ( ! failed && this.expectedToFail ) {
            fail("=== EXPECTED TO FAIL BUT PA SSED ===");
        }
    }
    
    private void weightVariantsAndCheckMatchingInternally() {
        weightedOutputs = this.analyze.processInputs(pattern, stringsToInputs(inputs));
        
        String expectedVariant;
        String actualVariant;
        List<Output> nextSimilarOutputs;
        
        List<String> reports = new ArrayList();        
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && weightedOutputs.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( weightedOutputs.next() && ( counter.get() < expected.size() ) ) {
            
            if ( weightedOutputs.isCurrentMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = weightedOutputs.current().input();
                
                if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                    reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                } else {
                    mismatches++;
                    reports.add(format(
                            "\n%s variant does not match expected: \n" +
                            "    expected : %s\n" +
                            "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                }
            } else {            
                nextSimilarOutputs = weightedOutputs.nextSimilarSublist();
                for (Output output : nextSimilarOutputs) {
                    actualVariant = output.input();
                    
                    if ( counter.get() < expected.size() ) {
                        expectedVariant = expected.get(counter.getAndIncrement());

                        if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                            reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                        } else {
                            mismatches++;
                            reports.add(format(
                                "\n%s variant does not match expected: \n" +
                                "    expected : %s\n" +
                                "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                        }
                    } else {
                        presentButNotExpected.add(format("\n %s\n", actualVariant));
                    }    
                }
            }           
        } 
        
        if ( nonEmpty(reports) ) {
            reports.add("\n === Diff with expected === ");
        }
        
        if ( weightedOutputs.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < weightedOutputs.size(); i++) {
                presentButNotExpectedVariant = weightedOutputs.get(i).input();
                presentButNotExpected.add(format("\n %s\n", presentButNotExpectedVariant));
            }
        }
        
        boolean hasNotExpected = nonEmpty(presentButNotExpected);
        if ( hasNotExpected ) {
            presentButNotExpected.add(0, "\n === Present but not expected === ");
        }
        
        boolean hasMissed = counter.get() < expected.size();
        List<String> expectedButMissed = new ArrayList<>();
        if ( hasMissed ) {            
            expectedButMissed.add("\n === Expected but missed === ");
            
            while ( counter.get() < expected.size() ) {                
                expectedButMissed.add(format("\n%s variant missed: %s", counter.get(), expected.get(counter.getAndIncrement())));
            }
        }
            
        if ( mismatches > 0 || hasMissed || hasNotExpected ) {    
            if ( hasMissed ) {
                reports.addAll(expectedButMissed);
            }
            if ( hasNotExpected ) {
                reports.addAll(presentButNotExpected);
            }
            reports.add(0, collectVariantsToReport());
            fail(reports.stream().collect(joining()));
        }
    }
    
    private String collectVariantsToReport() {
        List<String> variantsWithWeight = new ArrayList<>();
        weightedOutputs.resetTraversing();

        while ( weightedOutputs.next() ) {
            if ( weightedOutputs.isCurrentMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + weightedOutputs.current().input() + " is much better than next: " + weightedOutputs.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedOutputs.nextSimilarSublist()
                        .stream()
                        .forEach(candidate -> {
                            variantsWithWeight.add("\n  - " + candidate.input() + " : " + candidate.weight());
                        });
            }
        }
        if ( nonEmpty(variantsWithWeight) ) {            
            variantsWithWeight.add(0, "\n === Analyze result === ");
        }
        variantsWithWeight.add("");
        
        return variantsWithWeight.stream().collect(joining());
    }
    
}
