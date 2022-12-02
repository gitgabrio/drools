package org.kie.efesto.common.api.identifiers;

import java.util.Objects;

/**
 * A specific subclass of <code>ModelLocalUriId</code> that explicitly stores and provides the <b>modelName</b>
 */
public class NamedLocalUriId extends ModelLocalUriId {

    protected final String modelName;
    protected final String fileName;

    public NamedLocalUriId(LocalUri path, String fileName, String modelName)  {
        super(path);
        this.modelName = modelName;
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    public String modelName() {
        return modelName;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ModelLocalUriId that = (ModelLocalUriId) o;
        return Objects.equals(this.fullPath(), that.fullPath());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
