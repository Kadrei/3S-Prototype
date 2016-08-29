package de.thmgames.s3.Model;

import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.LocationSystem.MapElement;

/**
 * Created by Benedikt on 24.02.2015.
 */
public interface ISpinnerElement {
    public LocalizedString getTitle();
    public boolean hasParent();
    public MapElement getParent();
    public String toString();
}
