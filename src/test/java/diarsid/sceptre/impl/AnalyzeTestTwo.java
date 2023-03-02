package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;
import diarsid.support.objects.GuardedPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static diarsid.sceptre.impl.AnalyzeImpl.stringsToVariants;
import static diarsid.support.configuration.Configuration.configure;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;
import static org.junit.jupiter.api.Assertions.fail;

public class AnalyzeTestTwo {

    static {
        configure().withDefault(
                "log = true",
                "analyze.weight.base.log = true",
                "analyze.weight.positions.search.log = true",
                "analyze.weight.positions.clusters.log = true",
                "analyze.result.variants.limit = 11",
                "analyze.similarity.log.base = true",
                "analyze.similarity.log.advanced = true");
    }
    
    private static AnalyzeImpl analyzeInstance;
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;
    
    private AnalyzeImpl analyze;
    private boolean expectedToFail;
    private String pattern;
    private List<String> variants;
    private List<String> expected;
    private Variants weightedVariants;
    
    public AnalyzeTestTwo() {
    }

    @BeforeAll
    public static void setUpClass() {
        analyzeInstance = new AnalyzeImpl(pools());
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
        this.analyze.resultsLimitToDefault();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrposapi() {
        pattern = "ukrposapi";
        
        variants = asList(            
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
        
        variants = asList(            
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
            totalVariantsQuantity = totalVariantsQuantity + variants.size();
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
        weightedVariants = this.analyze.processVariants(pattern, stringsToVariants(variants));
        
        String expectedVariant;
        String actualVariant;
        List<Variant> nextSimilarVariants;
        
        List<String> reports = new ArrayList();        
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && weightedVariants.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( weightedVariants.next() && ( counter.get() < expected.size() ) ) {
            
            if ( weightedVariants.isCurrentMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = weightedVariants.current().value();
                
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
                nextSimilarVariants = weightedVariants.nextSimilarVariants();
                for (Variant variant : nextSimilarVariants) {
                    actualVariant = variant.value();
                    
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
        
        if ( weightedVariants.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < weightedVariants.size(); i++) {
                presentButNotExpectedVariant = weightedVariants.getVariantAt(i);
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
        weightedVariants.resetTraversing();

        while ( weightedVariants.next() ) {            
            if ( weightedVariants.isCurrentMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + weightedVariants.current().value() + " is much better than next: " + weightedVariants.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedVariants.nextSimilarVariants()
                        .stream()
                        .forEach(candidate -> {
                            variantsWithWeight.add("\n  - " + candidate.value() + " : " + candidate.weight());
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
