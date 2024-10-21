package io.smallrye.openapi.internal.models.tags;

import io.smallrye.openapi.api.util.VersionUtil;

public class Tag extends AbstractTag {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (VersionUtil.compareMicroProfileVersion("3.0") < 0) {
            // TCK versions before MP OpenAPI release 3.0 check AbstractTag instances are not equal
            return false;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        if (VersionUtil.compareMicroProfileVersion("3.0") < 0) {
            // TCK versions before MP OpenAPI release 3.0 check AbstractTag instances are not equal
            return System.identityHashCode(this);
        }

        return super.hashCode();
    }

}
