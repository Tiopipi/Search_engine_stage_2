package org.ulpgc.bd.control;

import org.ulpgc.bd.model.Metadata;

import java.util.List;

public interface MetadataLoader {
    List<Metadata> loadMetadata(String directory);
}
