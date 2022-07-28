package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.sceptre.api.LimitedWeightAnalyze;
import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;
import diarsid.sceptre.api.WeightAnalyze;
import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.strings.similarity.api.Similarity;
import diarsid.support.configuration.Configuration;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.Pools;

import static java.lang.Float.MAX_VALUE;
import static java.lang.Float.MIN_VALUE;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Locale.US;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.support.log.Logging.logFor;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.objects.collections.CollectionUtils.shrink;
import static diarsid.support.strings.StringUtils.containsWordsSeparator;
import static diarsid.support.strings.StringUtils.lower;

public class WeightAnalyzeReal implements LimitedWeightAnalyze {
    
    private final int weightAlgorithmVersion = 20;
//    private final PersistentAnalyzeCache<Float> cache;
    private final Float tooBadWeight;
    private final GuardedPool<AnalyzeUnit> analyzeUnitsPool;
    private final Similarity similarity;
    
    private final int defaultWeightResultLimit;
    private boolean isWeightedResultLimitPresent;
    private int weightedResultLimit;
    
    public WeightAnalyzeReal(Configuration configuration, Similarity similarity, Pools pools) {
        this.similarity = similarity;
        this.defaultWeightResultLimit = configuration.asInt("analyze.result.variants.limit");        
        this.tooBadWeight = 9000.0f;
        GuardedPool<Cluster> clusterPool = pools.createPool(
                Cluster.class, 
                () -> new Cluster());        
        this.analyzeUnitsPool = pools.createPool(
                AnalyzeUnit.class, 
                () -> new AnalyzeUnit(clusterPool));
        
//        BiFunction<String, String, Float> weightFunction = (target, pattern) -> {
//            return this.weightStringInternally(pattern, target, NOT_USE_CACHE);
//        }; 
        
//        this.cache = new PersistentAnalyzeCache<>(
//                systemInitiator(),
//                weightFunction,
//                PAIR_HASH_FUNCTION, 
//                weightAlgorithmVersion);
        
//        asyncDo(() -> {
//            logFor(WeightAnalyze.class).info("requesting for data module...");
//            requestPayloadThenAwaitForSupply(ResponsiveDataModule.class).ifPresent((dataModule) -> {
//                logFor(WeightAnalyze.class).info("cache loading...");
//                cache.initPersistenceWith(dataModule.cachedWeight());
//                logFor(WeightAnalyze.class).info("cache loaded");            
//            });
//        });
        
        this.isWeightedResultLimitPresent = true;
        this.weightedResultLimit = defaultWeightResultLimit;        
    }
    
    @Override
    public int resultsLimit() {
        return this.weightedResultLimit;
    }
    
    @Override
    public boolean isResultsLimitPresent() {
        return this.isWeightedResultLimitPresent;
    }
    
