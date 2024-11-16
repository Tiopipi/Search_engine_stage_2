package org.ulpgc.bd.repository.interfaces;

import org.ulpgc.bd.model.Metadata;

import java.util.List;

public interface MetadataLoader {
    List<Metadata> loadMetadata(String METADATA_FILE_PATH);
}
