package diarsid.sceptre.impl.weight;

public interface WeightConsumer {
    
    void accept(int index, float weight, WeightElement weightElement);
}
