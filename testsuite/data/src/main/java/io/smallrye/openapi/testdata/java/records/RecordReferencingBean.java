package io.smallrye.openapi.testdata.java.records;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema
public class RecordReferencingBean {

    NonBeanRecord theRecord;

    public NonBeanRecord getTheRecord() {
        return theRecord;
    }

    public void setTheRecord(NonBeanRecord theRecord) {
        this.theRecord = theRecord;
    }
}
