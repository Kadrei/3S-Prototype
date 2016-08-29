package de.thmgames.s3.Model.ParseModels.ActionSystem;

import com.parse.ParseClassName;

import de.thmgames.s3.Model.ParseModels.AbstractParseObject;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.WebElement;

/**
 * Created by Benedikt on 11.02.2015.
 */
@ParseClassName(Action.PARSE_KEY)
public class Action extends AbstractParseObject {
    public final static String PARSE_KEY = "Action";
    public final static String ACTION_TYPE="type";
    public final static String ACTION_INFORMATION="information";

    public enum TYPE {
        SHOW_STORY,SHOW_MESSAGE_DIALOG, INVALID;

        public int getInt() {
            switch (this) {
                case SHOW_MESSAGE_DIALOG:
                    return 0;
                case SHOW_STORY:
                    return 1;
                case INVALID:
                default:
                    return -1;
            }
        }
    }

    public String getActionInformation(){
        return getStringWithDefault(ACTION_INFORMATION);
    }

    public int getActionTypeInt(){
        return getIntWithDefault(ACTION_TYPE,TYPE.INVALID.getInt());
    }

    public TYPE getActionType(){
        int type = getActionTypeInt();
        if(type==TYPE.SHOW_STORY.getInt()){
            return TYPE.SHOW_STORY;
        }
        if(type==TYPE.SHOW_MESSAGE_DIALOG.getInt()){
            return TYPE.SHOW_MESSAGE_DIALOG;
        }
        return TYPE.INVALID;
    }

    @Override
    protected boolean shouldBeSavedInLocalStore() {
        return false;
    }

    public void resolveAction(ActionResolver resolver){
        switch(this.getActionType()){
            case SHOW_STORY:
                resolver.onActionShowWebElement(getActionInformation());
                break;
            case SHOW_MESSAGE_DIALOG:
                resolver.onActionShowMessageDialog(getActionInformation());
                break;
            case INVALID:
                resolver.onActionInvalid(getActionInformation());
                break;
        }
    }

    public interface ActionResolver{
        public void onActionInvalid(String information);
        public void onActionShowMessageDialog(String messageId);
        public void onActionShowWebElement(String storyId);
    }

}
