package diarsid.sceptre.api;

import java.util.List;
import java.util.Optional;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;
import diarsid.sceptre.impl.AnalyzeImpl;
import diarsid.support.model.versioning.Version;
import diarsid.support.objects.CommonEnum;
import diarsid.support.objects.Pools;

import static diarsid.support.objects.Pools.pools;

public interface Sceptre {

    public interface Weight {

        public static final float TOO_BAD = 9000f;

        public enum Estimate implements CommonEnum<Estimate> {

            PERFECT (3),
            GOOD (2),
            MODERATE (1),
            BAD (0);

            private final int level;

            private Estimate(int level) {
                this.level = level;
            }

            public static final float BAD_VS_MODERATE_BOUND = -10;
            public static final float MODERATE_VS_GOOD_BOUND = -36;
            public static final float GOOD_VS_PERFECT_BOUND = -75;

            public static Estimate of(Variant variant) {
                return of(variant.weight());
            }

            public static Estimate preliminarilyOf(float weight) {
                return of(weight - 5.0f);
            }

            public static Estimate of(float weight) {
                if ( weight > BAD_VS_MODERATE_BOUND) {
                    return BAD;
                }
                else if ( BAD_VS_MODERATE_BOUND >= weight && weight > MODERATE_VS_GOOD_BOUND) {
                    return MODERATE;
                }
                else if ( MODERATE_VS_GOOD_BOUND >= weight && weight > GOOD_VS_PERFECT_BOUND) {
                    return GOOD;
                }
                else {
                    return PERFECT;
                }
            }

            public boolean isEqualOrBetterThan(Estimate other) {
                return this.level >= other.level;
            }

            public boolean isBetterThan(Estimate other) {
                return this.level > other.level;
            }
        }
    }

    interface Analyze {

        Version version();

        Variants processStrings(String pattern, List<String> variants);

        Variants processVariants(String pattern, List<Variant> variants);

        Variants processStrings(String pattern, String noWorseThan, List<String> variants);

        Variants processVariants(String pattern, String noWorseThan, List<Variant> variants);

        List<Variant> processStringsToList(String pattern, List<String> strings);

        List<Variant> processStringsToList(String pattern, String noWorseThan, List<String> strings);

        List<Variant> processVariantsToList(String pattern, List<Variant> variants);

        List<Variant> processVariantsToList(String pattern, String noWorseThan, List<Variant> variants);

        Optional<Variant> processVariant(String pattern, Variant variant);

        float processString(String pattern, String string);

        interface LimitedBySize extends Analyze {

            boolean isResultsLimitPresent();

            int resultsLimit();

            void setResultsLimit(int newLimit);

            void disableResultsLimit();

            void enableResultsLimit();

            void resultsLimitToDefault();

        }
    }

    public static Analyze.LimitedBySize newAnalyzeInstance(int sizeLimit) {
        return new AnalyzeImpl(sizeLimit, pools());
    }

    public static Analyze.LimitedBySize newAnalyzeInstance(int sizeLimit, Pools pools) {
        return new AnalyzeImpl(sizeLimit, pools);
    }

    public static Analyze newAnalyzeInstance() {
        return new AnalyzeImpl(pools());
    }

    public static Analyze newAnalyzeInstance(Pools pools) {
        return new AnalyzeImpl(pools);
    }
}
