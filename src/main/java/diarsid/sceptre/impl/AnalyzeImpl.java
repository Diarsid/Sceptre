package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.sceptre.api.Analyze;
import diarsid.sceptre.api.WeightEstimate;
import diarsid.sceptre.api.model.Input;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.impl.logs.Logging;
import diarsid.support.model.versioning.Version;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.Pools;

import static java.lang.Float.MAX_VALUE;
import static java.lang.Float.MIN_VALUE;
import static java.util.Collections.sort;
import static java.util.Objects.nonNull;

import static diarsid.sceptre.api.LogType.BASE;
import static diarsid.sceptre.api.WeightEstimate.BAD;
import static diarsid.support.strings.StringUtils.lower;

public class AnalyzeImpl implements Analyze {

    public static final Version VERSION = new Version("1.5.0");

    private final GuardedPool<AnalyzeUnit> analyzeUnitsPool;

    private final Logging log;
    private final AnalyzeBuilder builder;
    
    public AnalyzeImpl(AnalyzeBuilder builder) {
        Pools pools = builder.pools;
        this.log = new Logging(builder);

        GuardedPool<Cluster> clusterPool = pools.createPool(
                Cluster.class, 
                () -> new Cluster(this.log));

        GuardedPool<WordInInput> wordPool = pools.createPool(
                WordInInput.class,
                () -> new WordInInput());

        GuardedPool<WordsInInput.WordsInRange> wordsInRangePool = pools.createPool(
                WordsInInput.WordsInRange.class,
                () -> new WordsInInput.WordsInRange());

        GuardedPool<Step2LoopCandidatePosition> step2LoopCandidatePositionsPool = pools.createPool(
                Step2LoopCandidatePosition.class,
                () -> new Step2LoopCandidatePosition());

        this.analyzeUnitsPool = pools.createPool(
                AnalyzeUnit.class,
                () -> new AnalyzeUnit(this.log, clusterPool, wordPool, wordsInRangePool, step2LoopCandidatePositionsPool));

        this.builder = builder;
    }

    @Override
    public Version version() {
        return VERSION;
    }
    
    static List<Input> stringsToInputs(List<String> strings) {
        List<Input> inputs = new ArrayList<>();
        Input input;
        for ( int i = 0; i < strings.size(); i++ ) {
            input = new Input(strings.get(i));
            ((InputIndexable) input).setIndex(i);
            inputs.add(input);
        }

        return inputs;
    }
    
    @Override
    public Optional<Output> process(String pattern, Input input) {
        return this.weightInputInternally(pattern, input);
    }

    @Override
    public float process(String pattern, String string) {
        return this.weightStringInternally(pattern, string);
    }

