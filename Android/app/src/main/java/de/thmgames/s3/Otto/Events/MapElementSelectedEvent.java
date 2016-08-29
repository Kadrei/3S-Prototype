package de.thmgames.s3.Otto.Events;

/**
 * Created by Benedikt on 14.12.2014.
 */
public class MapElementSelectedEvent {
    public int mSelectedParentElementIndex;
    public int mSelectedPartElementIndex;

    public MapElementSelectedEvent(int parent, int part) {
        this.mSelectedParentElementIndex = parent;
        this.mSelectedPartElementIndex = part;
    }


    public String toString() {
        return "Map Element #"+ mSelectedParentElementIndex +" choosen with Parent: "+mSelectedPartElementIndex;
    }
}
