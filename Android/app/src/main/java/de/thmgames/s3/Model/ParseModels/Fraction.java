package de.thmgames.s3.Model.ParseModels;

import android.graphics.Color;

import com.parse.ParseClassName;
import com.parse.ParseFile;

import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;

/**
 * Created by Benedikt on 20.10.2014.
 */
@ParseClassName(Fraction.PARSE_KEY)
public class Fraction extends AbstractParseObject {

    public final static String PARSE_KEY = "Fraction";
    public final static String FRACTION_NAME = "name";
    public final static String FRACTION_LORE = "lore";

    public final static String FRACTION_POINTS = "points";

    public final static String FRACTION_MAINCOLOR = "mainColor";
    public final static String FRACTION_TEXTCOLOR = "textColor";
    public final static String FRACTION_HEADER_IMG = "headerimg";
    public final static String FRACTION_LOGO = "logo";

    public final static String FRACTION_VISIBILITY = "visibility";

    public LocalizedString getName() {
        return (LocalizedString) getParseObject(FRACTION_NAME);
    }

    public int getPoints() {
        return getIntWithDefault(FRACTION_POINTS);
    }

    public WebElement getLore() {
        return (WebElement) getParseObject(FRACTION_LORE);
    }

    public int getMainColor() {
        return getColorIntWithDefault(FRACTION_MAINCOLOR);
    }

    public int getTextColor() {
        return getColorIntWithDefault(FRACTION_TEXTCOLOR, Color.parseColor("#ffffff"));
    }

    public boolean isVisible() {
        return getBooleanWithDefault(FRACTION_VISIBILITY);
    }


    public boolean hasLogo() {
        return has(FRACTION_LOGO) && get(FRACTION_LOGO)!=null;
    }

    public boolean hasHeaderImage() {
        return has(FRACTION_HEADER_IMG) && get(FRACTION_HEADER_IMG)!=null;
    }

    public ParseFile getLogo() {
        return getParseFile(FRACTION_LOGO);
    }

    public ParseFile getHeaderImg() {
        return getParseFile(FRACTION_HEADER_IMG);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }
}
