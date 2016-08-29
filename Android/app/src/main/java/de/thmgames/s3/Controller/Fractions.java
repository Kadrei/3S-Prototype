package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.ParseQuery;

import de.thmgames.s3.Model.ParseModels.Fraction;

/**
 * Created by Benedikt on 23.10.2014.
 */
public abstract class Fractions {
    private final static String TAG = Fractions.class.getName();
    public static final String[] STANDARD_INCLUDES = { Fraction.FRACTION_NAME, Fraction.FRACTION_LORE };

    public static void findAllVisibleFractionsInBackground(FindCallback<Fraction> callback) {
        getAllVisibleFractionsQuery().findInBackground(callback);
    }

    public static void findAllVisibleFractionsOrderedByKeyInBackground(String orderByKey, boolean ascending, FindCallback<Fraction> callback) {
        getAllVisibleFractionsOrderedByQuery(orderByKey, ascending).findInBackground(callback);
    }

    public static ParseQuery<Fraction>  getAllVisibleFractionsQuery(){
        ParseQuery<Fraction> query = ParseQuery.getQuery(Fraction.class);
        query.whereEqualTo(Fraction.FRACTION_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        return query;
    }

    public static ParseQuery<Fraction> getAllVisibleFractionsOrderedByQuery(String orderByKey, boolean ascending){
        ParseQuery<Fraction> query = ParseQuery.getQuery(Fraction.class);
        if(ascending){
            query.orderByAscending(orderByKey);
        }else{
            query.orderByDescending(orderByKey);
        }
        query.whereEqualTo(Fraction.FRACTION_VISIBILITY, true);
        for(String include: STANDARD_INCLUDES){
            query.include(include);
        }
        return query;
    }

}