    @Override
    public void resultsLimitToDefault() {
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    @Override
    public void disableResultsLimit() {
        this.isWeightedResultLimitPresent = false;
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    @Override
    public void enableResultsLimit() {
        this.isWeightedResultLimitPresent = true;
        this.weightedResultLimit = this.defaultWeightResultLimit;
    }
    
    @Override
    public void setResultsLimit(int newLimit) {
        this.weightedResultLimit = newLimit;
        this.isWeightedResultLimitPresent = true;
    }
    
    public static void logAnalyze(AnalyzeLogType logType, String format, Object... args) {
        if ( logType.isEnabled() ) {
            if ( args.length == 0 ) {
                System.out.println(format);
            } else {
                System.out.println(format(format, args));
            }            
        }
    }
    
    static List<Variant> stringsToVariants(List<String> variantStrings) {
        AtomicInteger counter = new AtomicInteger(0);
        return variantStrings
                .stream()
                .map(string -> new Variant(string, counter.getAndIncrement()))
                .collect(toList());
    }
    
    @Override
    public Variants weightStrings(String pattern, List<String> variants) {
        return this.weightVariants(pattern, stringsToVariants(variants));
    }
    
    @Override
    public Variants weightStrings(String pattern, String noWorseThan, List<String> variants) {
        return this.weightVariants(pattern, noWorseThan, stringsToVariants(variants));
    }
    
    private static boolean canBeEvaluatedByStrictSimilarity(String pattern, String target) {
        if ( containsWordsSeparator(target) ) {
            return false;
        }
        if ( pattern.length() == target.length() ) {
            return pattern.length() < 10;
        } else {
            int min = min(pattern.length(), target.length());
            if ( min > 9 ) {
                return false;
            } else {
                int diff = absDiff(pattern.length(), target.length());
                if ( diff > (min / 3) ) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    @Override
    public boolean isBad(float weight) {
        return (weight == this.tooBadWeight) || weight > this.tooBadWeight;
    }

    @Override
    public boolean isGood(float weight) {
        return weight < this.tooBadWeight;
    }

    @Override
    public boolean isSatisfiable(String pattern, String name) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, name) ) {
            return this.similarity.isSimilar(name, pattern);
        } else {
            return this.isGood(weightStringInternally(pattern, name));
        }        
    }
    
    @Override
    public boolean isSatisfiable(String pattern, Variant variant) {
        if ( canBeEvaluatedByStrictSimilarity(pattern, variant.value()) ) {
            return this.similarity.isSimilar(variant.value(), pattern);
        } else {
            return this.isGood(weightStringInternally(pattern, variant.value()));
        }        
    }
    
    @Override
    public Optional<Variant> weightVariant(String pattern, Variant variant) {
        return this.weightVariantInternally(pattern, variant);
    }

    @Override
    public float weightString(String pattern, String string) {
        return this.weightStringInternally(pattern, string);
    }

    private Optional<Variant> weightVariantInternally(
            String pattern, Variant variant) {
        Float weight = this.weightStringInternally(pattern, variant.value());
        if ( this.isGood(weight) ) {
            variant.set(weight, variant.value().equalsIgnoreCase(pattern));
            return Optional.of(variant);
        } else {
            return Optional.empty();
        }
    }
    
    private Float weightStringInternally(
            String pattern, String target) {
//        if ( cacheUsage.equals(USE_CACHE) ) {
//            Float cachedWeight = this.cache.searchNullableCachedFor(target, pattern);
//            if ( nonNull(cachedWeight) ) {
//                logFor(WeightAnalyze.class).info(format(
//                        "FOUND CACHED %s (target: %s, pattern: %s)", 
//                        cachedWeight, target, pattern));
//                return cachedWeight;
//            }
//        }
        
        AnalyzeUnit analyze = this.analyzeUnitsPool.give();
        try {
            analyze.set(pattern, target);
            if ( analyze.isVariantEqualsPattern() ) {
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    this.cache.addToCache(target, pattern, (float) analyze.weight.sum());
//                }
                return (float) analyze.weight.sum();
            }
            analyze.checkIfVariantTextContainsPatternDirectly();
            analyze.findPathAndTextSeparators();
            analyze.setPatternCharsAndPositions();
            analyze.findPatternCharsPositions();
//            analyze.checkUnsortedPositionsNormality();
            analyze.logUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            analyze.areAllPositionsPresentSortedAndNotPathSeparatorsBetween();
            analyze.ifSingleWordAbbreviation();
            if ( analyze.ifClustersPresentButWeightTooBad() ) {
                logAnalyze(AnalyzeLogType.BASE, "  %s is too bad.", analyze.variant);
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    this.cache.addToCache(target, pattern, this.tooBadWeight);
//                }
                return this.tooBadWeight;
            }
            if ( analyze.areTooMuchPositionsMissed() ) {
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    this.cache.addToCache(target, pattern, this.tooBadWeight);
//                }
                return this.tooBadWeight;
            }
            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
            analyze.calculateWeight();  
            analyze.logState();
            if ( analyze.isVariantTooBad() ) {
                logAnalyze(AnalyzeLogType.BASE, "%s is too bad.", analyze.variant);
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    this.cache.addToCache(target, pattern, this.tooBadWeight);
//                }
                return this.tooBadWeight;
            }
            
//            if ( cacheUsage.equals(USE_CACHE) ) {
//                this.cache.addToCache(target, pattern, (float) analyze.weight.sum());
//            }
            
            return (float) analyze.weight.sum();
        } finally {
            this.analyzeUnitsPool.takeBack(analyze);
        }
    }
    
    @Override
    public Variants weightVariants(String pattern, List<Variant> variants) {
        List<Variant> weightedVariants = this.weightVariantsList(pattern, variants);
        return new VariantsImpl(weightedVariants);
    }
    
    @Override
    public Variants weightVariants(String pattern, String noWorseThan, List<Variant> variants) {
        List<Variant> weightedVariants = this.weightVariantsList(pattern, noWorseThan, variants);
        return new VariantsImpl(weightedVariants);
    }
    
    @Override
    public List<Variant> weightVariantsList(String pattern, List<Variant> variants) {
        return this.weightVariantsListInternally(
                pattern, null, variants);
    }
    
    @Override
    public List<Variant> weightVariantsList(
            String pattern, String noWorseThan, List<Variant> variants) {
        return this.weightVariantsListInternally(
                pattern, noWorseThan, variants);
    }
    
    @Override
    public List<Variant> weightStringsList(String pattern, List<String> strings) {
        return this.weightVariantsListInternally(
                pattern, null, stringsToVariants(strings));
    }
    
    @Override
    public List<Variant> weightStringsList(
            String pattern, String noWorseThan, List<String> strings) {
        return this.weightVariantsListInternally(
                pattern, noWorseThan, stringsToVariants(strings));
    }
    
    private List<Variant> weightVariantsListInternally(
            String pattern, String noWorseThan, List<Variant> rawVariants) {
        boolean weightLimitPresent;
        Float weightLimit = 0.0f;
        if ( nonNull(noWorseThan) ) {
            weightLimit = weightStringInternally(pattern, noWorseThan);
            weightLimitPresent = this.isGood(weightLimit);
        } else {
            weightLimitPresent = false;
        }
        
        pattern = lower(pattern);
        
        sort(rawVariants);   
        
        Map<String, Variant> variantsByName = new HashMap<>();
        Map<String, Variant> variantsByText = new HashMap<>();
        
        Variant duplicateByName;
        Variant duplicateByText;
        
        String lowerVariantText;
        String lowerVariantName = "";
        String variantText;
        String variantName = "";
        
        List<Variant> weightedVariants = new ArrayList<>();        
        AnalyzeUnit analyzeUnit = this.analyzeUnitsPool.give();
        
        float minWeight = MAX_VALUE;
        float maxWeight = MIN_VALUE;
        
        try {
            variantsWeighting: for (Variant variant : rawVariants) {
                variantText = variant.value();
                lowerVariantText = lower(variantText);
                if ( variant.doesHaveName() ) {
                    variantName = variant.name();
                    lowerVariantName = lower(variantName);
                }
                
                if ( variantsByText.containsKey(lowerVariantText) ) {
                    duplicateByText = variantsByText.get(lowerVariantText);
                    if ( duplicateByText.equalsByLowerName(variant) ||
                         duplicateByText.equalsByLowerValue(variant) ) {
                        continue variantsWeighting;
                    }
                }        
                
                logAnalyze(AnalyzeLogType.BASE, "");
                logAnalyze(AnalyzeLogType.BASE, "===== ANALYZE : %s ( %s ) ===== ", variantText, pattern);
                variantsByText.put(lowerVariantText, variant);
                
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    Float cachedWeight = this.cache.searchNullableCachedFor(lowerVariantText, pattern);
//                    if ( nonNull(cachedWeight) ) {
//                        
//                        logAnalyze(BASE, format("  FOUND CACHED weight: %s ", cachedWeight));
//                        
//                        if ( isTooBad(cachedWeight) ) {
//                            logAnalyze(BASE, "  too bad.");
//                            continue variantsWeighting;
//                        }
//                        
//                        if ( variant.doesHaveName()) {
//                            if ( variantsByName.containsKey(lowerVariantName) ) {
//                                duplicateByName = variantsByName.get(lowerVariantName);
//                                if ( cachedWeight < duplicateByName.weight() ) {                                    
//                                    if ( weightLimitPresent ) {
//                                        if ( cachedWeight <= weightLimit ) {
//                                            logFor(WeightAnalyze.class).info("[DUPLICATE] " + variantText + " is better than: " + duplicateByName.value());
//                                            variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                            variantsByName.put(lowerVariantName, variant);
//                                            weightedVariants.add(variant);
//                                        } else {
//                                            logFor(WeightAnalyze.class).info("[DUPLICATE] " + variantText + " is worse than: " + noWorseThan);
//                                        }
//                                    } else {
//                                        logFor(WeightAnalyze.class).info("[DUPLICATE] " + variantText + " is better than: " + duplicateByName.value());
//                                        variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                        variantsByName.put(lowerVariantName, variant);
//                                        weightedVariants.add(variant);
//                                    }
//                                } 
//                            } else {
//                                if ( weightLimitPresent ) {
//                                    if ( cachedWeight <= weightLimit ) {
//                                        variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                        variantsByName.put(lowerVariantName, variant);
//                                        weightedVariants.add(variant);      
//                                    } else {
//                                        logFor(WeightAnalyze.class).info(variantText + " is worse than: " + noWorseThan);
//                                    }
//                                } else {
//                                    variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                    variantsByName.put(lowerVariantName, variant);
//                                    weightedVariants.add(variant);      
//                                }
//                                                      
//                            }
//                        } else {
//                            if ( weightLimitPresent ) {
//                                if ( cachedWeight <= weightLimit ) {
//                                    variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                    weightedVariants.add(variant);  
//                                } else {
//                                    logFor(WeightAnalyze.class).info(variantText + " is worse than: " + noWorseThan);
//                                }
//                            } else {
//                                variant.set(cachedWeight, lowerVariantText.equals(pattern));
//                                weightedVariants.add(variant);  
//                            }                                                
//                        }
//                        
//                        continue variantsWeighting;
//                    }
//                }        

                analyzeUnit.set(pattern, variantText);
                if ( analyzeUnit.isVariantNotEqualsPattern() ) {
                    analyzeUnit.checkIfVariantTextContainsPatternDirectly();
                    analyzeUnit.findPathAndTextSeparators();
                    analyzeUnit.setPatternCharsAndPositions();
                    analyzeUnit.findPatternCharsPositions();
                    analyzeUnit.logUnsortedPositions();
                    analyzeUnit.sortPositions();
                    analyzeUnit.findPositionsClusters();
                    analyzeUnit.areAllPositionsPresentSortedAndNotPathSeparatorsBetween();
                    analyzeUnit.ifSingleWordAbbreviation();
                    if ( analyzeUnit.ifClustersPresentButWeightTooBad() ) {
                        logAnalyze(AnalyzeLogType.BASE, "  %s is too bad.", analyzeUnit.variant);
                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variantText, pattern, this.tooBadWeight);
//                        }
                        
                        analyzeUnit.clearForReuse();
                        continue variantsWeighting;
                    }
                    if ( analyzeUnit.areTooMuchPositionsMissed() ) {
                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variantText, pattern, this.tooBadWeight);
//                        }
                        
                        analyzeUnit.clearForReuse();
                        continue variantsWeighting;
                    }
                    analyzeUnit.calculateClustersImportance();
                    analyzeUnit.isFirstCharMatchInVariantAndPattern(pattern);
                    analyzeUnit.calculateWeight();  
                    analyzeUnit.logState();
                    if ( analyzeUnit.isVariantTooBad() ) {
                        logAnalyze(AnalyzeLogType.BASE, "  %s is too bad.", analyzeUnit.variant);
                        
//                        if ( cacheUsage.equals(USE_CACHE) ) {
//                            this.cache.addToCache(variantText, pattern, this.tooBadWeight);
//                        }
                        
                        analyzeUnit.clearForReuse();                        
                        continue variantsWeighting;
                    }

                    if ( analyzeUnit.weight.sum() < minWeight ) {
                        minWeight = analyzeUnit.weight.sum();
                    }
                    if ( analyzeUnit.weight.sum() > maxWeight ) {
                        maxWeight = analyzeUnit.weight.sum();
                    }                
                }

                
                variant.set(analyzeUnit.weight.sum(), analyzeUnit.variantEqualsToPattern);
                if ( variant.doesHaveName() ) {
                    logFor(WeightAnalyze.class).info(variantText + ":" + variantName);
                    if ( variantsByName.containsKey(lowerVariantName) ) {
                        duplicateByName = variantsByName.get(lowerVariantName);
                        if ( variant.isBetterThan(duplicateByName) ) {
                            if ( weightLimitPresent ) {
                                if ( variant.weight() <= weightLimit ) {
                                    logFor(WeightAnalyze.class).info("[DUPLICATE] " + variantText + " is better than: " + duplicateByName.value());
                                    variantsByName.put(lowerVariantName, variant);
                                    weightedVariants.add(variant);
                                } else {
                                    logFor(WeightAnalyze.class).info(variantText + " is worse than: " + noWorseThan);
                                }
                            } else {
                                logFor(WeightAnalyze.class).info("[DUPLICATE] " + variantText + " is better than: " + duplicateByName.value());
                                variantsByName.put(lowerVariantName, variant);
                                weightedVariants.add(variant);
                            } 
                        } 
                    } else {
                        if ( weightLimitPresent ) {
                            if ( variant.weight() <= weightLimit ) {
                                variantsByName.put(lowerVariantName, variant);
                                weightedVariants.add(variant);     
                            } else {
                                logFor(WeightAnalyze.class).info(variantText + " is worse than: " + noWorseThan);
                            }
                        } else {
                            variantsByName.put(lowerVariantName, variant);
                            weightedVariants.add(variant);     
                        }         
                    }
                } else {
                    if ( weightLimitPresent ) {
                        if ( variant.weight() <= weightLimit ) {
                            weightedVariants.add(variant);             
                        } else {
                            logFor(WeightAnalyze.class).info(variantText + " is worse than: " + noWorseThan);
                        }
                    } else {
                        weightedVariants.add(variant);             
                    } 
                } 
                
//                if ( cacheUsage.equals(USE_CACHE) ) {
//                    cache.addToCache(variantText, pattern, (float) variant.weight());
//                }
                
                analyzeUnit.clearForReuse();
            }
        } finally {
            this.analyzeUnitsPool.takeBack(analyzeUnit);
        }
        
        sort(weightedVariants);
        if ( this.isWeightedResultLimitPresent ) {
            shrink(weightedVariants, this.weightedResultLimit);
        }
        logFor(WeightAnalyze.class).info("weightedVariants qty: " + weightedVariants.size());        
        weightedVariants
                .stream()
                .forEach(candidate -> logFor(WeightAnalyze.class).info(format(US, "%.3f : %s:%s", candidate.weight(), candidate.value(), candidate.name())));

        return weightedVariants;
    }
    
}
