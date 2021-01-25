package org.opengis.cite.iso19136.util;

import org.apache.sis.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ApacheSisUtils {

    public static Envelope getDomainOfValidity(final CoordinateReferenceSystem crs) {
        return CRS.getDomainOfValidity(crs);
    }
}
