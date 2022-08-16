package diarsid.sceptre.impl;

import diarsid.support.objects.GuardedPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static diarsid.support.configuration.Configuration.configure;

public class AnalyzeUnitTest {

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

    private static final GuardedPool<Cluster> clusterPool = new GuardedPool<>(() -> new Cluster());
    private static final GuardedPool<WordInVariant> wordPool = new GuardedPool<>(() -> new WordInVariant());
    private static final GuardedPool<WordsInVariant.WordsInRange> wordsInRangePool = new GuardedPool<>(() -> new WordsInVariant.WordsInRange());

    private AnalyzeUnit unit;

    @BeforeEach
    public void setUp() {
        this.unit = new AnalyzeUnit(clusterPool, wordPool, wordsInRangePool);
    }

    @Test
    public void test() {
        unit.set("", "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient");
        unit.findWordsAndPathAndTextSeparators();
    }
}
