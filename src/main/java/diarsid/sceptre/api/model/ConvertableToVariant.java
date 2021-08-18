package diarsid.sceptre.api.model;

public interface ConvertableToVariant {
    
    Variant toVariant(int variantIndex);
    
    default Variant toSingleVariant() {
        return this.toVariant(0);
    }
}
