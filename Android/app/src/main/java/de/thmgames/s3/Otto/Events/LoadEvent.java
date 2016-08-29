package de.thmgames.s3.Otto.Events;


/**
 * Created by Benedikt on 12.01.2015.
 */
public class LoadEvent {
    public boolean finished = false;
    public double progress=0.0;
    public String identifier = "";

    public LoadEvent(boolean finished, String identifier){
        this.finished=finished;
        if(finished) progress=1.0;
        this.identifier=identifier;
    }

    public LoadEvent(double progress, String identifier){
        if(progress==1.0) this.finished=true;
        this.progress=progress;
        this.identifier=identifier;
    }
}
