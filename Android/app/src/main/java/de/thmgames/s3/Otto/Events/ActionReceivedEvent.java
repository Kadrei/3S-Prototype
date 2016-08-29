package de.thmgames.s3.Otto.Events;

import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;

/**
 * Created by Benedikt on 25.02.2015.
 */
public class ActionReceivedEvent {
    public Action action;

    public ActionReceivedEvent(Action a){
        this.action=a;
    }

}
