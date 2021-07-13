package diarsid.sceptre.api.model;

import diarsid.sceptre.api.model.Variant;

public interface ConvertableToVariant {
    
    Variant toVariant(int variantIndex);
    
    default Variant toSingleVariant() {
        return this.toVariant(0);
    }
}
