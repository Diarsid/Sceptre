package diarsid.sceptre.api;

import java.util.List;
import java.util.Optional;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;

public interface WeightAnalyze {

    boolean isGood(float weight);

    boolean isBad(float weight);

    boolean isSatisfiable(String pattern, String name);

    boolean isSatisfiable(String pattern, Variant variant);

    Variants weightStrings(String pattern, List<String> variants);

    Variants weightVariants(String pattern, List<Variant> variants);

    Variants weightStrings(String pattern, String noWorseThan, List<String> variants);

    Variants weightVariants(String pattern, String noWorseThan, List<Variant> variants);

    List<Variant> weightStringsList(String pattern, List<String> strings);

    List<Variant> weightStringsList(String pattern, String noWorseThan, List<String> strings);

    List<Variant> weightVariantsList(String pattern, List<Variant> variants);

    List<Variant> weightVariantsList(String pattern, String noWorseThan, List<Variant> variants);

    Optional<Variant> weightVariant(String pattern, Variant variant);

    float weightString(String pattern, String string);
    
}
