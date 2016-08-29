package de.thmgames.s3.Model.ParseModels.Questsystem;

import com.parse.ParseClassName;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.WebElement;

/**
 * Created by Benedikt on 22.10.2014.
 */
@ParseClassName(Quest.PARSE_KEY)
public class Quest extends AbstractParseObject{
    public final static String PARSE_KEY="Quest";
    public final static String QUEST_NAME="name";
    public final static String QUEST_LORE="lore";
    public final static String QUEST_IMG="image";
    public final static String QUEST_MINI_IMG="miniImage";
    public final static String QUEST_START="startdate";
    public final static String QUEST_END="enddate";
    public final static String QUEST_SUBJOBS=Job.PARSE_KEY+"s";
    public final static String QUEST_DESCRIPTION ="description";
    public final static String QUEST_SHORT_DESCRIPTION ="shortDescription";
    public static final String QUEST_VISIBILITY = "visibility";
    public static final String QUEST_MINAPI="minAPI";

    public int getMinAPI(){
        return getIntWithDefault(QUEST_MINAPI);
    }

    public boolean isVisible(){
        return getBooleanWithDefault(QUEST_VISIBILITY);
    }

    public LocalizedString getName(){
        return (LocalizedString)getParseObject(QUEST_NAME);
    }

    public WebElement getLore(){
        return (WebElement) getParseObject(QUEST_LORE);
    }

    public WebElement getDescription(){
        return (WebElement) getParseObject(QUEST_DESCRIPTION);
    }

    public LocalizedString getShortDescription(){
        return (LocalizedString)getParseObject(QUEST_SHORT_DESCRIPTION);
    }

    public boolean hasJobs(){
        return has(QUEST_SUBJOBS);
    }

    public ParseFile getImage(){
        return getParseFile(QUEST_IMG);
    }

    public boolean isValidFor(Date now){
        return now.after(getDateWithDefault(QUEST_START)) && now.before(getDateWithDefault(QUEST_END));
    }


    public List<Job> getJobs(){
        if(has(QUEST_SUBJOBS)){
            return getList(QUEST_SUBJOBS);
        }else{
            return new ArrayList<>();
        }
    }

    public boolean hasImage(){
        return has(QUEST_IMG);
    }

    public boolean hasMiniImage(){
        return has(QUEST_MINI_IMG);
    }

    public ParseFile getMiniImage() {
        return getParseFile(QUEST_MINI_IMG);
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return true;
    }

}
