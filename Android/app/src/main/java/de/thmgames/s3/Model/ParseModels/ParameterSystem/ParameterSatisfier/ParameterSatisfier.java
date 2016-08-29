package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterDataProvider;
import de.thmgames.s3.Model.ParseModels.LocalizedString;

/**
 * Created by Benedikt on 08.02.2015.
 */
public interface ParameterSatisfier {
    public void setLocalizedMessage(LocalizedString message);
    public LocalizedString getLocalizedMessage();
    public boolean hasLocalizedMessage();
    public void setParameter(Parameter p);
    public Parameter getParameter();
    public boolean needsInput();
    public String getTag();
    public boolean isSatisfied();
    public String getValue();
    public void loadData();
    public void setSatisfiedCallback(SatisfiedCallback callback);
    public void setDataProvider(ParameterDataProvider provider);
}
