package de.thmgames.s3.Model.ParseModels.ParameterSystem;

import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.ApiVersionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.LocaleParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.NearLocationParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.TimestampParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserCodeParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserEnergyParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserFractionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UsernameParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserPointsParameterSatisfier;

/**
 * Created by Benedikt on 08.02.2015.
 */
public interface ParameterDataProvider {
    public void loadNextLocations(NearLocationParameterSatisfier nearLocationParameterSatisfier);
    public void loadUserFraction(UserFractionParameterSatisfier parameterSatisfier);
    public void loadTimestamp(TimestampParameterSatisfier timestampParameterSatisfier);
    public void loadUsername(UsernameParameterSatisfier parameterSatisfier);
    public void loadUserEnergy(UserEnergyParameterSatisfier userEnergyParameterSatisfier);
    public void loadUserInput(UserCodeParameterSatisfier userCodeParameterSatisfier);
    public void loadUserPoints(UserPointsParameterSatisfier userPointsParameterSatisfier);
    public void loadLocale(LocaleParameterSatisfier localeParameterSatisfier);
    public void loadApiVersion(ApiVersionParameterSatisfier apiVersionParameterSatisfier);
}