    private Optional<Output> weightInputInternally(
            String pattern, Input input) {
        float weight = this.weightStringInternally(pattern, input.string());
        if ( WeightEstimate.of(weight).isBetterThan(BAD) ) {
            Output output = new OutputImpl(input, 0, weight);
            return Optional.of(output);
        } else {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Output> processInputs(String pattern, List<Input> inputs) {
        return this.weightInputsListInternally(
                pattern, null, inputs);
    }
    
    @Override
    public List<Output> processInputs(
            String pattern, String noWorseThan, List<Input> inputs) {
        return this.weightInputsListInternally(
                pattern, noWorseThan, inputs);
    }
    
    @Override
    public List<Output> processStrings(String pattern, List<String> strings) {
        return this.weightInputsListInternally(
                pattern, null, stringsToInputs(strings));
    }
    
    @Override
    public List<Output> processStrings(
            String pattern, String noWorseThan, List<String> strings) {
        return this.weightInputsListInternally(
                pattern, noWorseThan, stringsToInputs(strings));
    }

    private float weightStringInternally(
            String pattern, String target) {

        AnalyzeUnit analyze = this.analyzeUnitsPool.give();

        this.log.begins();
        try {
            analyze.set(pattern, target);
            if ( analyze.isVariantEqualsPattern() ) {
                return analyze.weight.sum();
            }
            analyze.checkIfVariantTextContainsPatternDirectly();
            analyze.findWordsAndPathAndTextSeparators();
            analyze.setPatternCharsAndPositions();
            analyze.findPatternCharsPositions();
//            analyze.checkUnsortedPositionsNormality();
            analyze.logUnsortedPositions();
            analyze.sortPositions();
            analyze.findPositionsClusters();
            analyze.areAllPositionsPresentSortedAndNotPathSeparatorsBetween();
            analyze.ifSingleWordAbbreviation();

            if ( analyze.ifClustersPresentButWeightTooBad() ) {
                log.add(BASE, "  %s is too bad.", analyze.variant);
                return WeightEstimate.TOO_BAD;
            }

            if ( analyze.areTooMuchPositionsMissed() ) {
                return WeightEstimate.TOO_BAD;
            }

            analyze.calculateClustersImportance();
            analyze.isFirstCharMatchInVariantAndPattern(pattern);
            analyze.calculateWeight();
            analyze.logState();

            if ( analyze.isVariantTooBad() ) {
                log.add(BASE, "%s is too bad.", analyze.variant);
                return WeightEstimate.TOO_BAD;
            }

            return analyze.weight.sum();
        }
        finally {
            this.log.finished();
            this.analyzeUnitsPool.takeBack(analyze);
        }
    }

    private static void indexing(List<? extends Indexable> inputs) {
        if ( inputs.isEmpty() ) {
            return;
        }

        boolean isIndexed = inputs.get(0).index() == 0;

        if ( isIndexed ) {
            return;
        }

        for ( int i = 0; i < inputs.size(); i++ ) {
            inputs.get(i).setIndex(i);
        }
    }
    
    private List<Output> weightInputsListInternally(
            String pattern, String noWorseThan, List<Input> inputs) {
        indexing(inputs);

        boolean weightLimitPresent;
        float weightLimit = 0.0f;
        if ( nonNull(noWorseThan) ) {
            weightLimit = weightStringInternally(pattern, noWorseThan);
            weightLimitPresent = WeightEstimate.of(weightLimit).isBetterThan(BAD);
        } else {
            weightLimitPresent = false;
        }
        
        pattern = lower(pattern);

        String inputString;
        
        List<OutputImpl> weightedOutputs = new ArrayList<>();
        AnalyzeUnit analyzeUnit = this.analyzeUnitsPool.give();
        
        float minWeight = MAX_VALUE;
        float maxWeight = MIN_VALUE;

        this.log.begins();
        try {
            OutputImpl output;
            inputsWeighting: for (Input input : inputs) {
                inputString = input.string();
                
                log.add(BASE, "");
                log.add(BASE, "===== Pattern:'%s' Input:'%s' ===== ", pattern, inputString);

                analyzeUnit.set(pattern, inputString);

                if ( analyzeUnit.isVariantNotEqualsPattern() ) {
                    analyzeUnit.checkIfVariantTextContainsPatternDirectly();
                    analyzeUnit.findWordsAndPathAndTextSeparators();
                    analyzeUnit.setPatternCharsAndPositions();
                    analyzeUnit.findPatternCharsPositions();
                    analyzeUnit.logUnsortedPositions();
                    analyzeUnit.sortPositions();
                    analyzeUnit.findPositionsClusters();
                    analyzeUnit.areAllPositionsPresentSortedAndNotPathSeparatorsBetween();
                    analyzeUnit.ifSingleWordAbbreviation();

                    if ( analyzeUnit.ifClustersPresentButWeightTooBad() ) {
                        log.add(BASE, "  %s is too bad.", analyzeUnit.variant);
                        
                        analyzeUnit.clearForReuse();
                        continue inputsWeighting;
                    }

                    if ( analyzeUnit.areTooMuchPositionsMissed() ) {
                        
                        analyzeUnit.clearForReuse();
                        continue inputsWeighting;
                    }

                    analyzeUnit.calculateClustersImportance();
                    analyzeUnit.isFirstCharMatchInVariantAndPattern(pattern);
                    analyzeUnit.calculateWeight();  
                    analyzeUnit.logState();

                    if ( analyzeUnit.isVariantTooBad() ) {
                        log.add(BASE, "  %s is too bad.", analyzeUnit.variant);
                        
                        analyzeUnit.clearForReuse();                        
                        continue inputsWeighting;
                    }

                    if ( analyzeUnit.weight.sum() < minWeight ) {
                        minWeight = analyzeUnit.weight.sum();
                    }

                    if ( analyzeUnit.weight.sum() > maxWeight ) {
                        maxWeight = analyzeUnit.weight.sum();
                    }                
                }

                if ( this.builder.isDeclaringAdditionalData() ) {
                    output = new OutputImpl(
                            input,
                            analyzeUnit.weight.sum(),
                            analyzeUnit.produceAdditionalData(this.builder.additionalData));
                }
                else {
                    output = new OutputImpl(input, analyzeUnit.weight.sum());
                }

                if ( weightLimitPresent ) {
                    if ( output.weight() <= weightLimit ) {
                        weightedOutputs.add(output);
                    } else {
                        log.add(BASE, "%s is worse than: %s", inputString, noWorseThan);
                    }
                } else {
                    weightedOutputs.add(output);
                }
                
                analyzeUnit.clearForReuse();
            }
        
            sort(weightedOutputs);

            indexing(weightedOutputs);

            this.log.add(BASE, "outputs qty: " + weightedOutputs.size());

            for ( int i = 0; i < weightedOutputs.size(); i++ ) {
                output = weightedOutputs.get(i);
                this.log.add(BASE, "    %.3f : %s", output.weight(), output.input());
            }
        }
        finally {
            this.log.finished();
            this.analyzeUnitsPool.takeBack(analyzeUnit);
        }

        Object list = weightedOutputs;
        return (List<Output>) list;
    }
    
}
