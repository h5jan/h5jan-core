package io.github.h5jan.io.h5;

import org.eclipse.january.metadata.MetadataType;

/**
 * Marker interface for saving metadata.
 * The concrete class which you implement must serialize/deserialize to Json
 * and implement this interface please.
 * 
 * @author Matthew Gerring
 *
 */
public interface NxsMetadata extends MetadataType {

}
