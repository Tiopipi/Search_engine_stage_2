package org.ulpgc.bd.service.interfaces;

import org.ulpgc.bd.model.Metadata;

import java.util.List;
import java.util.Map;

public interface MetadataService {
    List<Metadata> searchMetadata(Map<String, String> filters);
}
