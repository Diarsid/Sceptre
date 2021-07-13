package diarsid.sceptre.api;

import diarsid.sceptre.api.WeightAnalyze;

public interface LimitedWeightAnalyze extends WeightAnalyze {

    boolean isResultsLimitPresent();

    int resultsLimit();

    void setResultsLimit(int newLimit);

    void disableResultsLimit();

    void enableResultsLimit();

    void resultsLimitToDefault();
    
}
